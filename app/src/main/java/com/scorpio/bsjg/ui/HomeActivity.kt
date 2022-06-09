package com.scorpio.bsjg.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.baseReqExecute
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityHomeBinding
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.CommonDialogUtil
import com.scorpio.bsjg.utils.DeviceIdUtil
import com.scorpio.bsjg.utils.DialogBuilder
import com.scorpio.bsjg.utils.SharedPrefUtil
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_home
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
            // 地图
            mMapImageView.setOnClickListener {
//                check("1", "YS")
                startActivity(MapActivity.newIntent(this@HomeActivity))
            }
            // 系统
            xitongTv.setOnClickListener {
//                check("2", "XT")
                startActivity(RightsOfAdministratorsActivity.newIntent(this@HomeActivity))
            }
            buluTv.setOnClickListener {
//                check("3", "BL")
                startActivity(Intent(this@HomeActivity, TaskRecordListActivity::class.java))
            }
            // 退出登录
            mLogoutTextView.setOnClickListener {
                CommonDialogUtil.commonGeneralDialog(this@HomeActivity, "退出登录", "确认要退出登录吗？", object : CommonDialogUtil.OnButtonClickListener {
                    override fun onLeftButtonClick(dialog: DialogBuilder) {
                        logout()
                    }

                    override fun onRightOrCenterButtonClick(dialog: DialogBuilder) {
                    }
                }, false, "确认", "取消")
            }

        }
    }

    override fun processingLogic() {
        findAdminInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun findAdminInfo() {
        lifecycleScope.launch {
            val mac = DeviceIdUtil.getDeviceId(this@HomeActivity)
            convertReqExecute({ appApi.findAdminInfo(mac) }, onSuccess = {
                // 保存用户信息
                SharedPrefUtil.put(AppConstants.USER, Gson().toJson(it))
                mDataBinding.mBottomTextView.text = "当前驾驶员：${it.name}    车牌：${it.carPlate}"
            }, onFailure = { _, _, _ ->
            }, isShowToast = false)
        }

    }

    private fun logout() {
        lifecycleScope.launch {
            baseReqExecute({ appApi.loginOut() }, onSuccess = {
            }, onFailure = { _, status, _ ->
                if (status == -205) {
                    SharedPrefUtil.put(AppConstants.USER_TOKEN, "")
                    SharedPrefUtil.put(AppConstants.USER_CODE, "")
                    SharedPrefUtil.put(AppConstants.USER, "")
                    startActivity(LoginModeChoiceActivity.newIntent(this@HomeActivity))
                    finish()
                }
            }, baseView = this@HomeActivity)
        }

    }

    private fun check(type: String, code: String) {
        lifecycleScope.launch {
            convertReqExecute({ appApi.selectOneCode(code) }, onSuccess = {
                when (type) {
                    "1" -> {
                        startActivity(MapActivity.newIntent(this@HomeActivity))
                    }
                    "2" -> {
                        startActivity(RightsOfAdministratorsActivity.newIntent(this@HomeActivity))
                    }
                    "3" -> {
                        startActivity(Intent(this@HomeActivity, TaskRecordListActivity::class.java))
                    }
                    "4" -> {
                    }
                }
            }, onFailure = { _, _, message ->
                ToastUtil.showShort(message)
            })
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }
}