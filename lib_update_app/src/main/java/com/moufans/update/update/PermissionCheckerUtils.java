package com.moufans.update.update;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;


import com.moufans.update.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限检查工具类
 */
public class PermissionCheckerUtils {
    public static boolean checkPermissions(final Activity activity, String[] permissions
            , final int requestCode, final int dialogMsgForRationale) {
        return checkPermissions(activity, R.string.dialog_imagepicker_permission_nerver_ask_cancel, R.string.dialog_imagepicker_permission_confirm,
                permissions, requestCode, dialogMsgForRationale, false);
    }

    /**
     * 检查权限的方法
     *
     * @param activity              发起检查的Activity
     * @param permissions           权限组
     * @param requestCode           请求Code
     * @param dialogMsgForRationale 为权限用途作解释的Dialog内容
     * @return 是否有权限，没有权限时会发起请求权限
     */
    public static boolean checkPermissions(final Activity activity, int leftStr, int rightStr, String[] permissions
            , final int requestCode, final int dialogMsgForRationale, boolean activityFinish) {
        //Android6.0以下默认有权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        final List<String> needList = new ArrayList<>();
        boolean needShowRationale = false;
        int length = permissions.length;

        for (int i = 0; i < length; i++) {
            String permisson = permissions[i];
            if (ActivityCompat.checkSelfPermission(activity, permisson) != PackageManager.PERMISSION_GRANTED) {
                needList.add(permisson);
            }
        }

        return needList.size() == 0;
    }


}