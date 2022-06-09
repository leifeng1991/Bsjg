package com.scorpio.bsjg.app

import com.moufans.lib_base.base.BaseApplication
import com.scorpio.bsjg.utils.SharedPrefUtil

class MyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        SharedPrefUtil.init(this)
    }
}