package com.xiaoma.mzrightbar.rightbar.model;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * author: mxc
 * date: 2018/4/25.
 */

public class AppInfo {
    private String packageName;
    private String appName;
    private String firstLetter;
    private Drawable icon;
    private Rect location;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public Rect getLocation() {
        return location;
    }

    public void setLocation(Rect location) {
        this.location = location;
    }
}
