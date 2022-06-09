package com.moufans.update.down;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.moufans.lib_base.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 描述： 下载的工具类
 */
public class DownloadUtil {

    Call call;
    File file;
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url      下载连接
     * @param saveDir  储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    public void downloadApk(final String url, final String saveDir, final String name, final OnDownloadListener listener) {
        //判断下载URL为空
        if (TextUtils.isEmpty(url)) {
            LogUtil.e("DownUtil", "==========1===下载地址为空");
            listener.onDownloadFailed("下载地址为空");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtil.e("DownUtil", "==========2==" + e.toString());
                        listener.onDownloadFailed("下载异常,请检查您的网络稍后再试!!");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        saveStream(response, saveDir, name, listener);
                    }
                });
            }
        }).start();

    }

    /**
     * 保存图片
     */
    private void saveStream(Response response, String saveDir, String name, OnDownloadListener listener) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        long sum = 0;
        // 储存下载文件的目录
        String savePath = isExistDir(saveDir);
        if (TextUtils.isEmpty(saveDir)) {
            LogUtil.e("DownUtil", "==========3==下载异常,请稍后再试!!");
            listener.onDownloadFailed("下载异常,请稍后再试!!");
            return;
        }
        try {
            is = response.body().byteStream();
            long total = response.body().contentLength();
            file = new File(savePath, name + ".apk");
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                listener.onDownloading(progress);
            }
            fos.flush();
            saveFile(file, name, listener);

        } catch (Exception e) {
            LogUtil.e("DownUtil", "==========4==下载异常,请稍后再试!!" + e.toString());
            listener.onDownloadFailed("下载异常,请稍后再试!!");
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
        }
    }

    private void saveFile(File file, String name, OnDownloadListener listener) {
        if (!FileUtils.isFileExists(file.getPath())) {
            LogUtil.e("DownUtil", "==========5==下载失败");
            listener.onDownloadFailed("下载失败");
            return;
        }
        String dirPath = isExistDir("APK");
        if (TextUtils.isEmpty(dirPath)) return;
        try {
            InputStream inStream = new FileInputStream(file);
            File newFile = new File(dirPath, name + ".apk");
            FileOutputStream outputStream = new FileOutputStream(newFile);
            int byteread = 0;
            byte[] buffer2 = new byte[1444];
            while ((byteread = inStream.read(buffer2)) != -1)
                outputStream.write(buffer2, 0, byteread);
            inStream.close();
            outputStream.close();
            listener.onDownloadSuccess(newFile);
            FileUtils.deleteFile(file);
        } catch (Exception e) {
            LogUtil.e("DownUtil", "==========6==下载失败" + e.getMessage());
            listener.onDownloadFailed("下载失败");
        }
    }

    /**
     * 取消下载
     */
    public void cancleCall() {
        if (call != null) call.cancel();
    }

    /**
     * 判断文件是否存在 且返回文件地址
     */
    private String isExistDir(String saveDir) {
        if (TextUtils.isEmpty(saveDir)) return null;
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "QinLiKeJi" + File.separator + saveDir;
        boolean isExis = FileUtils.createOrExistsDir(dirPath);
        return isExis ? dirPath : null;
    }

}

