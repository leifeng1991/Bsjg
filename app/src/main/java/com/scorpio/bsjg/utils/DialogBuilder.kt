package com.scorpio.bsjg.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog

/**
 * 描述:Dialog的常用功能
 */

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DialogBuilder {

    /**
     * 获取dialog
     */
    val dialog: Dialog

    /**
     * 存入的obj
     */
    var obj: Any? = null

    /**
     * 获取屏幕宽度并设置
     */
    private val screenWidth: Int
        get() {
            val wm = dialog.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            return dm.widthPixels
        }

    /**
     * dialog 是否展示
     */
    val isShowing: Boolean
        get() = dialog.isShowing

    /**
     * dialog 获取window
     */
    val window: Window?
        get() = dialog.window

    /**
     * dialog 构造方法
     */
    constructor(activity: Activity) {
        dialog = AppCompatDialog(activity)
    }

    /**
     * dialog 构造方法
     */
    constructor(activity: Activity, themeResId: Int) {
        dialog = AppCompatDialog(activity, themeResId)
    }

    /**
     * 设置dialog view
     */
    fun setView(layoutResID: Int): DialogBuilder {
        dialog.setContentView(layoutResID)
        return this
    }

    /**
     * 设置dialog view
     */
    fun setView(view: View): DialogBuilder {
        dialog.setContentView(view)
        return this
    }

    /**
     * 设置dialog view
     */
    fun setView(view: View, params: ViewGroup.LayoutParams): DialogBuilder {
        dialog.setContentView(view, params)
        return this
    }

    /**
     * 展示dialog
     */
    fun show(): DialogBuilder {
        dialog.show()
        return this
    }

    /**
     * 隐藏dialog
     */
    fun hide(): DialogBuilder {
        dialog.hide()
        return this
    }

    /**
     * 销毁dialog
     */
    fun dismiss(): DialogBuilder {
        dialog.dismiss()
        return this
    }

    /**
     * 取消dialog
     */
    fun cancel(): DialogBuilder {
        dialog.cancel()
        return this
    }

    /**
     * 设置dialog的动画样式
     *
     * @param styleId 动画样式
     */
    fun setAnimations(styleId: Int): DialogBuilder {
        window?.setWindowAnimations(styleId)
        return this
    }

    /**
     * 设置dialog的重力
     *
     * @param gravity 设置dialog的重力 例如：Gravity.BOTTOM
     */
    fun setGravity(gravity: Int): DialogBuilder {
        window?.apply {
            attributes.gravity = gravity // 设置重力
            this.attributes = attributes
        }
        return this
    }


    /**
     * 设置dialog的宽度
     *
     * @param scaleScreenWidth 相对于屏幕宽度的比例
     */
    fun setWidthScale(scaleScreenWidth: Double): DialogBuilder {
        return setWidthAndHeight((screenWidth * scaleScreenWidth).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    /**
     * 设置dialog的宽、高
     *
     * @param width  dialog的宽
     * @param height dialog的高
     */
    fun setWidthAndHeight(width: Int, height: Int): DialogBuilder {
        window?.apply {
            setBackgroundDrawable(null)//必须设置dialog背景,因为默认的dialog样式给设置了距离周围有距离的.9图片
            setLayout(width, height)
        }
        return this
    }


    /**
     * 设置dialog是否可取消
     */
    fun setCancelable(flag: Boolean): DialogBuilder {
        dialog.setCancelable(flag)
        return this
    }

    /**
     * 设置dialog点击外部区域是否可取消
     */
    fun setCanceledOnTouchOutside(cancel: Boolean): DialogBuilder {
        dialog.setCanceledOnTouchOutside(cancel)
        return this
    }

    /**
     * dialog弹出后显示软键盘
     */
    fun showSoftInput(): DialogBuilder {
        return setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * dialog设置软键盘模式
     */
    fun setSoftInputMode(mode: Int): DialogBuilder {
        window?.setSoftInputMode(mode)
        return this
    }


    companion object {

        fun create(activity: Activity): DialogBuilder {
            return DialogBuilder(activity)
        }

        fun create(activity: Activity, themeResId: Int): DialogBuilder {
            return DialogBuilder(activity, themeResId)
        }
    }
}
