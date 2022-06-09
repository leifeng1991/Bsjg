package com.moufans.lib_base.base.recyclerview.diff

/**
 * Created by leifeng on 2020/9/24.
 * 功能描述：
 */
abstract class  BaseDiff {
    abstract fun uniqueValue(): String?
    abstract fun contentValue(): String?
}