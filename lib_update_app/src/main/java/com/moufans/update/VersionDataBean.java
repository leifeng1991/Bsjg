package com.moufans.update;

public class VersionDataBean {
    // 0:不需要更新 1：强制更新 2：更新
    private String status;
    // 标题
    private String title;
    // 更新内容
    private String content;
    // 地址
    private String url;
    // 版本号
    private String newversion;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewversion() {
        return newversion;
    }

    public void setNewversion(String newversion) {
        this.newversion = newversion;
    }
}
