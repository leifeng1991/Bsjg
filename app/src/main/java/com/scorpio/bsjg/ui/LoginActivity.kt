package com.scorpio.bsjg.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.baseReqExecute
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.ext.setOnClickListener2
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.RequestParamJsonBean
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityLoginBinding
import com.scorpio.bsjg.event.LoginEvent
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.CheckUtils
import com.scorpio.bsjg.utils.DeviceIdUtil
import com.scorpio.bsjg.utils.SharedPrefUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_login
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun initView() {
    }

    override fun initListener() {
        mDataBinding.apply {
            // 登录
            mLoginRTextView.setOnClickListener2 {
                login()
            }
            // 注册
            mRegisterTextView.setOnClickListener2 {
                startActivity(RegisterActivity.newIntent(this@LoginActivity))
            }
            // 获取验证码
            mGetYzmRTextView.setOnClickListener {
                getVerificationCode()
            }
            // 密码登录
            mPasswordLoginTextView.setOnClickListener {
                if ("用密码登录" == mPasswordLoginTextView.text.toString()) {
                    mPasswordLayout.visibility = View.VISIBLE
                    mForgetPasswordTextView.visibility = View.VISIBLE
                    mAccountLayout.visibility = View.VISIBLE
                    mCodeLayout.visibility = View.GONE
                    mPhoneLayout.visibility = View.GONE
                    mPasswordLoginTextView.text = "用手机号登录"
                } else {
                    mPasswordLayout.visibility = View.GONE
                    mAccountLayout.visibility = View.GONE
                    mForgetPasswordTextView.visibility = View.GONE
                    mCodeLayout.visibility = View.VISIBLE
                    mPhoneLayout.visibility = View.VISIBLE
                    mPasswordLoginTextView.text = "用密码登录"
                }

            }
            // 同意协议
            mDataBinding.mAgreeCheckedTextView.setOnClickListener {
                mDataBinding.mAgreeCheckedTextView.isSelected = !mDataBinding.mAgreeCheckedTextView.isSelected
            }
        }
    }

    override fun processingLogic() {
    }

    /**
     * 获取验证码
     */
    private fun getVerificationCode() {
        val mPhone = mDataBinding.mInputPhoneEditText.text.toString().trim()
        if (CheckUtils.checkPhoneNumber(mPhone)) {
            lifecycleScope.launch {
                val json = Gson().toJson(RequestParamJsonBean().apply {
                    phone = mPhone
                })
                LogUtil.e("================================$json===")
                baseReqExecute({ appApi.getVerifyCode(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)) }, onSuccess = {
                    lifecycleScope.launch {
                        repeat(61) {
                            if (it == 60) {
                                mDataBinding.mGetYzmRTextView.text = "获取验证码"
                            } else {
                                mDataBinding.mGetYzmRTextView.text = "${60 - it} 秒"
                                delay(1000)
                            }

                        }
                    }
                }, baseView = this@LoginActivity)
            }

        }

    }

    /**
     * 登录
     */
    private fun login() {
        if (!mDataBinding.mAgreeCheckedTextView.isSelected) {
            ToastUtil.showShort("请选中同意用户协议")
            return
        }
        var mPhoneAccount = ""
        if (mDataBinding.mPhoneLayout.visibility == View.VISIBLE) {
            val mPhone = mDataBinding.mInputPhoneEditText.text.toString().trim()
            mPhoneAccount = mPhone
            if (!CheckUtils.checkPhoneNumber(mPhone)) {
                return
            }
        } else {
            val mAccount = mDataBinding.mInputAccountEditText.text.toString().trim()
            mPhoneAccount = mAccount
            if (TextUtils.isEmpty(mAccount)) {
                ToastUtil.showShort("请输入账号")
                return
            }
        }


        val yzm = mDataBinding.mInputYzmEditText.text.toString().trim()
        val mPassword = mDataBinding.mInputPasswordEditText.text.toString().trim()

        if (View.VISIBLE == mDataBinding.mCodeLayout.visibility) {

            // 短信验证码登录
            if (!CheckUtils.checkInput(yzm)) {
                return
            }
        } else {
            // 密码登录
            if (!CheckUtils.checkPassWord(mPassword)) {
                return
            }
        }


        val body = Gson().toJson(RequestParamJsonBean().apply {
            username = mPhoneAccount
            if (View.GONE == mDataBinding.mCodeLayout.visibility) {
                password = mPassword
            } else {
                code = yzm
            }

        }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        lifecycleScope.launch {
            convertReqExecute({ if (View.GONE == mDataBinding.mCodeLayout.visibility) appApi.login(body) else appApi.loginCode(body) }, onSuccess = {
                // 保存token
                SharedPrefUtil.put(AppConstants.USER_TOKEN, it.token)
                LogUtil.e("===========token==${it.token}=====${SharedPrefUtil.get(AppConstants.USER_TOKEN, "")}")
                if (mPhoneAccount == "admin") {
                    EventBus.getDefault().post(LoginEvent())
                    // 跳转到首页
                    startActivity(HomeActivity.newIntent(this@LoginActivity))
                    finish()
                } else {
                    findAdminInfo()
                }
            }, baseView = this@LoginActivity)
        }

    }

    private fun findAdminInfo() {
        lifecycleScope.launch {
            val mac = DeviceIdUtil.getDeviceId(this@LoginActivity)
            convertReqExecute({ appApi.findAdminInfo(mac) }, onSuccess = {
                EventBus.getDefault().post(LoginEvent())
                // 保存用户信息
                SharedPrefUtil.put(AppConstants.USER, Gson().toJson(it))
                // 跳转到首页
                startActivity(MapActivity.newIntent(this@LoginActivity))
                finish()
            }, onFailure = { _, _, _ ->
            }, isShowToast = false)
        }

    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}