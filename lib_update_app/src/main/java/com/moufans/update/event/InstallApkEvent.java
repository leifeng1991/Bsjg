package com.moufans.update.event;

public class InstallApkEvent {
    private String apkPath;

    public InstallApkEvent(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getApkPath() {
        return apkPath;
    }
}
