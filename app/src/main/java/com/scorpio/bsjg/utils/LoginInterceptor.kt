package com.scorpio.bsjg.utils

import android.text.TextUtils
import com.google.gson.Gson
import com.moufans.lib_base.utils.LogUtil
import com.scorpio.bsjg.bean.BaseBean
import com.scorpio.bsjg.event.LoginEvent
import okhttp3.Interceptor
import okhttp3.Response
import okio.BufferedSource
import org.greenrobot.eventbus.EventBus
import java.nio.charset.Charset

class LoginInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request)
        val responseBody = response.body
        if (responseBody != null && responseBody.contentLength() != 0L) {
            val source: BufferedSource = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            try {
                val json = buffer.clone().readString(Charset.forName("UTF-8"))
                if (!TextUtils.isEmpty(json)) {
                    val bean = Gson().fromJson(json, BaseBean::class.java)
                    if (bean.code == "-205") {
                        EventBus.getDefault().post(LoginEvent())
                    }
                }
            } catch (e: Exception) {

            }
            LogUtil.i("数据返回")
        }
        return response
    }
}