package com.moufans.lib_base.request.rx

/**
 * 定义通用异常
 */
class ResultErrorException(val status: String?, val msg: String?) : Throwable()
