package com.scorpio.bsjg.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.update.OnAppUpdateListener
import com.moufans.update.VersionDataBean
import com.scorpio.bsjg.R
import com.scorpio.bsjg.databinding.ActivityLoginModeChoiceBinding
import com.scorpio.bsjg.event.LoginEvent
import com.scorpio.bsjg.utils.AppVersionUpdateUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoginModeChoiceActivity : BaseActivity<ActivityLoginModeChoiceBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_login_mode_choice
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
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        mDataBinding.apply {
            // 扫码登录
            mEwmLoginLayout.setOnClickListener {
                startActivity(ScanLoginActivity.newIntent(this@LoginModeChoiceActivity))
            }
            // 账号密码登录
            mPassWordLoginLayout.setOnClickListener {
                startActivity(LoginActivity.newIntent(this@LoginModeChoiceActivity))
            }
        }
    }

    override fun processingLogic() {
        val xp = XXPermissions.with(this) // 不适配 Android 11 可以这样写
        //.permission(Permission.Group.STORAGE)
        // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            xp.permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            xp.permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            xp.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        xp.request(object : OnPermissionCallback {
            override fun onGranted(permissions: List<String>, all: Boolean) {
                if (all) {
                    AppVersionUpdateUtils.checkVersion(this@LoginModeChoiceActivity, object : OnAppUpdateListener {
                        override fun onSuccess(appVersionInfo: VersionDataBean?) {
                            AppVersionUpdateUtils.upDataVersion(this@LoginModeChoiceActivity, appVersionInfo) { url, isUpdate ->
                                if (isUpdate) {
                                    AppVersionUpdateUtils.toDownload(this@LoginModeChoiceActivity, url)
                                }
                            }
                        }

                        override fun onFailed(failedMessage: String?) {
                        }

                    })
                }
            }

            override fun onDenied(permissions: List<String>, never: Boolean) {
                if (never) {
                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                    XXPermissions.startPermissionActivity(this@LoginModeChoiceActivity, permissions)
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun loginSuccess(event: LoginEvent) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, LoginModeChoiceActivity::class.java)
        }
    }
}