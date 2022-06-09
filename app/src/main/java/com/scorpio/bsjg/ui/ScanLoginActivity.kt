package com.scorpio.bsjg.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.king.zxing.util.CodeUtils
import com.king.zxing.util.LogUtils
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.UserToken
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityScanLoginBinding
import com.scorpio.bsjg.event.LoginEvent
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.DeviceIdUtil
import com.scorpio.bsjg.utils.SharedPrefUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class ScanLoginActivity : BaseActivity<ActivityScanLoginBinding>() {
    private var mKey = ""

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_scan_login
    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun addHeaderView() {

    }

    override fun initView() {
    }

    override fun initListener() {
        mDataBinding.mEwmImageView.setOnClickListener {
            if (mDataBinding.mTipTextView.text.toString() == "二维码已失效，请点击刷新")
                getScanCode()
        }
    }

    override fun processingLogic() {
        getScanCode()
    }

    private fun getScanCode() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.padKey() }, onSuccess = {
                mKey = it
                val json = "{ \"data\": \"$it\", \"type\": \"pad_login\"\n}"
                LogUtils.e("==============$json")
                mDataBinding.mEwmImageView.setImageBitmap(CodeUtils.createQRCode(json, 400))
                mDataBinding.mTipTextView.text = "请打开千里科技 手机APP 扫一扫即可登"
                interval(it)
            }, baseView = this@ScanLoginActivity)
        }
    }

    private fun interval(data: String) {
         lifecycleScope.launch {
            delay(1000)
            checkData(data)
        }
    }

    private fun checkData(data: String) {
        lifecycleScope.launch {
            convertReqExecute({ appApi.getResult(data) }, onSuccess = {
                EventBus.getDefault().post(LoginEvent())
                SharedPrefUtil.put(AppConstants.USER_TOKEN, it.token)
                // 保存用户信息
                SharedPrefUtil.put(AppConstants.USER, Gson().toJson(it))
                // 跳转到首页
                startActivity(MapActivity.newIntent(this@ScanLoginActivity))
                finish()
            }, onFailure = { _, code, message ->
                if (code == -202) {
                    mDataBinding.mTipTextView.text = "二维码已失效，请点击刷新"
                    ToastUtil.showShort(message)
                } else {
                    if (mKey == data) {
                        interval(data)
                    }
                }

            }, isShowToast = false)
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, ScanLoginActivity::class.java)
        }
    }
}