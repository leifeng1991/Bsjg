package com.moufans.lib_base.base.fragment

import android.os.Bundle
import androidx.databinding.ViewDataBinding

/**
 * 描述:页面显示的时候就刷新
 */
abstract class HintRefreshFragment<DB : ViewDataBinding> : ReuseViewFragment<DB>() {

    protected var isViewInitiated: Boolean = false
    protected var isVisibleToUser: Boolean = false

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

    abstract fun hintRefreshData()

    open fun prepareRefreshOnceData() {
        if (isVisibleToUser && isViewInitiated) {
            hintRefreshData()
        }
    }

}
