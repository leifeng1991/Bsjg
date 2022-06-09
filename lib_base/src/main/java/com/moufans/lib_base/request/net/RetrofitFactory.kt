package com.moufans.lib_base.request.net

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 描述: 网络请求工厂
 */
class RetrofitFactory(baseUrl: String, vararg interceptors: Interceptor) {

    // 单例
    companion object {
        private var mRetrofitFactory: RetrofitFactory? = null
        fun getInstance(baseUrl: String, vararg interceptors: Interceptor): RetrofitFactory {
            if (mRetrofitFactory == null) {
                synchronized(RetrofitFactory::class.java) {
                    if (mRetrofitFactory == null)
                        mRetrofitFactory = RetrofitFactory(baseUrl, *interceptors)
                }
            }
            return mRetrofitFactory!!
        }
    }

    private var retrofit: Retrofit

    init {
        // 封装 client
        val builder = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)// 读超时
                .writeTimeout(60, TimeUnit.SECONDS)// 写超时
        // 动态添加interceptor
        if (interceptors.isNotEmpty())
            for (interceptor in interceptors) {
                if (interceptor is NetworkInterceptorFlag)
                    builder.addNetworkInterceptor(interceptor)
                else
                    builder.addInterceptor(interceptor)
            }
        val client = builder.build()
        // 封装 retrofit
        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(client)
                .build()
    }

    /**
     * 具体服务实例化
     */
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}