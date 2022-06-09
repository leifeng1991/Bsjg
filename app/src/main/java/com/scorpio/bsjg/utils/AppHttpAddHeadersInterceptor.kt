package com.scorpio.bsjg.utils

import com.moufans.lib_base.request.net.HttpAddHeadersInterceptor
import com.moufans.lib_base.utils.DeviceUtils
import com.moufans.lib_base.utils.InitUtils.getApplication
import com.scorpio.bsjg.constants.AppConstants
import okhttp3.Request

class AppHttpAddHeadersInterceptor : HttpAddHeadersInterceptor() {
    override fun setHeader(request: Request.Builder) {
        super.setHeader(request)
        request.addHeader("Version", DeviceUtils.getVersionName(getApplication()))
        request.addHeader("token", SharedPrefUtil.get(AppConstants.USER_TOKEN, "") ?: "")
        request.addHeader("mac", DeviceIdUtil.getDeviceId(getApplication()))
        request.addHeader("imei", DeviceIdUtil.getIMEI(getApplication()))
        request.addHeader("androidId", DeviceIdUtil.getAndroidId(getApplication()))
        request.addHeader("serialNumber", DeviceIdUtil.getSERIAL())
    }
}