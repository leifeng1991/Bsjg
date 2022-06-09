package com.scorpio.bsjg.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.moufans.lib_base.ext.BaseExtKt;
import com.moufans.lib_base.request.rx.OnRequestListener;
import com.moufans.lib_base.utils.DeviceUtils;
import com.moufans.lib_base.utils.InitUtils;
import com.moufans.lib_base.utils.LogUtil;
import com.moufans.update.OnAppUpdateListener;
import com.moufans.update.VersionDataBean;
import com.moufans.update.update.DownAPKService;
import com.moufans.update.update.UpdateVersionDialog;
import com.scorpio.bsjg.ext.AppExtKt;

import java.util.List;


/**
 * 简介: 版本更新的工具类
 */
public class AppVersionUpdateUtils {
    private static final String TAG = "AppVersionUpdateUtils";
    public static final int REQUEST_CODE_START_DOWNLOAD_READ_WRITE = 10012;

    /**
     * 请求版本信息
     */
    public static void checkVersion(Activity context, OnAppUpdateListener onAppUpdateListener) {
        if (context == null) return;
        String version = "V" + DeviceUtils.getVersionName(InitUtils.getApplication());
        BaseExtKt.convertExecute(AppExtKt.getAppApi().checkVersion(version), new OnRequestListener<VersionDataBean>(null) {

            @Override
            public void onSuccess(VersionDataBean list) {
                VersionDataBean bean = list;
                // 0:不需要更新 1：强制更新 2：更新
                // 是否强制更新（0/否，1/是）
                String status = bean.getStatus();
                if (!version.equals(bean.getNewversion())) {
//                    "1".equals(status) || "2".equals(status)
                    bean.setStatus("1".equals(status) ? "1" : "2");
                    // 更新
                    if (onAppUpdateListener != null) {
                        onAppUpdateListener.onSuccess(bean);
                    }
                } else {
                    if (onAppUpdateListener != null)
                        onAppUpdateListener.onFailed("已是最新版本");
                }
            }

            @Override
            public void onFailed(boolean isResultError, int status, @NonNull String message) {

            }
        });

    }

    /**
     * 匹配版本信息
     *
     * @param context
     * @param data
     * @return
     */
    public static UpdateVersionDialog upDataVersion(Activity context, VersionDataBean data, UpdateVersionDialog.UpdateVersionListener listener) {

        if (context == null) return null;
//        int versionCode = AppUtil.getVersionCode(context);
//        int serviceVersionCode = Integer.parseInt(data.getVersionCode());
        String downloadUrl = data.getUrl();
        UpdateVersionDialog updateVersionDialog = new UpdateVersionDialog(context, data.getTitle(), data.getContent(), downloadUrl, "1".equals(data.getStatus()));
        updateVersionDialog.setOnUpdateNickListener(listener);
        if ("1".equals(data.getStatus()) || "2".equals(data.getStatus())) {
            if (!TextUtils.isEmpty(downloadUrl)) {
                LogUtil.e(TAG, "下载地址==" + downloadUrl);
                updateVersionDialog.show();
            } else {
                LogUtil.e("下载地址为空----------------------");
            }
        } else {
        }

        return updateVersionDialog;
    }

    /**
     * 下载更新
     *
     * @param context
     * @param downloadUrl
     */
    public static void toDownload(Activity context, String downloadUrl) {
        if (context == null || TextUtils.isEmpty(downloadUrl)) return;
        XXPermissions xp = XXPermissions.with(context);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            xp.permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            xp.permission(Permission.Group.STORAGE);
        } else {
            xp.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        xp.request((permissions, all) -> {
            LogUtil.i("---------DownAPKService-------------");
            Intent intent = new Intent(context, DownAPKService.class);
            intent.putExtra("downUrl", downloadUrl);
            context.startService(intent);
        });

    }


}
