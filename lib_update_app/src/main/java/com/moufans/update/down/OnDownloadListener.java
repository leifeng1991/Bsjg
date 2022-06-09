package com.moufans.update.down;

import java.io.File;

public interface OnDownloadListener {
    /**
     * 下载成功
     */
    void onDownloadSuccess(File file);

    /**
     * @param progress 下载进度
     */
    void onDownloading(int progress);

    /**
     * 下载失败
     *
     * @param message
     */
    void onDownloadFailed(String message);
}
