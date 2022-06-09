package com.scorpio.bsjg.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.databinding.ActivityRightsOfAdministratorsBinding
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.DeviceIdUtil
import kotlinx.coroutines.launch

class RightsOfAdministratorsActivity : BaseActivity<ActivityRightsOfAdministratorsBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_rights_of_administrators
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
            // 返回
            mBackTextView.setOnClickListener {
                finish()
            }
            // 车牌绑定
            mLicensePlateBindingRTextView.setOnClickListener {
                checkDevice(true)
            }
            // 车牌解绑
            mLicensePlateUnbindingRTextView.setOnClickListener {
                checkDevice(false)
            }
            // 其他
            mOtherRTextView.setOnClickListener {

            }
        }
    }

    override fun processingLogic() {
    }

    private fun check(type: String, code: String) {
        lifecycleScope.launch {
            convertReqExecute({ appApi.selectOneCode(code) }, onSuccess = {
                when (type) {
                    "1" -> {
                        checkDevice(true)
                    }
                    "2" -> {
                        checkDevice(false)
                    }
                }
            })
        }
    }

    /**
     * 查询当前设备信息
     */
    private fun checkDevice(isBinding: Boolean) {
        lifecycleScope.launch {
            convertReqExecute({ appApi.transportSelectOne(DeviceIdUtil.getDeviceId(this@RightsOfAdministratorsActivity)) }, onSuccess = {
                mDataBinding.apply {
                    if ((isBinding && TextUtils.isEmpty(it.carId)) || (!isBinding && !TextUtils.isEmpty(it.carId))) {
                        startActivity(BindingAndUnbindingActivity.newIntent(this@RightsOfAdministratorsActivity, Gson().toJson(it)))
                    } else {
                        if (isBinding && !TextUtils.isEmpty(it.carId)) {
                            ToastUtil.showShort("此设备已绑定车辆")
                        }
                        if (!isBinding && TextUtils.isEmpty(it.carId)) {
                            ToastUtil.showShort("此设备未绑定车辆")
                        }
                    }
                }
            }, baseView = this@RightsOfAdministratorsActivity)
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, RightsOfAdministratorsActivity::class.java)
        }
    }
}