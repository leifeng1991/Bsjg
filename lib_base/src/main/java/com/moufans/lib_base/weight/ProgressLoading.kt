package com.moufans.lib_base.weight

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.Gravity
import android.view.View
import com.moufans.lib_base.R

/**
 * 描述:加载框
 */
class ProgressLoading private constructor(context: Context, themeResId: Int) : Dialog(context, themeResId) {

    companion object {
        fun newInstance(activity: Activity): ProgressLoading {
            return ProgressLoading(activity, R.style.BaseLightProgressDialog)
        }
    }

    private var animationDrawable: AnimationDrawable? = null

    init {
        setContentView(R.layout.base_progress_dialog)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        window?.attributes?.gravity = Gravity.CENTER
        // 设置灰暗程度
        val lp = window?.attributes
        lp?.dimAmount = 0.2f
        window?.attributes = lp
        // 获取AnimationDrawable
        val loadingView = findViewById<View>(R.id.mLoadingIV)
        animationDrawable = loadingView.background as AnimationDrawable?
    }

    fun showLoading() {
        if (!isShowing) super.show()
        animationDrawable?.apply {
            if (!this.isRunning) start()
        }
    }

    fun hideLoading() {
        if (isShowing) super.dismiss()
        animationDrawable?.apply {
            if (this.isRunning) stop()
        }
    }

    fun gc() {
        if (isShowing) super.dismiss()
        animationDrawable?.apply {
            if (this.isRunning) stop()
            animationDrawable = null
        }
    }
}