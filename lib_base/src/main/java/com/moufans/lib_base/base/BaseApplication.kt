package com.moufans.lib_base.base

import androidx.multidex.MultiDexApplication
import com.moufans.lib_base.utils.InitUtils

open class BaseApplication : MultiDexApplication() {
    
    override fun onCreate() {
        super.onCreate()
        InitUtils.init(this)
    }
}