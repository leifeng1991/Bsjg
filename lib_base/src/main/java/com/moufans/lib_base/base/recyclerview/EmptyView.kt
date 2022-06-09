package com.moufans.lib_base.base.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.moufans.lib_base.R

/**
 * 描述:通用空布局
 *
 * 2019/4/30 13:14
 */
class EmptyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), EmptyViewAllState {
    var onRefreshBtnClickListener: OnClickListener? = null
    private var mFailedView: View? = null
    private lateinit var mEmptyViewRefreshBtnTv: View
    private lateinit var mEmptyViewRootView: NestedScrollView
    private lateinit var mEmptyViewSuccessNoDataLayoutFl: FrameLayout
    private lateinit var mEmptyViewFailedLayoutLl: LinearLayout
    private lateinit var mEmptyViewStateHintTv: TextView
    
    init {
        init(context)
    }
    
    private fun init(context: Context) {
        val rootView = View.inflate(context, R.layout.base_layout_empty_view, null)
        mEmptyViewRefreshBtnTv = rootView.findViewById(R.id.mEmptyViewRefreshBtnTv)
        mEmptyViewRootView = rootView.findViewById(R.id.mEmptyViewRootView)
        mEmptyViewSuccessNoDataLayoutFl = rootView.findViewById(R.id.mEmptyViewSuccessNoDataLayoutFl)
        mEmptyViewFailedLayoutLl = rootView.findViewById(R.id.mEmptyViewFailedLayoutLl)
        mEmptyViewStateHintTv = rootView.findViewById(R.id.mEmptyViewStateHintTv)
        // addView
        addView(rootView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        // initView
        mEmptyViewRefreshBtnTv.setOnClickListener {
            onRefreshBtnClickListener?.onClick(it)
        }
        // 常用于adapter中，所以默认设置为false
        setViewIsNestedScrollingEnabled(false)
    }
    
    fun setViewIsNestedScrollingEnabled(isNestedScrollingEnabled: Boolean) {
        mEmptyViewRootView?.isNestedScrollingEnabled = isNestedScrollingEnabled
    }
    
    /**
     * 成功没数据
     */
    override fun onSuccessNoData() {
        if (mEmptyViewSuccessNoDataLayoutFl.childCount > 0) {
            // 有无数据的View，设置显示此View
            mEmptyViewSuccessNoDataLayoutFl.visibility = View.VISIBLE// 成功没数据布局
            mEmptyViewFailedLayoutLl.visibility = View.GONE// 失败布局
        } else {
            // 没有无数据的View，设置【mEmptyViewFailedLayoutLl】显示并提示
            mEmptyViewSuccessNoDataLayoutFl.visibility = View.GONE// 成功没数据布局
            mEmptyViewFailedLayoutLl.visibility = View.VISIBLE// 失败布局
            mEmptyViewStateHintTv.text = "暂无数据哦～"// 失败提示
        }
    }
    
    override fun onFailedNoNet() {
        setFailedLayout(true)
    }
    
    override fun onFailedOther() {
        setFailedLayout(false)
    }
    
    /**
     * 设置失败布局
     */
    private fun setFailedLayout(isNoNet: Boolean) {
        // 显示失败布局，提示无网络
        mEmptyViewSuccessNoDataLayoutFl.visibility = View.GONE// 成功没数据布局
        mEmptyViewFailedLayoutLl.visibility = View.VISIBLE// 失败布局
        if (mFailedView != null) {
            // 设置了失败布局，通知失败布局状态
            if (mFailedView is EmptyViewFailedState) {
                // 通知
                if (isNoNet) (mFailedView as EmptyViewFailedState).onFailedNoNet() else (mFailedView as EmptyViewFailedState).onFailedOther()
            }
        } else {
            // 未设置失败布局，使用默认布局
            mEmptyViewStateHintTv.text = if (isNoNet) "无网络～" else "数据错误"// 失败提示
        }
    }
    
    /**
     * 设置成功没数据的View
     */
    fun setSuccessNoDataView(successNoDataView: View) {
        mEmptyViewSuccessNoDataLayoutFl.removeAllViews()
        mEmptyViewSuccessNoDataLayoutFl.addView(successNoDataView)
    }
    
    /**
     * 设置失败的View
     */
    fun setFailedView(failedView: View) {
        mFailedView = failedView
        mEmptyViewFailedLayoutLl.removeAllViews()
        mEmptyViewFailedLayoutLl.addView(failedView)
    }
    
    object Helper {
        @JvmStatic
        fun setEmptyViewIsShow(emptyView: View, contentView: View, isShowEmptyView: Boolean) {
            emptyView.visibility = if (isShowEmptyView) View.VISIBLE else View.GONE
            contentView.visibility = if (isShowEmptyView) View.GONE else View.VISIBLE
        }
    }
}
