package com.moufans.lib_base.base.webview

import android.content.Context
import android.content.Intent
import com.moufans.lib_base.R
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.databinding.BaseActivityWebviewFullscreenBinding
import com.moufans.lib_base.utils.FullscreenInputWorkaround
import com.moufans.lib_base.utils.StatusBarUtil


/**
 * 全屏网页跳转
 */
class FullScreenWebViewActivity : BaseActivity<BaseActivityWebviewFullscreenBinding>(), WebViewFragment.OnWebViewListener {
    private var webViewFragment: WebViewFragment? = null
    
    override fun getDataBindingLayoutResId(): Int {
        return R.layout.base_activity_webview_fullscreen
    }
    
    override fun addHeaderView() {
    
    }
    
    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }
    
    override fun initView() {
//        FullscreenInputWorkaround.assistActivity(this, mDataBinding.mFullscreenWebViewFl)// 兼容软键盘弹起
        // 网址
        val webUrl = intent.getStringExtra(WEB_URL)
        // 设置内容，传递网址
        webViewFragment = WebViewFragment.newInstance(webUrl ?: "")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mWebView, webViewFragment!!)// 将fragment设置到布局上
        fragmentTransaction.commitAllowingStateLoss()
    }
    
    override fun initListener() {
        // 设置webView的监听
        webViewFragment!!.onWebViewListener = this
    }
    
    override fun processingLogic() {
    
    }
    
    override fun onBackPressed() {
        webViewFragment!!.goBackUntilFinishActivity()
    }
    
    override fun onPageFinished(isCanGoBack: Boolean, title: String, url: String) {
    
    }
    
    companion object {
        
        private const val WEB_URL = "webUrl"
        
        /**
         * @param webUrl      加载的url地址
         */
        @JvmStatic
        fun newIntent(context: Context, webUrl: String): Intent {
            val intent = Intent(context, FullScreenWebViewActivity::class.java)
            intent.putExtra(WEB_URL, webUrl)
            return intent
        }
    }
    
}
