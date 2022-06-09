package com.moufans.lib_base.base.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager


/**
 * 描述:弹框基类
 */
abstract class BaseDialogFragment<DB : ViewDataBinding> : DialogFragment() {
    lateinit var mFragmentDataBinding: DB
    private var mOnDismissListener: DialogInterface.OnDismissListener? = null
    private var isFirst = true// 是否是第一次执行
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, 0)// 统一设置STYLE_NO_TITLE，解决低版本弹框顶部有条线
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            isFirst = true
            mFragmentDataBinding = DataBindingUtil.bind(inflater.inflate(getLayoutId(),container,false))!!
            rootView = mFragmentDataBinding.root
        } else {
            isFirst = false
            val parent = rootView!!.parent
            if (parent != null && parent is ViewGroup) {
                parent.removeView(rootView)
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // BaseFragment的Fragment生命周期方法都要覆盖，在此判断是否不执行其生命周期
        // 第一次执行，走生命周期，否则不走生命周期，解决 initView()、initListener()、processingLogic()重复调用
        // 不能用rootView==null判断，因为rootView始终不等于null
        if (isFirst){
            super.onViewCreated(view, savedInstanceState)
            initView()
            initListener()
            processingLogic()
        }
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        mOnDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mOnDismissListener?.onDismiss(dialog)
    }

    override fun show(manager: FragmentManager, tag: String?) {
//        super.show(manager, tag)
        // 因为在Activity调用onSaveInstanceState后，再给它调用commit添加Fragment就会出错，所以改成调用commitAllowingStateLoss添加
        // mDismissed = false
        val mClass = DialogFragment::class.java
        val dismissed = mClass.getDeclaredField("mDismissed")
        dismissed.isAccessible = true
        dismissed.set(this, false)

        // mShownByMe = true
        val shownByMe = mClass.getDeclaredField("mShownByMe")
        shownByMe.isAccessible = true
        shownByMe.set(this, true)

        // commit
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initListener()
    abstract fun processingLogic()
}