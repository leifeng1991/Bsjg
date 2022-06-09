package com.moufans.lib_base.request.net

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by leifeng on 2020/9/14.
 * 功能描述：请求头拦截器
 */
open class HttpAddHeadersInterceptor : Interceptor {
    
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().apply {
            addHeader("Content-Type", "application/json;charset=utf-8")
            setHeader(this)
        }.build()
        return chain.proceed(request)
    }
    
    open fun setHeader(request: Request.Builder){
    
    }
    
}