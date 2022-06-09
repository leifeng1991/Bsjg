package com.moufans.lib_base.request;

import com.google.gson.Gson;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * retrofit 请求体 post json
 * (非表单体)
 */
public class RetrofitRequestByPostJson {

    /**
     * 获取请求体 通过 javabean
     * @param javabean
     * @return
     */
    public static RequestBody getRequestByEntry(Object javabean){
        RequestBody requestBody = null;
        if(javabean!=null){
            String strEntry = new Gson().toJson(javabean);
            requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),strEntry);
        }
        return requestBody;
    }
    /**
     * 获取请求体 通过 map
     * @param datas
     * @return
     */
    public static RequestBody getRequestByMap(Map<String,String> datas){
        RequestBody requestBody = null;
        if(datas!=null && !datas.isEmpty()){
            String strEntry = new Gson().toJson(datas);
            requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),strEntry);
        }
        return requestBody;
    }
}
