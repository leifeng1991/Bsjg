package com.moufans.update.down;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;


import com.moufans.lib_base.utils.LogUtil;
import com.moufans.update.R;
import com.moufans.update.down.badger.BadgeUtil;

import java.io.File;

public class NotificationUtils {

    String Tag = "安装错误异常";
    NotificationManager mNotificationManager = null;
    Notification.Builder builder;
    int mNotificationID = 0x10000;
    //记录消息发送的时间
    long notificationTime = 0;
    private static NotificationUtils instance;

    public static NotificationUtils getInstance() {
        if (instance == null) {
            instance = new NotificationUtils();
        }
        return instance;
    }

    /**
     * 不带数字角标
     *
     * @param mContext
     * @param NotificationID id
     * @param title          标题
     * @param content        内容
     */
    public void initNotification(Context mContext, int NotificationID, String title, String content) {
        mNotificationID = NotificationID;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(mNotificationID + "",
                    title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.FLAG_ONLY_ALERT_ONCE);
            mNotificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(mContext, mNotificationID + ""); //与channelId对应
        } else {
            builder = new Notification.Builder(mContext); //与channelId对应
            if (Build.VERSION.SDK_INT > 16) {
                builder.setShowWhen(true);
            }

        }
        builder.setSmallIcon(com.moufans.lib_base.R.mipmap.ic_launcher);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setWhen(System.currentTimeMillis());//设置通知时间，一般设置的是收到通知时的System.currentTimeMillis()
        builder.setNumber(0);
        //设置 只能代码调用取消
        builder.setOngoing(true);
        builder.setProgress(100, 0, false);
        builder.setOnlyAlertOnce(true);


    }

    /**
     * 设置普通的 notification(设置角标)
     *
     * @param mContext
     * @param NotificationID id
     * @param title          标题
     * @param content        内容
     * @param mclass         跳转的页面
     */
    public void initOrdinaryNotification(Context mContext, int NotificationID, int number, String title, String content, String targetId, String chatType, String specialGroupData, Class mclass) {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder mBulider = null;
        if (mNotificationManager == null) return;
        if (null == title || "".equals(title)) {
            title = "新消息";
        }
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                if (NotificationID == -1) this.mNotificationID = NotificationID;
                if (TextUtils.isEmpty(title)) title = "千里科技";
                NotificationChannel channel = new NotificationChannel(mNotificationID + "",
                        title, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setLightColor(Color.GREEN);
                channel.enableLights(true);
                channel.setLockscreenVisibility(Notification.FLAG_ONLY_ALERT_ONCE);

                mNotificationManager.createNotificationChannel(channel);
                mBulider = new Notification.Builder(mContext, mNotificationID + "");
                mBulider.setShowWhen(true);
            } catch (Exception e) {
                Log.e("推送适配8.0异常*************:", e.getMessage());
            }
        } else {
            mBulider = new Notification.Builder(mContext); //与channelId对应
            if (Build.VERSION.SDK_INT > 16) {
                mBulider.setShowWhen(true);
            }
        }
        Intent intent = new Intent(mContext, mclass);
        if (null != targetId && !"".equals(targetId)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("targetId", targetId);
            intent.putExtra("chatType", chatType);
            intent.putExtra("conversationName", title);
            intent.putExtra("specialGroupData", specialGroupData);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        PendingIntent pi = PendingIntent.getActivities(mContext, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
        //处理 多个通知同时触发的状况 一秒内通知不触发 设置声音的操作
        if (System.currentTimeMillis() - notificationTime > 1000)
            mBulider.setDefaults(Notification.DEFAULT_SOUND);
        else
            mBulider.setDefaults(Notification.DEFAULT_VIBRATE);

        mBulider.setSmallIcon(com.moufans.lib_base.R.mipmap.ic_launcher)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())//设置通知时间，一般设置的是收到通知时的System.currentTimeMillis()
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi);
        Notification notification = mBulider.build();    //创建通知栏对象，显示通知信息
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        BadgeUtil.sendBadgeNotification(notification, NotificationID, mContext, number, 0);  //设置角标

        notificationTime = System.currentTimeMillis();
    }

    /**
     * Notification 更新进度
     *
     * @param progress
     */
    public void upProgress(Context context, int progress) {
        // 这里是处理 Notification 频繁更新卡顿问题.(部分手机可以开线程 小部分手机不兼容 华为p8 不兼容)
        if (progress % 5 == 0) {
            builder.setProgress(100, progress, false);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null)
                mNotificationManager.notify(mNotificationID, builder.build());
        }
    }

    /**
     * 代码取消 Notification
     *
     * @param context
     */
    public void cancleNotification(Context context) {
        if (context == null) return;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) mNotificationManager.cancel(mNotificationID);
        if (builder != null) builder = null;
    }

    public void cancleAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /**
     * 更新完成安装apk
     *
     * @param mContext
     * @param content
     * @param apkFile
     */
    public void updateSucess(Context mContext, String content, File apkFile) {
        LogUtil.e("=================" + content + apkFile.getAbsolutePath());
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setAction(Intent.ACTION_VIEW);
        installIntent.addCategory(Intent.CATEGORY_DEFAULT);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            //判读版本是否在7.0以上
            String authority = mContext.getApplicationInfo().packageName + ".fileProvider";
            LogUtil.e("=================>=1" + authority);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            installIntent.setDataAndType(FileProvider.getUriForFile(mContext, authority, apkFile), "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        LogUtil.e("=================>=2");
        //隐式意图拉起安装器
        PendingIntent mPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPendingIntent = PendingIntent.getActivity(mContext, 0, installIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPendingIntent = PendingIntent.getActivity(mContext, 0, installIntent, 0);
        }
        builder.setContentText(content);
        builder.setContentIntent(mPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(mNotificationID, builder.build());
        mContext.startActivity(installIntent);// 下载完成之后自动弹出安装界面
        cancleNotification(mContext);
    }

}