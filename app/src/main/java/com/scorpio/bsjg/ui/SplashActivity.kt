package com.scorpio.bsjg.ui

import androidx.lifecycle.lifecycleScope
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.utils.DeviceUtils
import com.moufans.lib_base.utils.StatusBarUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_splash
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setDarkMode(this)
    }

    override fun initView() {
        mDataBinding.mVersionNameTextView.text = "Version ${DeviceUtils.getVersionName(this)}"
    }

    override fun initListener() {
    }

    override fun processingLogic() {
        lifecycleScope.launch {
            repeat(3) {
                delay(1000)
                if (it == 2) {
                    startActivity(LoginModeChoiceActivity.newIntent(this@SplashActivity))
                    finish()
                }
            }
        }
    }
}