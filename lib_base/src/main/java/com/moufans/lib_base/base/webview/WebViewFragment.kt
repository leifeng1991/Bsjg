package com.moufans.lib_base.base.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.moufans.lib_base.R
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.base.fragment.ReuseViewFragment
import com.moufans.lib_base.databinding.BaseFragmentWebviewBinding
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.listener.ScrollBoundaryDecider
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.HashMap


/**
 * Fragment的webView
 */
class WebViewFragment : ReuseViewFragment<BaseFragmentWebviewBinding>() {
    private var animationDrawable: AnimationDrawable? = null
    private var isError: Boolean = false
    private var mWebView: WebView? = null
    var onWebViewListener: OnWebViewListener? = null
    
    /** 视频全屏参数  */
    private var customView: View? = null
    private var fullscreenContainer: FrameLayout? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    
    override fun getLayoutId(): Int {
        return R.layout.base_fragment_webview
    }
    
    override fun initView() {
        mFragmentDataBinding.mWebViewSmartRefreshLayout.setEnableRefresh(false)
        // 初始化控件
        mWebView = mFragmentDataBinding.mWebViewWv
        // 加载进度动画
        animationDrawable = mFragmentDataBinding.mLoadDataLayout.mLoadingIV.background as AnimationDrawable?
        // 初始化刷新控件
        mFragmentDataBinding.apply {
            mWebViewSmartRefreshLayout.setRefreshHeader(ClassicsHeader(context))
            mWebViewSmartRefreshLayout.setEnableLoadMore(false)
            mWebViewSmartRefreshLayout.setScrollBoundaryDecider(object : ScrollBoundaryDecider {
                override fun canRefresh(content: View?): Boolean {
                    return if (mWebView == null) false else mWebView!!.scrollY <= 0
                }
                
                override fun canLoadMore(content: View?): Boolean {
                    return false
                }
            })
        }
    }
    
    override fun initListener() {
        mFragmentDataBinding.mErrorEmptyView.onRefreshBtnClickListener = View.OnClickListener {
            mWebView?.reload()// 会重新调用WebViewClient的生命周期
        }
        // 设置刷新监听
        mFragmentDataBinding.mWebViewSmartRefreshLayout.setOnRefreshListener {
            reload()
            mFragmentDataBinding.mWebViewSmartRefreshLayout.finishRefresh()
        }
    }
    
    override fun processingLogic() {
        // 初始化数据
        initData()
    }
    
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onResume() {
        super.onResume()
        mWebView?.onResume()
//        mWebView?.resumeTimers()
    }
    
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onPause() {
        super.onPause()
        mWebView?.onPause()
//        mWebView?.pauseTimers()// 它会暂停所有webView的layout，parsing，javaScriptTimer，降低CPU功耗。这个方法不仅仅针对当前的webView而是全局的全应用程序的webView
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mWebView?.clearHistory()
        mWebView?.clearCache(true)
        mWebView?.destroy()
    }
    
    /**
     * 加载 url
     */
    fun loadUrl(urlString: String) {
        LogUtil.i(TAG, urlString)
        mWebView?.loadUrl(urlString)
    }
    
    /**
     * 内嵌网页向上返回一页，如果返回到头，则会销毁此activity页面
     */
    fun goBackUntilFinishActivity() {
        when {
            customView != null -> hideCustomView()// 兼容视频全屏播放
            canGoBack() -> goBack()// 能返回，返回上一个页面
            else -> activity?.finish()// 不能返回，则销毁此页面
        }
    }
    
    /**
     * 设置滚动到顶部
     */
    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun setScrollTop() {
        mWebView?.scrollY = 0
    }
    
    /**
     * 内部页面返回上一级
     */
    fun goBack() {
        mWebView?.goBack()
    }
    
    /**
     * 内部页面是否有上一级，是否返回到头
     */
    fun canGoBack(): Boolean {
        return mWebView?.canGoBack() ?: false
    }
    
    
    /**
     * 返回到顶部地址
     */
    fun goTopUrl() {
        while (canGoBack()) {
            goBack()
        }
    }
    
    /**
     * 重新加载
     */
    fun reload() {
        mWebView?.reload()
    }
    
    /**
     * 调用js方法
     */
    fun callJavascriptMethod(method: String) {
        mWebView?.post {
            LogUtil.e("callJavascriptMethod==$method")
            mWebView?.loadUrl("javascript:$method")
        }
    }
    
    fun getWebView() = mWebView
    
    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface", "ObsoleteSdkInt")
    private fun initData() {
        if (mWebView == null) return
        // 网址
        val rawUrl = arguments?.getString(INTENT_PARAM_WEB_URL) ?: ""
//        mWebView!!.loadUrl(rawUrl, getAdditionalHttpHeaders())// 添加请求头
        mWebView!!.loadUrl(rawUrl)
        LogUtil.i(TAG, rawUrl)
        // webView的设置
        mWebView!!.isHorizontalScrollBarEnabled = false
        mWebView!!.isVerticalScrollBarEnabled = false
        mWebView!!.webChromeClient = MyWebChromeClient()
        mWebView!!.webViewClient = MyWebViewClient()
        // js交互
//        mWebView!!.addJavascriptInterface(WebAppJavascriptInterface(), "obj")
        mWebView!!.addJavascriptInterface(WebAppJavascriptInterface(), "mBridge")
        
        // webView settings设置
        val webSettings = mWebView!!.settings
        webSettings.javaScriptEnabled = true
        // 设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true // 将图片调整到适合webView的大小
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            webSettings.loadWithOverviewMode = true
        }//  缩放至屏幕的大小
        // 缩放操作
        webSettings.setSupportZoom(false) // 不支持缩放
//        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
//        webSettings.displayZoomControls = false //隐藏原生的缩放控件
        // 其他细节操作
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            webSettings.allowFileAccess = true
        } // 允许访问文件
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 不加载缓存内容
        webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSettings.defaultTextEncodingName = "utf-8"//设置编码格式
        // 设置缓存存储
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            webSettings.domStorageEnabled = true
        } // 开启 DOM storage API 功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            webSettings.databaseEnabled = false
        }   //关闭 database storage API 功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            webSettings.setAppCacheEnabled(false)
        }//关闭 Application Caches 功能
        //缓存模式如下：
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
        //不使用缓存:
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 设置WebView是否通过手势触发播放媒体，默认是true，需要手势触发。
            webSettings.mediaPlaybackRequiresUserGesture = false
        }
        // 在安卓5.0之前默认允许其加载混合网络协议内容，在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webView允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        // 设置网家家通用userAgent，用来标识来源为App内部Web
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            webSettings.userAgentString = "syt"
        }
        // 开始显示网页，执行动画
        onWebStart()
    }
    
    internal inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress >= 80) onWebEnd(view)// TODO 频率太高会影响回调，但是目前多设置几次没有太大影响，应及时修改
        }
        
        /*** 视频播放相关的方法 **/
        override fun getDefaultVideoPoster(): Bitmap? {
            return BitmapFactory.decodeResource(resources, R.mipmap.base_bg_transparent)
        }
        
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            showCustomView(view, callback)
        }
        
        override fun onHideCustomView() {
            hideCustomView()
            // 解决html跳到视频页面返回后没监听到回调，视频没销毁
            mWebView?.post { mWebView?.loadUrl("javascript:exitFullscreen()") }
        }
        
        // TODO 选择文件未适配低版本和部分机型
        // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
        fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String, capture: String) {
            LogUtil.e("fffff", "openFileChooser 3")
        }
        
        // file upload callback (Android 5.0 (API level 21) -- current) (public method)
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            LogUtil.e("fffff", "onShowFileChooser")
//            mPermissionHelper.requestPermissions("请授予[相机][读写]权限，否则无法拍照和选择文件", object : PermissionHelper.PermissionListener {
//                override fun doAfterGrand(vararg permission: String) {
//                    // 请求权限成功
//                    showFileChooser()
//                }
//
//                override fun doAfterDenied(vararg permission: String) {
//                    // 设置失败
//                }
//            }, android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            return true
        }
    }
    
    internal inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onWebStart()
        }
        
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)// 已过时，之后用进度判断，待研究
            onWebError()
        }

//        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//            super.onReceivedError(view, request, error)// 会监听js错误等网页内部错误，频率太高，不监测网页内部错误
//            onWebError()
//        }

//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//            onWebEnd(view)
//        }
        
        @RequiresApi(Build.VERSION_CODES.DONUT)
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("ftp:")) {
                // 网址，内部浏览
                LogUtil.i(TAG, "shouldOverrideUrlLoading=loadUrl=$url")
                view.loadUrl(url)
            } else {
                // scheme，打开[url]指定页面
                try {
                    LogUtil.i(TAG, "shouldOverrideUrlLoading=startActivity=$url")
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    intent.component = null
                    startActivity(intent)
                } catch (e: Exception) {
                    LogUtil.e(TAG, "shouldOverrideUrlLoading=startActivity=Exception=${e.message}")
                }
            }
            return true
        }
    }
    
    internal inner class WebAppJavascriptInterface {
        /**
         * 销毁此页面
         */
        @JavascriptInterface
        fun finish() {
            mWebView?.post { goBackUntilFinishActivity() }
        }
        
        @JavascriptInterface
        fun destroy() {
            mWebView?.post { activity?.finish() }
        }
        
        @JvmOverloads
        @JavascriptInterface
        fun invoke(api: String, params: String? = null, callback: String? = null) {
            LogUtil.i(TAG,"===========$api==========$params==========$callback")
            if (P_GO_BACK == api) {
                mWebView?.post { goBackUntilFinishActivity() }
            } else if (P_SETUP_NAVI == api) {
                LogUtil.i(TAG,"=======###################################")
                lifecycleScope.launch {
                    if (!TextUtils.isEmpty(params)){
                        val obj = JSONObject(params)
                        val isHidden: Int = obj.optInt("isHidden")
                        val statusBarColor: Int = obj.optInt("statusBarColor")
                        val backgroundColor: Int = obj.optInt("backgroundColor")
                        val title: String = obj.optString("title")
                        if(activity is BaseActivity<*>){
                            ( activity as BaseActivity<*>).apply {
                                setHeaderViewVisibleByWebView(isHidden == 2)
                                setTitle(title)
                            }
                        }
                    }
                }
                
                callback?.let {
                    val backNavi: MutableMap<String, Any> = HashMap()
                    backNavi["naviHeight"] = StatusBarUtil.getStatusBarHeight(requireContext())
                    backNavi["scale"] = requireContext().resources.displayMetrics.density
                    callJavascriptMethod("$callback(${Gson().toJson(backNavi)})")
                }
            }
        }
    }
    
    private fun onWebStart() {
        showView(isShowLoadView = true, isShowWebView = true, isShowErrorView = false)
        isError = false
    }
    
    private fun onWebError() {
        isError = true
    }
    
    private fun onWebEnd(view: WebView?) {
        if (isError) {
            // 来自错误页面，显示错误布局
            showView(isShowLoadView = false, isShowWebView = false, isShowErrorView = true)
        } else {
            // 来自成功页面，显示webView
            showView(isShowLoadView = false, isShowWebView = true, isShowErrorView = false)
            // 设置页面加载完成的监听
            onWebViewListener?.onPageFinished(canGoBack(), view?.title ?: "", view?.url ?: "")
        }
    }
    
    // 是否展示WebView
    private fun showView(isShowLoadView: Boolean, isShowWebView: Boolean, isShowErrorView: Boolean) {
        if (isShowLoadView) {
            mFragmentDataBinding.mLoadDataLayout.mRootLayout.visibility = View.GONE
            animationDrawable?.start()
        } else {
            mFragmentDataBinding.mLoadDataLayout.mRootLayout.visibility = View.INVISIBLE
            animationDrawable?.stop()
        }
        
        mWebView?.visibility = if (isShowWebView) View.VISIBLE else View.INVISIBLE
        mFragmentDataBinding.mErrorEmptyView.visibility = if (isShowErrorView) View.VISIBLE else View.INVISIBLE
    }
    
    /** 视频播放全屏  */
    private fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
        // 如果视图已经存在，则立即终止新视图
        if (customView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
                callback.onCustomViewHidden()
            }
            return
        }
        // 切换屏幕
        switchScreen(requireActivity())
        // 给Activity的decorView增加一层全屏播放的布局
        val content = requireActivity().findViewById<FrameLayout>(android.R.id.content)
        fullscreenContainer = FullscreenHolder(requireContext())// 全屏容器界面
        fullscreenContainer!!.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))// 全屏界面添加视频界面
        content.addView(fullscreenContainer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))// activity添加全屏界面
        // 保存状态
        customView = view
        customViewCallback = callback
    }
    
    /** 隐藏视频全屏  */
    @RequiresApi(Build.VERSION_CODES.ECLAIR_MR1)
    private fun hideCustomView() {
        if (customView == null) {
            return
        }
        // 切换屏幕
        switchScreen(requireActivity())
        // 移除全屏视频界面
        val content = requireActivity().findViewById<FrameLayout>(android.R.id.content)
        content.removeView(fullscreenContainer)
        // 通知隐藏
        customViewCallback?.onCustomViewHidden()
        // 清空状态
        fullscreenContainer = null
        customView = null
    }
    
    /** 全屏容器界面  */
    internal class FullscreenHolder(context: Context) : FrameLayout(context) {
        
        init {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.black))// 背景黑色
        }
        
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(evt: MotionEvent): Boolean {
            return true
        }
    }
    
    fun switchScreen(activity: Activity) {
        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏，切换为横屏
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            // 横屏，切换为竖屏
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    
    interface OnWebViewListener {
        fun onPageFinished(isCanGoBack: Boolean, title: String, url: String)
    }
    
    companion object {
        private const val TAG = "WebView"
        private const val INTENT_PARAM_WEB_URL = "webUrl"
        private const val P_GO_BACK = "goBack"
        private const val P_SETUP_NAVI = "setupNavi"
        
        /**
         * 获取给WebViewFragment 传值的Bundle
         */
        private fun newBundle(webUrl: String): Bundle {
            val bundle = Bundle()
            bundle.putString(INTENT_PARAM_WEB_URL, webUrl)
            return bundle
        }
        
        /**
         * 获取给WebViewFragment 传值的Bundle
         * @param webUrl 网址
         */
        fun newInstance(webUrl: String): WebViewFragment {
            val webViewFragment = WebViewFragment()
            webViewFragment.arguments = newBundle(webUrl)
            return webViewFragment
        }
    }
    
    
}
