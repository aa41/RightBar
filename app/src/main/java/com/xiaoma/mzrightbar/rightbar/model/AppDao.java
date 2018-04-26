package com.xiaoma.mzrightbar.rightbar.model;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * author: mxc
 * date: 2018/4/26.
 */

@RealmClass
public class AppDao extends RealmObject {
    private String packageName;

    private String appName;
    private String firstLetter;


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
}
