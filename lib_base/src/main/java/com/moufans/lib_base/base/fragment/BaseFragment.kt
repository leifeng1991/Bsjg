package com.moufans.lib_base.base.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.request.BaseView
import com.moufans.lib_base.utils.ToastUtil
import com.moufans.lib_base.weight.ProgressLoading
import com.trello.rxlifecycle4.components.support.RxFragment


/**
 * 描述:fragment基础类
 */
abstract class BaseFragment<DB : ViewDataBinding> : RxFragment(), BaseView, View.OnClickListener {
    lateinit var mFragmentDataBinding: DB
    private val mLoadingDialog: ProgressLoading? by lazy {
        if (activity == null || requireActivity().isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && requireActivity().isDestroyed))
            null
        else
            ProgressLoading.newInstance(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentDataBinding = DataBindingUtil.bind(inflater.inflate(getLayoutId(), container, false))!!
        return mFragmentDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        processingLogic()
    }

    override fun showLoading() {
        if (activity == null || (requireActivity().isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && requireActivity().isDestroyed))) return // 页面销毁，不展示弹框
        if (activity is BaseActivity<*>)
            (activity as BaseActivity<*>).showLoading()
        else {
            mLoadingDialog?.showLoading()
        }
    }

    override fun hideLoading() {
        if (activity != null && activity is BaseActivity<*>)
            (activity as BaseActivity<*>).hideLoading()
        else
            mLoadingDialog?.hideLoading()
    }

    override fun onError(status: Int, message: String) {
        if (activity != null && activity is BaseActivity<*>)
            (activity as BaseActivity<*>).onError(status, message)
        else
            ToastUtil.showShort(message)
    }

    override fun onClick(v: View?) {

    }

    protected abstract fun getLayoutId(): Int
    protected abstract fun initView()
    protected abstract fun initListener()
    protected abstract fun processingLogic()
}