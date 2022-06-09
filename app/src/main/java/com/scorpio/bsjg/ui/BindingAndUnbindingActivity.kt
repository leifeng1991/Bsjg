package com.scorpio.bsjg.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.RequestParamJsonBean
import com.scorpio.bsjg.bean.TransportSelectOneDataBean
import com.scorpio.bsjg.databinding.ActivityBindingAndUnbindingBinding
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.DeviceIdUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class BindingAndUnbindingActivity : BaseActivity<ActivityBindingAndUnbindingBinding>() {

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_binding_and_unbinding
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun initView() {
        LogUtil.e("IMEI号:${DeviceIdUtil.getIMEI(this@BindingAndUnbindingActivity)}        AndroidId:${DeviceIdUtil.getAndroidId(this)}        序列号：${DeviceIdUtil.getSERIAL()}")
        mDataBinding.mDeviceIdTextView.text = "IMEI号:${DeviceIdUtil.getIMEI(this@BindingAndUnbindingActivity)}        AndroidId:${DeviceIdUtil.getAndroidId(this)}        序列号：${DeviceIdUtil.getSERIAL()}"
    }

    override fun initListener() {
        mDataBinding.apply {
            mBackTextView.setOnClickListener {
                finish()
            }
            mSureButton.setOnClickListener {
                if ("绑定" == mSureButton.text.toString()) {
                    upCar()
                } else if ("解除绑定" == mSureButton.text.toString()) {
                    upCarNo()
                }
            }
        }
    }

    override fun processingLogic() {
        mDataBinding.apply {
            val it = Gson().fromJson(intent.getStringExtra(INTENT_P_JSON_BEAN), TransportSelectOneDataBean::class.java)
            mContentLayout.visibility = View.VISIBLE
            mDeviceNumberTextView.text = "本车载设备编号：${it.number}"
            if (TextUtils.isEmpty(it.carId)) {
                mSureButton.text = "绑定"
                mInputEditText.hint = "请输入车牌"
            } else {
                mSureButton.text = "解除绑定"
                mInputEditText.isEnabled = false
                mInputEditText.setText("绑定车牌：${it.carPlate}")
            }
        }
    }

    private fun upCar() {
        val it = Gson().fromJson(intent.getStringExtra(INTENT_P_JSON_BEAN), TransportSelectOneDataBean::class.java)
        val mCarPlate = mDataBinding.mInputEditText.text.toString()
        if (TextUtils.isEmpty(mCarPlate)) {
            ToastUtil.showShort("请输入车牌号")
            return
        }
        val body = Gson().toJson(RequestParamJsonBean().apply {
            // 设备id
            id = it.id
            // 车牌号
            carPlate = mCarPlate
        }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        lifecycleScope.launch {
            convertReqExecute({ appApi.upCar(body) }, onSuccess = {
                // 关闭页面
                this@BindingAndUnbindingActivity.finish()
                ToastUtil.showShort("绑定成功")
            }, baseView = this@BindingAndUnbindingActivity)
        }
    }

    private fun upCarNo() {
        val it = Gson().fromJson(intent.getStringExtra(INTENT_P_JSON_BEAN), TransportSelectOneDataBean::class.java)
        val body = Gson().toJson(RequestParamJsonBean().apply {
            // 设备id
            id = it.id
        }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        lifecycleScope.launch {
            convertReqExecute({ appApi.upCarNo(body) }, onSuccess = {
                // 关闭页面
                this@BindingAndUnbindingActivity.finish()
                ToastUtil.showShort("解绑成功")
            }, onFailure = { _, status, message ->
                if (status == 200) {
                    ToastUtil.showShort("解绑成功")
                    // 关闭页面
                    this@BindingAndUnbindingActivity.finish()
                } else {
                    ToastUtil.showShort(message)
                }
            }, baseView = this@BindingAndUnbindingActivity, isShowToast = false)
        }
    }

    companion object {
        private const val INTENT_P_JSON_BEAN = "TransportSelectOneDataBeanJson"

        fun newIntent(context: Context, json: String): Intent {
            return Intent(context, BindingAndUnbindingActivity::class.java).apply {
                putExtra(INTENT_P_JSON_BEAN, json)
            }
        }
    }
}