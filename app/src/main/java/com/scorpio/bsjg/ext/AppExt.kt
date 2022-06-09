package com.scorpio.bsjg.ext

import com.moufans.lib_base.request.net.HttpLogInterceptor
import com.moufans.lib_base.request.net.RetrofitFactory
import com.scorpio.bsjg.BuildConfig.BASE_URL
import com.scorpio.bsjg.api.AppApi
import com.scorpio.bsjg.utils.AppHttpAddHeadersInterceptor
import com.scorpio.bsjg.utils.LoginInterceptor

/**
 * 功能描述：扩展
 **/
val RetrofitFactory.Companion.appInstance
    get() = getInstance(BASE_URL, AppHttpAddHeadersInterceptor(), HttpLogInterceptor())

val appApi by lazy { RetrofitFactory.appInstance.create(AppApi::class.java) }