package com.moufans.lib_base.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

/**
 * @author deadline
 * android >=M 的权限申请统一处理
 * notice:
 * 很多手机对原生系统做了修改，比如小米4的6.0的shouldShowRequestPermissionRationale
 * 就一直返回false，而且在申请权限时，如果用户选择了拒绝，则不会再弹出申请权限对话框了, 因此有了
 * void doAfterDenied(String... permission);
 */

@RequiresApi(Build.VERSION_CODES.HONEYCOMB) @Suppress("unused")
class PermissionHelper(private val mContext: Any) {
    
    private var mListener: PermissionListener? = null
    
    private var mPermissionList: List<String>? = null
    
    init {
        checkCallingObjectSuitability(mContext)
    }
    
    
    /**
     * 权限授权申请
     *
     * @param hintMessage 要申请的权限的提示
     * @param listener    申请成功之后的callback
     * @param permissions 要申请的权限
     */
    fun requestPermissions(hintMessage: CharSequence, listener: PermissionListener?, vararg permissions: String) {
        if (listener != null) {
            mListener = listener
        }
        mPermissionList = listOf(*permissions)
        if (!hasPermissions(mContext, *permissions)) {
            //没全部权限
            //需要向用户解释为什么申请这个权限
            var shouldShowRationale = false
            for (perm in permissions) {
                shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(mContext, perm)
            }
            if (shouldShowRationale) {
                // 展示提示
                showMessageOKCancel(hintMessage, "确定", { _, _ -> executePermissionsRequest(mContext, permissions, REQUEST_PERMISSION_CODE) }, { _, _ ->
                    mListener?.doAfterDenied(*permissions)
                })
            } else {
                // 不展示提示
                executePermissionsRequest(mContext, permissions, REQUEST_PERMISSION_CODE)
            }
        } else { //有全部权限
            mListener?.doAfterGrand(*permissions)
        }
    }
    
    /**
     * 处理onRequestPermissionsResult
     */
    @Suppress("UNUSED_PARAMETER")
    fun handleRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                var allGranted = true// 所有的授予
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        // 有一个没被授予的就为false
                        allGranted = false
                        break
                    }
                }
                
                if (allGranted && mListener != null) {
                    // 所有的权限授予以后
                    mListener!!.doAfterGrand(*mPermissionList!!.toTypedArray())
                } else if (!allGranted && mListener != null) {
                    // 所有的权限有一个没授予以后
                    mListener!!.doAfterDenied(*mPermissionList!!.toTypedArray())
                    // 为了解决小米手机等，申请一次，拒绝后不再提示申请，所以去设置页面自己设置
                    showMessageOKCancel("当前应用缺少必要权限。\n\n请点击\"设置\"-\"权限\"-打开所需权限。", "去设置", { _, _ ->
                        startAppSettings(getActivity(mContext)!!)
                    }, { _, _ -> })
                }
            }
        }
    }
    
    /**
     * 请求权限,
     * 兼容fragment
     */
    @TargetApi(23)
    @Suppress("DEPRECATION")
    private fun executePermissionsRequest(any: Any, perms: Array<out String>, requestCode: Int) {
        when (any) {
            is Activity -> ActivityCompat.requestPermissions(any, perms, requestCode)
            is Fragment -> any.requestPermissions(perms, requestCode)
            is android.app.Fragment -> any.requestPermissions(perms, requestCode)
        }
    }
    
    /**
     * 检查传递Context是否合法
     */
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @Suppress("DEPRECATION")
    private fun checkCallingObjectSuitability(any: Any?) {
        if (any == null) {
            throw NullPointerException("Activity or Fragment should not be null")
        }
        
        val isActivity = any is Activity
        val isSupportFragment = any is Fragment
        val isAppFragment = any is android.app.Fragment
        if (!(isSupportFragment || isActivity || isAppFragment && isNeedRequest)) {
            if (isAppFragment) {
                throw IllegalArgumentException("target sdk needs to be greater than 23 if caller is android.app.Fragment")
            } else {
                throw IllegalArgumentException("Caller must be an Activity or a Fragment.")
            }
        }
    }
    
    private fun showMessageOKCancel(message: CharSequence, okStr: String, okListener: DialogInterface.OnClickListener, cancelListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(getActivity(mContext)!!).setMessage(message).setPositiveButton(okStr, okListener).setNegativeButton("取消", cancelListener).setCancelable(false).create().show()
    }
    
    interface PermissionListener {
        /**
         * 所有的权限授予之后
         *
         * @param permission 所有的权限
         */
        fun doAfterGrand(vararg permission: String)
        
        /**
         * 所有的权限有一个没授予以后
         *
         * @param permission 所有的权限
         */
        fun doAfterDenied(vararg permission: String)
    }
    
    companion object {
        
        private const val REQUEST_PERMISSION_CODE = 1000
        
        /**
         * 判断是否具有某权限，规则：小于6.0具有，如果有个没有则返回false
         */
        private fun hasPermissions(any: Any, vararg perms: String): Boolean {
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }
            
            for (perm in perms) {
                val hasPerm = ContextCompat.checkSelfPermission(getActivity(any)!!, perm) == PackageManager.PERMISSION_GRANTED
                if (!hasPerm) {
                    return false
                }
            }
            return true
        }
        
        
        /**
         * 将要展示请求权限说明
         * 兼容fragment
         */
        @TargetApi(23)
        @Suppress("DEPRECATION")
        private fun shouldShowRequestPermissionRationale(any: Any, perm: String): Boolean {
            return if (any is Activity) {
                ActivityCompat.shouldShowRequestPermissionRationale(any, perm)
            } else (any as? android.app.Fragment)?.shouldShowRequestPermissionRationale(perm) ?: ((any as? Fragment)?.shouldShowRequestPermissionRationale(perm) ?: false)
        }
        
        /**
         * 获取activity
         */
        @Suppress("DEPRECATION")
        private fun getActivity(any: Any): Activity? {
            return any as? Activity ?: when (any) {
                is Fragment -> any.activity
                is android.app.Fragment -> any.activity
                else -> null
            }
        }
        
        /**
         * 是否需要请求权限
         */
        private val isNeedRequest: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        
        // 启动应用的设置
        fun startAppSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
        }
    }
}