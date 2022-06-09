package com.moufans.lib_base.base.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.core.view.MotionEventCompat;

/**
 * 描述: 主要解决viewPager嵌套webView横向滚动问题
 * 来源：https://www.jianshu.com/p/d4212df4823f
 * @author zhangrq
 * createTime 2020/3/29 9:51
 */
public class ExtendedWebView extends WebView {
    private boolean isScrollX = false;

    public ExtendedWebView(Context context) {
        super(context);
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEventCompat.getPointerCount(event) == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isScrollX = false;
                    //事件由webview处理
                    getParent().getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //嵌套Viewpager时
                    getParent().getParent().requestDisallowInterceptTouchEvent(!isScrollX);
                    break;
                default:
                    getParent().getParent().requestDisallowInterceptTouchEvent(false);
            }
        } else {
            //使webview可以双指缩放（前提是webview必须开启缩放功能，并且加载的网页也支持缩放）
            getParent().getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(event);
    }

    //当webview滚动到边界时执行
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        isScrollX = clampedX;
    }
}
