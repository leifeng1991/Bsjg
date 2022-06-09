package com.moufans.lib_base.base.activity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ViewDataBinding
import com.moufans.lib_base.R
import com.moufans.lib_base.base.BaseViewModel
import com.moufans.lib_base.request.BaseView
import com.moufans.lib_base.base.recyclerview.EmptyView
import com.moufans.lib_base.databinding.BaseActivityBaseBinding
import com.moufans.lib_base.databinding.BaseLayoutHeaderBarBinding
import com.moufans.lib_base.ext.setOnClickListener2
import com.moufans.lib_base.request.rx.FAILED_STATUS_CODE_NO_NET
import com.moufans.lib_base.request.rx.FAILED_STATUS_NO_DATA
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.moufans.lib_base.weight.ProgressLoading

/**
 * 描述:Activity基础类
 */
abstract class BaseActivity<DB : ViewDataBinding> : AppCompatActivity(), BaseView {
    // 通过dataBinding直接使用控件
    lateinit var mDataBinding: DB
        protected set

    // 控制标题栏和内容区
    private lateinit var mComActivityBaseBinding: BaseActivityBaseBinding

    // loading款是否初始化 true已经初始化 false未初始化
    private var mLoadingDialogIsInit = false

    // 加载框
    private val mLoadingDialog: ProgressLoading? by lazy {
        mLoadingDialogIsInit = true
        if (isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)) null
        else ProgressLoading.newInstance(this)
    }

    // 默认布局
    private val mDefaultEmptyView by lazy {
        EmptyView(this).apply {
            onRefreshBtnClickListener = View.OnClickListener { onRetry() }
        }
    }

    // 标题栏
    var mHeaderBarDataBinding: BaseLayoutHeaderBarBinding? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mComActivityBaseBinding = setContentView(this, R.layout.base_activity_base)
        mDataBinding = DataBindingUtil.bind(LayoutInflater.from(this@BaseActivity).inflate(getDataBindingLayoutResId(), null))!!
        init()
    }

    private fun init() {
        setStatusBar()
        addHeaderView()
        addContentRootView()
        initView()
        initListener()
        processingLogic()
    }

    /**
     * 设置状态栏相关
     */
    open fun setStatusBar() {
        StatusBarUtil.setTranslucent(this)
        StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
        StatusBarUtil.setLightMode(this)
    }

    /**
     * 添加头
     */
    open fun addHeaderView() {
        mHeaderBarDataBinding = DataBindingUtil.bind(LayoutInflater.from(this@BaseActivity).inflate(R.layout.base_layout_header_bar, null))
        setHeaderLeftImage()
        if (mHeaderBarDataBinding?.root != null) mComActivityBaseBinding.rlHeaderBar.addView(mHeaderBarDataBinding!!.root)
    }

    /**
     * 只适用于WebView交互
     */
    open fun setHeaderViewVisibleByWebView(isVisible: Boolean) {
        if (isVisible) {
            mHeaderBarDataBinding = DataBindingUtil.bind(LayoutInflater.from(this@BaseActivity).inflate(R.layout.base_layout_header_bar, null))
            setHeaderLeftImage()
            if (mHeaderBarDataBinding?.root != null) {
                mComActivityBaseBinding.rlHeaderBar.visibility = View.VISIBLE
                mHeaderBarDataBinding!!.root.setPadding(0, StatusBarUtil.getStatusBarHeight(this), 0, 0)
                mComActivityBaseBinding.rlHeaderBar.addView(mHeaderBarDataBinding!!.root)
            }
        } else {
            if (mHeaderBarDataBinding?.root != null) {
                mComActivityBaseBinding.rlHeaderBar.setPadding(0, 0, 0, 0)
                mComActivityBaseBinding.rlHeaderBar.visibility = View.GONE
                mComActivityBaseBinding.rlHeaderBar.removeAllViews()
            }
        }
    }

    /**
     * 添加内容
     */
    open fun addContentRootView() {
        mComActivityBaseBinding.flContent.addView(mDataBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLoadingDialogIsInit) mLoadingDialog?.gc()
    }

    inline fun <reified VM : BaseViewModel> initViewModel(isHandlerError: Boolean = true, isHandlerSuccess: Boolean = true): Lazy<VM> {
        val vm = viewModels<VM>()
        vm.value.mFailedCode.observe(this, {
            if (isHandlerError)
                handlerError(it)
        })
        vm.value.mSuccessDataBean.observe(this, {
            if (isHandlerSuccess)
                handlerSuccess(it)
        })
        return vm
    }

    /**
     * 展示加载框
     */
    override fun showLoading() {
        if (isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)) return
        mLoadingDialog?.showLoading()
    }

    /**
     * 隐藏加载框
     */
    override fun hideLoading() {
        mLoadingDialog?.hideLoading()
    }

    override fun onError(status: Int, message: String) {
        ToastUtil.showShort(message)
    }

    fun getHeaderViewParentView(): View {
        return mComActivityBaseBinding.rlHeaderBar
    }

    fun getEmptyView(): EmptyView {
        return mDefaultEmptyView
    }

    /**
     * 重置标题栏
     */
    fun resetDefaultHeaderView(headerView: View) {
        mComActivityBaseBinding.rlHeaderBar.removeAllViews()
        mComActivityBaseBinding.rlHeaderBar.addView(headerView)
    }

    /**
     * 显示隐藏标题栏
     */
    fun setVisibleHeaderView(visible: Int) {
        mComActivityBaseBinding.rlHeaderBar.visibility = visible
    }


    /**
     * 设置左侧按钮图片和监听事件
     *
     * @param imgRes          imgRes
     * @param onClickListener onClickListener
     */
    fun setHeaderLeftImage(imgRes: Int? = null, onClickListener: View.OnClickListener? = null) {
        mHeaderBarDataBinding?.apply {
            mLeftIv.setImageResource(imgRes ?: R.mipmap.base_ic_left_back)
            if (onClickListener == null) {
                mLeftIv.setOnClickListener2 {
                    onBackPressed()
                }
            } else {
                mLeftIv.setOnClickListener(onClickListener)
            }
            mLeftIv.visibility = View.VISIBLE
        }
    }

    /**
     * 用来设置页面标题
     */
    fun setHeaderTitle(title: CharSequence?) {
        mHeaderBarDataBinding?.apply {
            mTitleTv.text = title
            mTitleTv.visibility = if (TextUtils.isEmpty(title)) View.GONE else View.VISIBLE
        }
    }

    /**
     * 设置右侧文字
     */
    fun setHeaderRightText(rightText: CharSequence?, rightTextColor: Int? = null, rightTextSize: Float? = null, onClickListener: View.OnClickListener? = null) {
        mHeaderBarDataBinding?.apply {
            rightText?.apply {
                mRightTv.text = this
            }
            rightTextColor?.apply {
                mRightTv.setTextColor(this)
            }
            rightTextSize?.apply {
                // 单位dp
                mRightTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, this)
            }
            mRightTv.setOnClickListener2 {
                onClickListener?.onClick(it)
            }
            mRightTv.visibility = if (TextUtils.isEmpty(rightText)) View.GONE else View.VISIBLE
        }
    }


    /**
     * 设置右侧左图片和监听事件
     *
     * @param imgRes          imgRes
     * @param onClickListener onClickListener
     */
    fun setHeaderRightLeftImage(imgRes: Int, onClickListener: View.OnClickListener? = null) {
        mHeaderBarDataBinding?.apply {
            mRightLeftImageView.setImageResource(imgRes)
            mRightLeftImageView.setOnClickListener2 {
                onClickListener?.onClick(it)
            }
            mRightLeftImageView.visibility = View.VISIBLE
        }
    }

    /**
     * 设置右侧右图片和监听事件
     *
     * @param imgRes          imgRes
     * @param onClickListener onClickListener
     */
    fun setHeaderRightRightImage(imgRes: Int, onClickListener: View.OnClickListener? = null) {
        mHeaderBarDataBinding?.apply {
            mRightRightImageView.setImageResource(imgRes)
            mRightRightImageView.setOnClickListener2 {
                onClickListener?.onClick(it)
            }
            mRightRightImageView.visibility = View.VISIBLE
        }
    }

    /**
     * 数据请求成功可以调用
     */
    fun <T> handlerSuccess(t: T) {
        mComActivityBaseBinding.flContent.removeAllViews()
        if (t == null) {
            mComActivityBaseBinding.flContent.addView(mDefaultEmptyView)
            mDefaultEmptyView.onSuccessNoData()
        } else {
            mComActivityBaseBinding.flContent.addView(mDataBinding.root)
        }
    }


    /**
     * 数据请求失败调用
     */
    fun handlerError(failedCode: Int) {
        mComActivityBaseBinding.flContent.removeAllViews()
        mComActivityBaseBinding.flContent.addView(mDefaultEmptyView)
        when (failedCode) {
            FAILED_STATUS_CODE_NO_NET -> {
                mDefaultEmptyView.onFailedNoNet()
            }
            FAILED_STATUS_NO_DATA -> {
                mDefaultEmptyView.onSuccessNoData()
            }
            else -> {
                mDefaultEmptyView.onFailedOther()
            }
        }
    }

    // 获取布局id
    abstract fun getDataBindingLayoutResId(): Int

    // 初始化view
    abstract fun initView()

    // 初始化监听
    abstract fun initListener()

    // 处理业务逻辑
    abstract fun processingLogic()

    // 重新加载操作
    open fun onRetry() {}

}