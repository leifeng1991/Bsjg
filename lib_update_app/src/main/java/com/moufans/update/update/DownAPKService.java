package com.moufans.update.update;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import com.moufans.lib_base.utils.DeviceUtils;
import com.moufans.lib_base.utils.LogUtil;
import com.moufans.lib_base.utils.ToastUtil;
import com.moufans.update.down.DownloadUtil;
import com.moufans.update.down.NotificationUtils;
import com.moufans.update.down.OnDownloadListener;
import com.moufans.update.event.InstallApkEvent;
import com.moufans.update.event.ProgressEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class DownAPKService extends Service {
    private static final String TAG = "DownAPKService";
    private final int NotificationID = 0x10000;
    private boolean isDown;
    // 文件下载路径
    private String downUrl = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("downUrl")) {
            downUrl = intent.getStringExtra("downUrl");
            if (!TextUtils.isEmpty(downUrl) && !isDown)
                DownFile(downUrl);
            else
                Log.e("下载----------------", "下载地址为空");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void DownFile(String file_url) {
        LogUtil.i(TAG, "=======开始下载====" + file_url);
        NotificationUtils.getInstance().initNotification(getApplicationContext(), NotificationID, "千里科技", "正在更新 千里科技");
        DownloadUtil.get().downloadApk(file_url, "current", "trinidad_technology_" + DeviceUtils.getVersionName(getApplicationContext()), new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
//                EventBus.getDefault().post(new InstallApkEvent(file.getPath()));
                NotificationUtils.getInstance().updateSucess(getApplicationContext(), "下载完成", file);
                isDown = false;
            }

            @Override
            public void onDownloading(int progress) {
                EventBus.getDefault().post(new ProgressEvent(progress));
                NotificationUtils.getInstance().upProgress(getApplicationContext(), progress);
                isDown = true;
            }

            @Override
            public void onDownloadFailed(String message) {
                showMessage(message);
                EventBus.getDefault().post(new ProgressEvent(0));
                NotificationUtils.getInstance().cancleNotification(getApplicationContext());
                isDown = false;
            }
        });
    }

    private void showMessage(final String message) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            if (message == null || message.length() < 1)
                emitter.onNext(message);
            else
                emitter.onNext("下载失败,请稍后再试!");
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String message) {
                ToastUtil.showShort(message);
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.showShort(message);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @Override
    public void onDestroy() {
        DownloadUtil.get().cancleCall();
        NotificationUtils.getInstance().cancleNotification(getApplicationContext());
        super.onDestroy();
        stopSelf();
    }

}
