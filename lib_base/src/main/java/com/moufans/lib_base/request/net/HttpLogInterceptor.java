package com.moufans.lib_base.request.net;


import com.moufans.lib_base.utils.LogUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class HttpLogInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        long startNs = System.nanoTime();
        Request request = chain.request();

        Response response = chain.proceed(request);

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        // 打印请求地址
        LogUtil.i("数据请求地址", request.url().toString() + "时间==" + tookMs + "ms)");
        // 打印请求头
        Headers requestHeaders = request.headers();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < requestHeaders.size(); i++) {
            String name = requestHeaders.name(i);
            String value = requestHeaders.value(i);
            stringBuilder.append("（").append(name).append(" = ").append(value).append("）");
        }

        LogUtil.i("数据请求头", stringBuilder.toString());
        // 打印请求参数
        RequestBody requestBody = request.body();
        if (requestBody != null && requestBody.contentLength() != 0L) {
            Buffer requestBuffer = new Buffer();
            requestBody.writeTo(requestBuffer);
            String requestParamsStr = requestBuffer.readString(Charset.forName("UTF-8"));
            LogUtil.i("数据请求参数", requestParamsStr);
        }

            ResponseBody responseBody = response.body();
        if (responseBody != null && responseBody.contentLength() != 0L) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            LogUtil.i("数据返回", buffer.clone().readString(Charset.forName("UTF-8")));
        }
        return response;
    }

}
