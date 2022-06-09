package com.moufans.update;


public interface OnAppUpdateListener {
    void onSuccess(VersionDataBean appVersionInfo);

    void onFailed(String failedMessage);
}
