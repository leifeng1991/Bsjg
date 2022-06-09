package com.moufans.lib_base.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup

/**
 * 用于修复全屏状态下adjustResize不生效的问题,当弹出输入法时重新设定内容view的高度,使输入框正常显示
 */
class FullscreenInputWorkaround private constructor(private val activity: Activity, private val mChildOfContent: View, private val inputShowListener: InputShowListener?) {
    private var usableHeightPrevious: Int = 0
    private val layoutParams: ViewGroup.LayoutParams

    init {
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
        layoutParams = mChildOfContent.layoutParams
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height

            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                layoutParams.height = usableHeightSansKeyboard - heightDifference
                inputShowListener?.inputShow(true)
            } else {
                // keyboard probably just became hidden
                layoutParams.height = usableHeightSansKeyboard - StatusBarUtil.getStatusBarHeight(activity)// 减去导航栏的高度为新增
                inputShowListener?.inputShow(false)
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    @SuppressLint("NewApi")
    private fun computeUsableHeight(): Int {
        val frame = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(frame)
        val statusBarHeight = frame.top

        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)

        //这个判断是为了解决19之后的版本在弹出软键盘时，键盘和推上去的布局（adjustResize）之间有黑色区域的问题
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            r.bottom - r.top + statusBarHeight
        } else r.bottom - r.top

    }

    interface InputShowListener {
        fun inputShow(show: Boolean)
    }

    companion object {

        // For more information, see https://code.google.com/p/android/issues/detail?id=5497
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
        private val TAG = "AndroidBug5497Workaroun"

        fun assistActivity(activity: Activity, contentView: View, inputShowListener: InputShowListener? = null): FullscreenInputWorkaround {
            return FullscreenInputWorkaround(activity, contentView, inputShowListener)
        }
    }
}
