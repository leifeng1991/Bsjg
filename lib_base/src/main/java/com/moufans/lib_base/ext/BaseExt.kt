package com.moufans.lib_base.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.moufans.lib_base.request.BaseResp
import com.moufans.lib_base.request.BaseView
import com.moufans.lib_base.request.rx.ConvertBaseFunc
import com.moufans.lib_base.request.rx.ConvertDataFunc
import com.moufans.lib_base.request.rx.ResultErrorException
import com.moufans.lib_base.utils.ToastUtil
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import javax.net.ssl.SSLException


/**
 * activity  启动
 * startActivity<DetailActivity>(DetailActivity.ID to item.id, DetailActivity.NAME to item.name)
 *
 */
inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, String>) {
    val intent = Intent(this, T::class.java)
    params.forEach { intent.putExtra(it.first, it.second) }
    startActivity(intent)
}
/**
 * Created by leifeng on 2020/9/14.
 * 功能描述：
 **/
/**
 * 返回值BaseResp
 */
fun <T> Observable<BaseResp<T>>.execute(subscriber: Observer<BaseResp<T>>) {
    this.flatMap(ConvertBaseFunc()).rawExecute(subscriber)
}

/**
 * 返回值BaseResp--绑定生命周期
 */
fun <T> Observable<BaseResp<T>>.execute(subscriber: Observer<BaseResp<T>>, lifecycleProvider: LifecycleProvider<*>) {
    this.flatMap(ConvertBaseFunc()).rawExecute(subscriber, lifecycleProvider)
}


/**
 * 返回值BaseResp--转换Data执行
 */
fun <T> Observable<BaseResp<T>>.convertExecute(subscriber: Observer<T>) {
    this.flatMap(ConvertDataFunc()).rawExecute(subscriber)
}

/**
 * 返回值BaseResp--转换Data执行--绑定生命周期
 */
fun <T> Observable<BaseResp<T>>.convertExecute(subscriber: Observer<T>, lifecycleProvider: LifecycleProvider<*>) {
    this.flatMap(ConvertDataFunc()).rawExecute(subscriber, lifecycleProvider)
}

/**
 * 原始执行
 */
fun <T> Observable<T>.rawExecute(subscriber: Observer<T>) {
    this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber)
}

/**
 * 原始执行-绑定生命周期
 */
fun <T> Observable<T>.rawExecute(subscriber: Observer<T>, lifecycleProvider: LifecycleProvider<*>) {
    this.subscribeOn(Schedulers.io()).compose(lifecycleProvider.bindToLifecycle()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber)

}

/**
 * 协程-原始执行
 */
suspend fun <T, R> rawReqExecute(block: suspend () -> T, onSuccess: ((value: T) -> R)? = null, onFailure: ((exception: Throwable) -> R)? = null, baseView: BaseView? = null): T? {
    baseView?.showLoading()
    runCatching {
        block.invoke()
    }.onSuccess {
        onSuccess?.invoke(it)
        return it
    }.onFailure {
        onFailure?.invoke(it)
    }
    baseView?.hideLoading()
    return null
}


/**
 * 协程-转换执行
 */
suspend fun <T, R> convertReqExecute(block: suspend () -> BaseResp<T>, onSuccess: ((value: T) -> R)? = null, onFailure: ((isResultError: Boolean, status: Int, message: String) -> R)? = null, baseView: BaseView? = null, isShowToast: Boolean = true): T? {

    baseView?.showLoading()
    runCatching {
        block.invoke()
    }.onSuccess {
        baseView?.hideLoading()
        return if (it.code != "200" || it.data == null) {
            handleFailed(ResultErrorException(it.code, it.message ?: ""), onFailure, isShowToast)
            null
        } else {
            onSuccess?.invoke(it.data)
            it.data
        }
    }.onFailure {
        handleFailed(it, onFailure, isShowToast)
        baseView?.hideLoading()
    }

    return null
}

/**
 * 协程-BaseReq执行
 */
suspend fun <T> baseReqExecute(block: suspend () -> BaseResp<T>, onSuccess: ((value: BaseResp<T>) -> T)? = null, onFailure: ((isResultError: Boolean, status: Int, message: String) -> T)? = null, baseView: BaseView? = null, isShowToast: Boolean = true): BaseResp<T>? {
    baseView?.showLoading()
    runCatching {
        block.invoke()
    }.onSuccess {
        if (it.code != "200") {
            handleFailed(ResultErrorException(it.code, it.message ?: ""), onFailure, isShowToast)
        } else {
            onSuccess?.invoke(it)
        }
        baseView?.hideLoading()
        return it

    }.onFailure {
        handleFailed(it, onFailure, isShowToast)
    }
    baseView?.hideLoading()
    return null
}


/**
 * 异常处理
 */
fun <T> handleFailed(throwable: Throwable, onFailure: ((isResultError: Boolean, status: Int, message: String) -> T)? = null, isShowToast: Boolean = true) {
    var status = 0
    val message = when (throwable) {
        is ResultErrorException -> {
            status = throwable.status?.toInt() ?: 0
            throwable.msg
        }
        is HttpException -> {
            when (throwable.code()) {
                401 -> {
                    status = 401
                    "操作未授权"
                }
                403 -> {
                    status = 403
                    "请求被拒绝"
                }
                404 -> {
                    status = 404
                    "资源不存在"
                }
                408 -> {
                    status = 408
                    "服务器执行超时"
                }
                500 -> {
                    status = 500
                    "服务器内部错误"
                }
                503 -> {
                    status = 503
                    "服务器不可用"
                }
                else -> {
                    status = -1
                    "网络错误"
                }
            }
        }
        is ConnectException -> {
            status = 1001
            "连接失败"
        }
        is UnknownHostException -> {
            // 无网络
            status = -1
            "无网络"
        }
        is SocketTimeoutException, is ConnectTimeoutException -> {
            // 网络超时
            status = 1003
            "网络超时"
        }
        is JsonSyntaxException, is JSONException, is ParseException, is MalformedJsonException -> {
            // 解析错误
            status = 1004
            "解析错误"
        }
        is SSLException -> {
            status = 1005
            "证书验证失败"
        }
        else -> {
            status = 1000
            // 未知错误，打印出来，遇到可添加如上处理，正式的时候不会打印此消息
            "未知错误"
        }
    }

    onFailure?.let { it -> it(true, status, message ?:"") }

    if (isShowToast && status != -205) {
        ToastUtil.showShort(message)
    }
}

