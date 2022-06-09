package com.moufans.lib_base.request.rx

import android.text.TextUtils
import com.google.gson.JsonSyntaxException
import com.moufans.lib_base.request.BaseView
import com.moufans.lib_base.utils.ToastUtil
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 参数可传null，代表不展示loading
 */
const val FAILED_STATUS_CODE_NO_NET = -1// 失败码：无网络
const val FAILED_STATUS_CODE_OTHER = -2// 失败码：其它
const val FAILED_STATUS_NO_DATA = -3// 请求成功，数据返回为null

abstract class OnRequestListener<T>(private val baseView: BaseView?) : Observer<T> {
    
    override fun onSubscribe(d: Disposable) {
        onStart()
    }
    
    override fun onNext(t: T) {
        onSuccess(t)
        onEnd()
    }
    
    override fun onComplete() {
        // 	成功：onSubscribe->onNext->onComplete
        //	失败：onSubscribe->onError
    }
    
    override fun onError(e: Throwable) {
        if (e is ResultErrorException) {
            // 服务器返回的异常
            onFailed(true, if (TextUtils.isEmpty(e.status)) 0 else e.status?.toInt() ?: 0, e.msg ?: "")
        } else {
            // 网络、解析等异常
            val status: Int
            val exceptionMessage = when (e) {
                is UnknownHostException -> {
                    // 无网络
                    status = FAILED_STATUS_CODE_NO_NET
                    "无网络"
                }
                is SocketTimeoutException -> {
                    // 网络超时
                    status = FAILED_STATUS_CODE_NO_NET
                    "网络超时"
                }
                is JsonSyntaxException -> {
                    // 解析错误
                    status = FAILED_STATUS_CODE_OTHER
                    "解析错误"
                }
                else -> {
                    // 未知错误，打印出来，遇到可添加如上处理，正式的时候不会打印此消息
                    status = FAILED_STATUS_CODE_OTHER
                    e.printStackTrace()
                    e.message ?: "未知错误"
                }
            }
            onFailed(false, status, exceptionMessage)
        }
        onEnd()
    }
    
    /**
     * 开始
     */
    open fun onStart() {
        baseView?.showLoading()
    }
    
    /**
     * 成功
     */
    abstract fun onSuccess(bean: T)
    
    /**
     * 失败-所有情况
     */
    open fun onFailed(isResultError: Boolean, status: Int, message: String) {
        if (isResultError)
            onFailedError(status, message)
        else
            onFailedException(status, message)
    }
    
    /**
     * 失败-服务器返回错误信息
     */
    open fun onFailedError(status: Int, message: String) {
        // TODO 不应该在此提示，后期考虑架构
        ToastUtil.showShort(message)
    }
    
    /**
     * 失败-网络或解析等异常
     */
    open fun onFailedException(status: Int, message: String) {
        ToastUtil.showShort(message)// 内部处理，正式的时候不会打印具体错误信息
    }
    
    /**
     * 结束
     */
    open fun onEnd() {
        baseView?.hideLoading()
    }
}