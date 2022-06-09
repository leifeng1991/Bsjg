package com.moufans.lib_base.request

/**
 * 描述:loading框
 */
interface BaseView {
    /**
     * 显示加载框
     */
    fun showLoading()
    
    /**
     * 隐藏加载框
     */
    fun hideLoading()
    
    /**
     * 错误回调
     */
    fun onError(status: Int, message: String)
}