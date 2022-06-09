package com.scorpio.bsjg.utils

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.moufans.lib_base.utils.ToastUtil

import java.util.regex.Pattern

/**
 * 检查内容
 *
 * @author zhangrq
 */
object CheckUtils {
    /**
     * 正则表达式：验证手机号
     */
    private const val REGEX_MOBILE = "1\\d{10}"

    /**
     * 正则表达式：验证纯数字
     */
    const val REGEX_NUM = "\\d+"

    /**
     * 正则表达式：验证身份证号
     */
    private const val REGEX_ID_CARD = "\\d{17}(\\d|x|X)"

    /**
     * 正则表达式：验证密码
     */
    private const val PASS_WORD = "[a-zA-Z0-9]{6,12}"

    private val context: Context?
        get() = null

    /**
     * 检查手机号
     */

    fun checkPhoneNumber(phoneNumber: String): Boolean {

        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtil.showShort("手机号不能为空")
            return false
        } else if (!Pattern.matches(REGEX_MOBILE, phoneNumber)) {
            ToastUtil.showShort("请输入正确手机号")
            return false
        }
        return true
    }

    /**
     * 检查注册密码
     */
    fun checkPassWord(password: String): Boolean {

        if (TextUtils.isEmpty(password)) {
            ToastUtil.showShort("密码不能为空")
            return false
        } else if (!Pattern.matches(PASS_WORD, password)) {
            ToastUtil.showShort("密码为6-12位，数字和字母组合，不可含有符号")
            return false
        }
        return true
    }

    /**
     * 检查手机号和密码
     */
    fun checkPhoneNumberAndPassword(phoneNumber: String, password: String): Boolean {
        if (!checkPhoneNumber(phoneNumber)) {
            // 手机号有问题
            return false
        } else if (!checkPassWord(password)) {
            return false
        }
        return true
    }

    /**
     * 检查身份证号
     *
     * @param idCard 身份证号
     */
    private fun checkIdCard(idCard: String): Boolean {
        if (TextUtils.isEmpty(idCard)) {
            ToastUtil.showShort("请输入正确身份证号")
            return false
        } else if (!Pattern.matches(REGEX_ID_CARD, idCard)) {
            ToastUtil.showShort("请输入正确身份证号")
            return false
        }
        return true
    }

    /**
     * 检查验证码
     *
     * @param verCode 验证码
     */
    fun checkInput(verCode: String): Boolean {
        // 检测密码
        if (TextUtils.isEmpty(verCode)) {
            ToastUtil.showShort("验证码不能为空")
            return false
        }
        return true
    }

    /**
     * 检查手机号、验证码
     *
     * @param phoneNumber 手机号
     * @param verCode     验证码
     */
    fun checkInput(phoneNumber: String, verCode: String): Boolean {
        // 检测密码
        if (!checkPhoneNumber(phoneNumber)) {
            // 手机号有问题
            return false
        } else if (!checkInput(verCode)) {
            // 验证码有问题
            return false
        }
        return true
    }

    /**
     * 检查手机号、验证码、邀请码
     *
     * @param phoneNumber      手机号
     * @param verificationCode 验证码
     * @param inviteCode       邀请码
     */
    fun checkInput(phoneNumber: String, verificationCode: String, inviteCode: String): Boolean {
        if (!checkInput(phoneNumber, verificationCode)) {
            // 手机号、验证码有问题
            return false
        } else if (TextUtils.isEmpty(inviteCode)) {
            ToastUtil.showShort("邀请码输入错误")
            return false
        }
        return true
    }

    /**
     * 检查身份证、验证码
     *
     * @param idNumber 身份证
     * @param verCode  验证码
     */
    fun checkInputs(idNumber: String, verCode: String): Boolean {
        // 检测密码
        if (!checkIdCard(idNumber)) {
            // 身份证号有问题
            return false
        } else if (!checkInput(verCode)) {
            // 验证码有问题
            return false
        }
        return true
    }

    fun checkInputReason(reason: String): Boolean {
        if (TextUtils.isEmpty(reason)) {
            ToastUtil.showShort("请输入申请退款原因")
            return false
        }
        return true
    }


    /**
     * 设置值，并手机号设置高亮
     */
    fun setTextAndCheckPhoneHighLight(textView: TextView, rawContent: String, listener: OnPhoneClickListener) {
        if (TextUtils.isEmpty(rawContent)) {
            return
        }
        // 加载文章内容高亮多个关键字，只高亮一个关键字去掉循环
        val spannableString = SpannableString(rawContent)
        val p = Pattern.compile(REGEX_MOBILE)
        val m = p.matcher(spannableString)
        while (m.find()) {
            val start = m.start()
            val end = m.end()
            spannableString.setSpan(PhoneClickableSpan(rawContent.substring(start, end), listener), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    /**
     * 检测Data
     */
    fun checkData(data: Any?): Boolean {
        if (data == null) {
            ToastUtil.showShort("未获取到数据，请检查网络或稍后再试")
            return false
        }
        return true
    }

    class PhoneClickableSpan(private val phoneNum: String, private val listener: OnPhoneClickListener?) : ClickableSpan() {

        override fun onClick(widget: View) {
            listener?.onPhoneClick(phoneNum)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = Color.parseColor("#4976ff")
            ds.isUnderlineText = true
        }
    }

    interface OnPhoneClickListener {
        fun onPhoneClick(phoneNum: String)
    }
}
