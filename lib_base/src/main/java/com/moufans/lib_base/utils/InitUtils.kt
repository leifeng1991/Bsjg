package com.moufans.lib_base.utils

import android.annotation.SuppressLint
import android.app.Application
import java.lang.reflect.InvocationTargetException


/**
 * 描述:初始化工具类
 */
object InitUtils {
    private var mApplication: Application? = null
    
    
    @JvmStatic
    fun init(context: Application) {
        mApplication = context
    }
    
    @JvmStatic
    fun getApplication(): Application {
        if (mApplication == null)
            mApplication = getApplicationByReflect()
        return mApplication!!
    }
    
    private fun getApplicationByReflect(): Application {
        try {
            @SuppressLint("PrivateApi")
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
                    ?: throw NullPointerException("u should init first")
            return app as Application
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        throw NullPointerException("u should init first")
    }
}