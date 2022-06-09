package com.scorpio.bsjg.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.scorpio.bsjg.R


/**
 * Common弹框
 */
object CommonDialogUtil {

    fun showRawDialogNoButton(activity: Activity, message: String) {
        AlertDialog.Builder(activity)
            .setTitle("提示")
            .setMessage(message)
            .show()
    }

    /**
     * 通用模板弹框
     *
     * @param activity
     * @param title         标题
     * @param content       内容
     * @param clickListener 左中右按钮点击事件
     * @return
     */
    @JvmStatic
    fun commonGeneralDialog(activity: Activity, title: CharSequence?, content: CharSequence?, clickListener: OnButtonClickListener?, isShowClose:Boolean,vararg btnTexts: CharSequence?): Dialog {
        val loadDataView = View.inflate(activity, R.layout.common_dialog_general, null)
        // 标题
        val mDialogTitleTextView = loadDataView.findViewById<TextView>(R.id.mDialogTitleTextView)


        val mDialogTitleLayout = loadDataView.findViewById<LinearLayout>(R.id.mDialogTitleLayout)
        if (TextUtils.isEmpty(title)) {
            mDialogTitleLayout.visibility = View.GONE
        } else {
            mDialogTitleLayout.visibility = View.VISIBLE
            mDialogTitleTextView.text = title
        }
        // 内容
        val mDialogContentTextView = loadDataView.findViewById<TextView>(R.id.mDialogContentTextView)
        if (TextUtils.isEmpty(content)) {
            mDialogContentTextView.visibility = View.GONE
        } else {
            mDialogContentTextView.visibility = View.VISIBLE
            mDialogContentTextView.text = content
        }
        // 左 中 右按钮
        val mTwoButtonGroup = loadDataView.findViewById<Group>(R.id.mTwoButtonGroup)
        val mDialogLeftTextView = loadDataView.findViewById<TextView>(R.id.mDialogLeftTextView)
        val mDialogRightTextView = loadDataView.findViewById<TextView>(R.id.mDialogRightTextView)
        val mDialogCenterLineView = loadDataView.findViewById<TextView>(R.id.mDialogCenterTextView)
        // 控制按钮的显示
        when {
            btnTexts.size > 1 -> {
                // 左右两个按钮
                mTwoButtonGroup.visibility = View.VISIBLE
                mDialogLeftTextView.visibility = View.VISIBLE
                mDialogRightTextView.visibility = View.VISIBLE
                mDialogCenterLineView.visibility = View.GONE
                mDialogLeftTextView.text = btnTexts[0]
                mDialogRightTextView.text = btnTexts[1]
            }
            btnTexts.isNotEmpty() -> {
                // 中间按钮
                mTwoButtonGroup.visibility = View.GONE
                mDialogLeftTextView.visibility = View.GONE
                mDialogRightTextView.visibility = View.GONE
                mDialogCenterLineView.visibility = View.VISIBLE
                mDialogCenterLineView.text = btnTexts[0]
            }
            else -> {
                mTwoButtonGroup.visibility = View.GONE
                mDialogCenterLineView.visibility = View.GONE
            }
        }

        val dialogBuilder = DialogBuilder
            .create(activity)
            .setView(loadDataView)
            .setWidthScale(0.72)
            .show()
        loadDataView.findViewById<ImageView>(R.id.mCloseImageView).apply {
            visibility = if (isShowClose) View.VISIBLE else View.GONE
            setOnClickListener {
                dialogBuilder.dismiss()
            }
        }
        // 左侧按钮点击事件
        mDialogLeftTextView.setOnClickListener {
            dialogBuilder.dismiss()
            clickListener?.onLeftButtonClick(dialogBuilder)
        }
        // 右侧按钮点击事件
        mDialogRightTextView.setOnClickListener {
            dialogBuilder.dismiss()
            clickListener?.onRightOrCenterButtonClick(dialogBuilder)
        }
        // 中间按钮点击事件
        mDialogCenterLineView.setOnClickListener {
            dialogBuilder.dismiss()
            clickListener?.onRightOrCenterButtonClick(dialogBuilder)
        }
        dialogBuilder.setCancelable(true)
        return dialogBuilder.dialog
    }

    interface OnButtonClickListener {
        fun onLeftButtonClick(dialog: DialogBuilder)

        fun onRightOrCenterButtonClick(dialog: DialogBuilder)
    }

}
