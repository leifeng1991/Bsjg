package com.moufans.lib_base.base.fragment

import android.os.Bundle
import androidx.databinding.ViewDataBinding

/**
 * 描述:ViewPager fragment
 *
 * @author leifeng
 * 2018/10/23 10:43
 */

abstract class ViewPageFragment<DB : ViewDataBinding> : ReuseViewFragment<DB>() {

    private var isViewInitiated: Boolean = false
    private var isVisibleToUser: Boolean = false
    var isDataInitiated: Boolean = false// 设置data初始化

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 执行顺序 onAttach...onCreate...onCreateView...onActivityCreated...onStart...onResume...
        isViewInitiated = true
        prepareRefreshOnceData()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        prepareRefreshOnceData()
    }

    abstract fun refreshOnceData()

    private fun prepareRefreshOnceData() {
        if (isVisibleToUser && isViewInitiated && !isDataInitiated && view != null) {// view销毁则不通知
            refreshOnceData()
            isDataInitiated = true
        }
    }
}