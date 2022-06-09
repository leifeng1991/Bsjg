package com.moufans.lib_base.base.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil

/**
 * Created by leifeng on 2020/9/24.
 * 功能描述：
 */
class Diff<T : BaseDiff> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.uniqueValue() == newItem.uniqueValue()
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.contentValue() == newItem.contentValue()
    }
}