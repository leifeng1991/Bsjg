package com.scorpio.bsjg.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.baseReqExecute
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.adapter.RoleListAdapter
import com.scorpio.bsjg.bean.RequestParamJsonBean
import com.scorpio.bsjg.bean.RoleDataBean
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityRegisterBinding
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.CheckUtils
import com.scorpio.bsjg.utils.DeviceIdUtil
import com.scorpio.bsjg.utils.SharedPrefUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    private val mRoleListAdapter by lazy {
        RoleListAdapter()
    }
    private var mRoleDataBean: RoleDataBean? = null

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_register
    }

    override fun initView() {
        mDataBinding.mRoleRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RegisterActivity)
            adapter = mRoleListAdapter
        }
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun initListener() {
        mDataBinding.apply {
            // 选择角色
            mSelectRoleLayout.setOnClickListener {
                val inputName = mInputAccountEditText.text.toString()
                if (TextUtils.isEmpty(inputName)) {
                    ToastUtil.showShort("请输入姓名")
                    return@setOnClickListener
                }
                mTitleTextView.text = "请选择角色"
                mRoleRecyclerView.visibility = View.VISIBLE
                mAccountLayout.visibility = View.GONE
                mSelectRoleLayout.visibility = View.GONE
                selIdentity()
            }
            // 获取验证码
            mGetYzmRTextView.setOnClickListener {
                getVerificationCode()
            }
            // 注册
            mRegisterLayout.setOnClickListener {
                register()
            }
            // 同意协议
            mDataBinding.mAgreeCheckedTextView.setOnClickListener {
                mDataBinding.mAgreeCheckedTextView.isSelected = !mDataBinding.mAgreeCheckedTextView.isSelected
            }
        }
        mRoleListAdapter.addChildClickViewIds(R.id.mRTextView)
        mRoleListAdapter.setOnItemChildClickListener{ _, _, position ->
            mRoleDataBean = mRoleListAdapter.data[position]
            mDataBinding.apply {
                mTitleTextView.text = "欢迎注册炳圣建工"
                mRoleRecyclerView.visibility = View.GONE
                mPhoneLayout.visibility = View.VISIBLE
                mCodeLayout.visibility = View.VISIBLE
                mRegisterLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun processingLogic() {
    }


    private fun selIdentity() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.selIdentity() }, onSuccess = {
                mRoleListAdapter.setList(it)
            }, baseView = this@RegisterActivity)
        }
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
                }, baseView = this@RegisterActivity)
            }

        }

    }

    /**
     * 注册
     */
    private fun register() {
        val mPhone = mDataBinding.mInputPhoneEditText.text.toString().trim()
        val yzm = mDataBinding.mInputYzmEditText.text.toString().trim()
        if (!CheckUtils.checkInput(mPhone, yzm)) {
            return
        }

        if (!mDataBinding.mAgreeCheckedTextView.isSelected) {
            ToastUtil.showShort("请选中同意用户协议")
            return
        }

        val body = Gson().toJson(RequestParamJsonBean().apply {
            username = mPhone
            code = yzm
            mac =  DeviceIdUtil.getDeviceId(this@RegisterActivity)
        }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        lifecycleScope.launch {
            convertReqExecute({ appApi.logonCode(body) }, onSuccess = {
                lifecycleScope.launch {
                    userUpdate()
                }
            }, baseView = this@RegisterActivity)
        }

    }

    private fun userUpdate(){
        val body = Gson().toJson(RequestParamJsonBean().apply {
            name = mDataBinding.mInputAccountEditText.text.toString()
            code = mRoleDataBean?.code
        }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        lifecycleScope.launch {
            convertReqExecute({ appApi.userUpdate(body) }, onSuccess = {
                lifecycleScope.launch {
                    // 保存token
                    SharedPrefUtil.put(AppConstants.USER_TOKEN, it.token ?: "")
                    // 跳转到首页
                    startActivity(HomeActivity.newIntent(this@RegisterActivity))
                    // 关闭页面
                    finish()
                }
            }, baseView = this@RegisterActivity)
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, RegisterActivity::class.java)
        }
    }
}