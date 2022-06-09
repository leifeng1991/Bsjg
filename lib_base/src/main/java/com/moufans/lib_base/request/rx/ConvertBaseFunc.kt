package com.moufans.lib_base.request.rx

import com.moufans.lib_base.request.BaseResp
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function

/**
 * 通用数据类型转换封装
 */
class ConvertBaseFunc<T> : Function<BaseResp<T>, Observable<BaseResp<T>>> {
    override fun apply(t: BaseResp<T>): Observable<BaseResp<T>> {
        if (t.code != "200" || t.data == null) {
            return Observable.error(ResultErrorException(if (t.data == null) "$FAILED_STATUS_NO_DATA" else t.code, t.message))
        }
        return Observable.just(t)
    }
}
