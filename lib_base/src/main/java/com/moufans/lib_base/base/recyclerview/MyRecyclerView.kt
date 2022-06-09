package com.moufans.lib_base.base.recyclerview

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.moufans.lib_base.R
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import java.util.*


@Suppress("unused")
/**
 * 描述: 封装的 RecyclerView
 * 2016/9/2 14:08
 */
class MyRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle) {
    /**
     * 设置每次加载的数量
     */
    var loadSize = 10
    
    /**
     * 获取RecyclerView
     */
    lateinit var recyclerView: RecyclerView
    
    /**
     * 获取SmartRefreshLayout
     */
    lateinit var smartRefreshLayout: SmartRefreshLayout
    
    /**
     * 默认EmptyView（即没设置EmptyView）的【SuccessNoData】的View
     */
    private var mDefaultEmptyViewSuccessNoDataView: View? = null
    
    /**
     * 默认EmptyView（即没设置EmptyView）的【failed】的View
     */
    private var mDefaultEmptyViewFailedView: View? = null
    
    /**
     * 无网络按钮点击监听
     */
    private var onNoNetViewClickListener: OnClickListener? = null
    
    /**
     * 获取刷新监听
     */
    private var refreshListener: OnRefreshListener? = null
    
    /**
     * 当前页数
     */
    var currentPage = 1
    
    /**
     * 上下文
     */
    private var mContext: Context? = null
    
    /**
     * 获取 BaseAdapter
     *
     * @return 如果没设置 BaseAdapter 则返回 null
     */
    val baseAdapter: BaseQuickAdapter<*, *>?
        get() = if (recyclerView.adapter is BaseQuickAdapter<*, *>) recyclerView.adapter as BaseQuickAdapter<*, *>? else null
    
    init {
        init(context)
    }
    
    private fun init(context: Context) {
        this.mContext = context
        val rootView = View.inflate(context, R.layout.base_my_recyclerview, null)
        // initView
        smartRefreshLayout = rootView.findViewById(R.id.smartRefreshLayout)
        recyclerView = rootView.findViewById(R.id.recyclerView)
        // init
        smartRefreshLayout.setRefreshHeader(ClassicsHeader(context))
        smartRefreshLayout.setRefreshFooter(ClassicsFooter(context))
        setPullRefreshAndLoadingMoreEnabled(pullRefreshEnabled = true, loadingMoreEnabled = false)// 默认设置可刷新，不可加载
        setLayoutManager(LinearLayoutManager(mContext))// 默认线性布局
        // addView
        addView(rootView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    
    /**
     * 处理成功，适用于带刷新加载的
     */
    fun <T, BD : ViewDataBinding> handlerSuccess(adapter: BaseQuickAdapter<T, BaseDataBindingHolder<BD>>, addData: List<T>?) {
        var addListData = addData
        // 检查是否初始化 EmptyView
        checkInitEmptyView(adapter)
        // 设置内容
        if (addListData == null) addListData = ArrayList()// 兼容null
        val size = addListData.size
        if (currentPage == 1) {
            // 刷新
            adapter.replaceData(addListData)// 先清后删数据
            refreshComplete()// 设置动画刷新完成
            loadMoreComplete()// 设置动画加载完成，因为有可能是加载
            smartRefreshLayout.setNoMoreData(false)// 重置
        } else {
            // 加载
            if (addListData.isNotEmpty())
                adapter.addData(addListData)// 增数据
            loadMoreComplete()// 设置动画加载完成
        }
        
        if (size < loadSize) {
            // 显示没有更多数据
            smartRefreshLayout.finishLoadMore(0, true, true)
        }
        // 设置空布局显示
        // EmptyView记录最后一次网络成功或者失败
        // 成功但是没有数据，显示0【成功没数据】
        setEmptyViewLayout(0)
    }
    
    /**
     * RecyclerView处理失败，刷新失败清空数据
     */
    fun <T, BD : ViewDataBinding> handlerError(adapter: BaseQuickAdapter<T, BaseDataBindingHolder<BD>>?, failedStatusCode: Int) {
        if (adapter == null) return
        // 检查是否初始化 EmptyView，空布局的显示会在BaseQuickAdapter设置值的时候判断，有内容的时候不显示空布局
        checkInitEmptyView(adapter)
        // 设置内容
        if (currentPage == 1) {
            // 刷新
            adapter.replaceData(ArrayList<T>())// 刷新失败，清空内容
            refreshComplete()// 设置刷新动画结束
            loadMoreComplete()// 设置动画加载完成，因为有可能是加载
        } else {
            // 加载
            adapter.loadMoreModule.loadMoreFail()// 加载失败，设置加载失败提示
            loadMoreComplete()// 设置加载动画结束
        }
        // 设置空布局显示
        // EmptyView记录最后一次网络成功或者失败
        // 失败，显示1【失败（包括无网络，服务返回失败提示）】
        setEmptyViewLayout(1, failedStatusCode)
    }
    
    /**
     * 检查是否初始化 EmptyView
     */
    private fun <T, BD : ViewDataBinding> checkInitEmptyView(adapter: BaseQuickAdapter<T, BaseDataBindingHolder<BD>>?) {
        if (adapter == null)
            return
        val emptyView = if (adapter.emptyLayout is ViewGroup) adapter.emptyLayout as ViewGroup else null
        if (emptyView == null || emptyView.childCount == 0) {
            // 没有设置过空布局，设置空布局
            val defaultEmptyView = EmptyView(context)
            defaultEmptyView.onRefreshBtnClickListener = OnClickListener { view ->
                // 有监听的话，先走监听，其次没监听的话，走刷新
                onNoNetViewClickListener?.onClick(view)
                        ?: refreshListener?.onRefresh(smartRefreshLayout)
            }
            // 设置SuccessNoDataView
            if (mDefaultEmptyViewSuccessNoDataView != null)
                defaultEmptyView.setSuccessNoDataView(mDefaultEmptyViewSuccessNoDataView!!)
            // 设置SuccessNoDataView
            if (mDefaultEmptyViewFailedView != null)
                defaultEmptyView.setFailedView(mDefaultEmptyViewFailedView!!)
            // 设置空布局
            adapter.setEmptyView(defaultEmptyView)
        }
    }
    
    /**
     * 设置 EmptyView 的显示，[emptyViewState]，0为成功没数据，1为失败（包括无网络，服务返回失败提示）
     * [failedStatusCode] 错误状态码，[emptyViewState]为【1】时必传
     */
    private fun setEmptyViewLayout(emptyViewState: Int, failedStatusCode: Int = 0) {
        val emptyView = baseAdapter?.emptyLayout
        if (emptyView != null && emptyView is ViewGroup && emptyView.childCount > 0) {
            // 有emptyView，获取第一个
            val mEmptyView = emptyView.getChildAt(0)!!
            if (mEmptyView is EmptyViewAllState) {
                // 有状态的EmptyView，控制状态
                when (emptyViewState) {
                    0 -> mEmptyView.onSuccessNoData()
//                    1 -> if (failedStatusCode == FAILED_STATUS_CODE_NO_NET) mEmptyView.onFailedNoNet() else mEmptyView.onFailedOther()
                    1 -> if (failedStatusCode == -1) mEmptyView.onFailedNoNet() else mEmptyView.onFailedOther()
                }
            }
        }
    }
    
    /**
     * 单独设置刷新监听器
     */
    fun setOnRefreshListener(listener: OnRefreshListener) {
        this.refreshListener = listener
        smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            // 设置页数
            currentPage = 1
            // 通知刷新
            listener.onRefresh(refreshLayout)
        }
    }
    
    /**
     * 单独设置加载监听器
     */
    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        smartRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            // 设置页数，大于等于一页内容（说明还有下一页），获取下一页的，否则获取第一页的
            val baseAdapter = baseAdapter
            currentPage = if (baseAdapter != null && baseAdapter.data.size >= loadSize) baseAdapter.data.size / loadSize + 1 else 1
            // 通知加载
            listener.onLoadMore(refreshLayout)
        }
    }
    
    /**
     * 同时设置刷新和加载监听器
     */
    fun setOnRefreshLoadMoreListener(listener: OnRefreshLoadMoreListener) {
        setOnRefreshListener(listener)// 设置刷新的监听
        setOnLoadMoreListener(listener)// 设置加载的监听
    }
    
    /**
     * 设置刷新和加载是否可用
     *
     * @param pullRefreshEnabled true为设置刷新可用
     * @param loadingMoreEnabled true为设置加载可用
     */
    fun setPullRefreshAndLoadingMoreEnabled(pullRefreshEnabled: Boolean, loadingMoreEnabled: Boolean) {
        setPullRefreshEnabled(pullRefreshEnabled)
        setLoadingMoreEnabled(loadingMoreEnabled)
    }
    
    /**
     * 设置是否可以下拉刷新
     */
    fun setPullRefreshEnabled(pullRefreshEnabled: Boolean) {
        smartRefreshLayout.setEnableRefresh(pullRefreshEnabled)
    }
    
    /**
     * 设置是否可以上拉加载更多
     */
    fun setLoadingMoreEnabled(loadingMoreEnabled: Boolean) {
        smartRefreshLayout.setEnableLoadMore(loadingMoreEnabled)
    }
    
    /**
     * 设置RecyclerView的adapter
     */
    fun <T, BD : ViewDataBinding> setAdapter(adapter: BaseQuickAdapter<T, BaseDataBindingHolder<BD>>) {
        // 默认设置头和尾和空布局如果都有的话都显示，在setAdapter之前调用
        this.baseAdapter?.headerWithEmptyEnable = true
        this.baseAdapter?.footerWithEmptyEnable = true
        // 检查是否初始化 EmptyView
//        checkInitEmptyView(adapter)// 默认进来先不显示空布局
        // 设置adapter
        recyclerView.adapter = adapter
    }
    
    /**
     * 设置RecyclerView的排列方式
     */
    fun setLayoutManager(layout: RecyclerView.LayoutManager) {
        recyclerView.layoutManager = layout
    }
    
    /**
     * 在setAdapter之后调用
     * 设置没数据的View，View可实现[EmptyViewAllState]来维护View状态，设置此View后要自行处理所有【点击】事件
     * 没设置此View，会默认设置一个EmptyView
     */
    fun setEmptyView(emptyView: View) {
        val baseAdapter = baseAdapter
        baseAdapter?.setEmptyView(emptyView)
    }
    
    /**
     * 设置默认EmptyView（即没设置EmptyView）的【SuccessNoData】的View
     */
    fun setDefaultEmptyViewSuccessNoDataView(successNoDataView: View) {
        mDefaultEmptyViewSuccessNoDataView = successNoDataView
    }
    
    /**
     * 设置默认EmptyView（即没设置EmptyView）的【failed】的View，View可实现[EmptyViewFailedState]来维护View状态
     */
    fun setDefaultEmptyViewFailedView(failedView: View) {
        mDefaultEmptyViewFailedView = failedView
    }
    
    /**
     * 刷新
     */
    fun refresh() {
        smartRefreshLayout.autoRefresh()
    }
    
    /**
     * RecyclerView增加头
     */
    fun addHeaderView(header: View) {
        this.baseAdapter?.addHeaderView(header)
    }
    
    fun removeHeaderView(header: View) {
        this.baseAdapter?.removeHeaderView(header)
    }
    
    fun removeAllHeaderView() {
        this.baseAdapter?.removeAllHeaderView()
    }
    
    /**
     * RecyclerView增加尾
     */
    fun addFooterView(footer: View) {
        this.baseAdapter?.addFooterView(footer)
    }
    
    fun removeFooterView(footer: View) {
        this.baseAdapter?.removeFooterView(footer)
    }
    
    fun removeAllFooterView() {
        this.baseAdapter?.removeAllFooterView()
    }
    
    /**
     *  设置头和尾和空布局是否同时显示
     *  [isHeadAndEmpty]为true同时显示头和空，[isFootAndEmpty]为true同时显示尾和空
     */
    fun setHeaderFooterEmpty(isHeadAndEmpty: Boolean, isFootAndEmpty: Boolean) {
        // 默认设置头和尾和空布局如果都有的话都显示，在Adapter通知之前调用
        this.baseAdapter?.headerWithEmptyEnable = isHeadAndEmpty
        this.baseAdapter?.footerWithEmptyEnable = isFootAndEmpty
    }
    
    /**
     *  设置是否使用EmptyView，在setAdapter之后调用
     */
    fun isUseEmpty(isUseEmpty: Boolean) {
        this.baseAdapter?.isUseEmpty = isUseEmpty// 设置是否用空布局
    }
    
    /**
     * 设置xRecyclerView加载是否可用
     */
    fun addItemDecoration(decor: RecyclerView.ItemDecoration) {
        recyclerView.addItemDecoration(decor)
    }
    
    /**
     * 刷新完成
     */
    fun refreshComplete() {
        smartRefreshLayout.finishRefresh(0)
    }
    
    /**
     * 加载完成
     */
    fun loadMoreComplete() {
        smartRefreshLayout.finishLoadMore(0)
    }
    
    fun refreshAndLoadMoreComplete() {
        refreshComplete()
        loadMoreComplete()
    }
    
    fun setRefreshFooterDefault() {
        // 设置是否在全部加载结束之后Footer跟随内容
        smartRefreshLayout.setEnableFooterFollowWhenNoMoreData(true)
        val classicsFooter = ClassicsFooter(mContext)
        ClassicsFooter.REFRESH_FOOTER_NOTHING = "一 · 已到底部，没有了哦 · 一"
        classicsFooter.setTextSizeTitle(13f)
        classicsFooter.setAccentColor(Color.WHITE)
        smartRefreshLayout.setRefreshFooter(classicsFooter)
    }
    
    /**
     * 设置无网络图片的点击监听
     */
    fun setOnNoNetViewClickListener(onNoNetViewClickListener: OnClickListener) {
        this.onNoNetViewClickListener = onNoNetViewClickListener
    }
    
    // 解决魅族手机上的提示的view id重名冲突
    override fun onRestoreInstanceState(state: Parcelable) {
        try {
            super.onRestoreInstanceState(state)
        } catch (ignored: Exception) {
        
        }
    }
}
