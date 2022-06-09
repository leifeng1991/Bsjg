package com.moufans.lib_base.request

/**
 * 描述:通用返回格式
 */
data class BaseResp<out T>(val code: String?, val message: String?, val data: T)