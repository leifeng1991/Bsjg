package com.moufans.lib_base.base.webview

import android.content.Context
import android.content.Intent
import com.moufans.lib_base.R
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.databinding.BaseActivityWebviewBinding


/**
 * 网页跳转
 */
class WebViewActivity : BaseActivity<BaseActivityWebviewBinding>(), WebViewFragment.OnWebViewListener {
    private var mWebViewFragment: WebViewFragment? = null
    private var mEnterAnim: Int = 0
    private var mExitAnim: Int = 0
    private val mTitleName by lazy { intent.getStringExtra(INTENT_TITLE_NAME) }
    
    override fun getDataBindingLayoutResId(): Int {
        // 设置动画
        mEnterAnim = intent.getIntExtra("enterAnim", -1)
        mExitAnim = intent.getIntExtra("exitAnim", -1)
        if (mEnterAnim != -1 && mExitAnim != -1)
            overridePendingTransition(mEnterAnim, mExitAnim)
        return R.layout.base_activity_webview
    }
    
    override fun initView() {
        // 标题头名字
        setHeaderTitle(mTitleName)
        // 网址
        val mWebUrl = intent.getStringExtra(INTENT_WEB_URL) ?: ""
        // 设置内容，传递网址
        mWebViewFragment = WebViewFragment.newInstance(mWebUrl)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mWebView, mWebViewFragment!!)// 将fragment设置到布局上
        fragmentTransaction.commitAllowingStateLoss()
    }
    
    override fun initListener() {
        // 设置webView的监听
        mWebViewFragment!!.onWebViewListener = this
    }
    
    override fun processingLogic() {
    
    }
    
    override fun finish() {
        super.finish()
        if (mEnterAnim != -1 && mExitAnim != -1)
            overridePendingTransition(mEnterAnim, mExitAnim)
    }
    
    override fun onBackPressed() {
        mWebViewFragment!!.goBackUntilFinishActivity()
    }
    
    override fun onPageFinished(isCanGoBack: Boolean, title: String, url: String) {
        // 设置title
        setHeaderTitle(title)
    }
    
    companion object {
        
        private const val INTENT_WEB_URL = "webUrl"
        private const val INTENT_TITLE_NAME = "titleName"
        private const val INTENT_IS_SHOW_TITLE = "isShowTitle"
        private const val INTENT_ENTER_ANIM = "enterAnim"
        private const val INTENT_EXIT_ANIM = "exitAnim"
        
        /**
         * @param webUrl      加载的url地址
         * @param titleName   标题头名字，为null则用H5的Title
         * @param isShowTitle 是否展示标题头
         * @param enterAnim   WebViewActivity的进入动画
         * @param exitAnim    WebViewActivity的退出动画
         */
        @JvmOverloads
        @JvmStatic
        fun newIntent(context: Context, webUrl: String, titleName: String? = null, isShowTitle: Boolean = true, enterAnim: Int = -1, exitAnim: Int = -1): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(INTENT_WEB_URL, webUrl)
                putExtra(INTENT_TITLE_NAME, titleName)
                putExtra(INTENT_IS_SHOW_TITLE, isShowTitle)
                putExtra(INTENT_ENTER_ANIM, enterAnim)
                putExtra(INTENT_EXIT_ANIM, exitAnim)
            }
        }
    }
}
