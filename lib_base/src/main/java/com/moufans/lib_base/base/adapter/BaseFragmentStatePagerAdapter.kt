package com.moufans.lib_base.base.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * 描述:适用于页面比较多，销毁以前的Fragment以释放内存
 */
open class BaseFragmentStatePagerAdapter(fm: FragmentManager, private var titleList: List<String>, private var fragmentList: List<Fragment>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size ?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position < titleList.size) titleList[position] else null
    }

    override fun finishUpdate(container: ViewGroup) {
        try {
            super.finishUpdate(container)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

}
