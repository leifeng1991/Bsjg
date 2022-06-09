package com.moufans.lib_base.base.adapter

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder


abstract class BaseAdapter<T, BD : ViewDataBinding>(@LayoutRes layoutResId: Int, data: MutableList<T>?) : BaseQuickAdapter<T, BaseDataBindingHolder<BD>>(layoutResId, data) {

    override fun convert(holder: BaseDataBindingHolder<BD>, item: T) {
        convert(holder, holder.layoutPosition - headerLayoutCount, item)
    }

    protected abstract fun convert(holder: BaseDataBindingHolder<BD>, position: Int, item: T)

    /**
     * 移除item
     */
    fun removeItem(oldItem: T) {
        val position = data.indexOf(oldItem)
        if (position == -1) return
        remove(position)
    }

    /**
     * 最后添加item
     */
    fun addItemAtLast(item: T) {
        addData(item)
    }

}