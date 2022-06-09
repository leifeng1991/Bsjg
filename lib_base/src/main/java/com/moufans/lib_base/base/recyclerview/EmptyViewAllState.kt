package com.moufans.lib_base.base.recyclerview

/**
 * 描述:空布局状态接口
 */
interface EmptyViewAllState : EmptyViewFailedState {
    /**
     * 成功没数据
     */
    fun onSuccessNoData()

}

interface EmptyViewFailedState {

    /**
     * 无网络失败
     */
    fun onFailedNoNet()

    /**
     * 其它失败
     */
    fun onFailedOther()
}