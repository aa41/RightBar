package com.xiaoma.mzrightbar.rightbar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.xiaoma.mzrightbar.rightbar.RightBar;
import com.xiaoma.mzrightbar.rightbar.model.AppInfo;

import java.util.List;

import static com.xiaoma.mzrightbar.rightbar.utils.Utils.getPackageList;

/**
 * author: mxc
 * date: 2018/4/25.
 */

public class RightWindow {
    private final int width;
    private WindowManager windowManager;
    private final Context context;
    private RightBar rightBar;
    private static WindowManager.LayoutParams layoutParams;
    private boolean isShowing = false;
    private int rightTouchW;
    private final AppInstallReceiver receiver;

    public RightWindow(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        rightBar = new RightBar(context);
        layoutParams = new WindowManager.LayoutParams();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        width = dm.widthPixels;
        rightTouchW = width / 30;
        /* <action android:name="android.intent.action.PACKAGE_ADDED"/>
                <action android:name="android.intent.action.PACKAGE_REPLACED"/>
                <action android:name="android.intent.action.PACKAGE_REMOVED"/>*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        receiver = new AppInstallReceiver();
        context.registerReceiver(receiver, intentFilter);


    }

    public void destory() {
        if (windowManager != null) {
            windowManager.removeViewImmediate(rightBar);
        }
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public void show() {
        if (!isShowing) {
            rightBar.setAppInfos(getPackageList(context));
            // view.setFocusableInTouchMode(true);
            rightBar.setOnTouchListener(new RightBar.OnTouchListener() {
                @Override
                public void touch(boolean isTouching) {
                    if (isTouching) {
                        if (layoutParams.width != WindowManager.LayoutParams.MATCH_PARENT) {
                            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            windowManager.updateViewLayout(rightBar, layoutParams);
                        }
                    } else {
                        if (layoutParams.width != rightTouchW) {
                            layoutParams.width = rightTouchW;
                            windowManager.updateViewLayout(rightBar, layoutParams);
                        }
                    }
                }
            });


            //  layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.flags = layoutParams.flags & ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
            // 不设置这个弹出框的透明遮罩显示为黑色
            layoutParams.format = PixelFormat.TRANSLUCENT;
            // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
            // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
            // 不设置这个flag的话，home页的划屏会有问题
            layoutParams.width = rightTouchW;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            windowManager.addView(rightBar, layoutParams);
            isShowing = true;
        }
    }

    public View getContentView() {
        return rightBar;
    }

    public void setImageBackground(Bitmap bitmap) {
        rightBar.setBackgroundImage(bitmap);
    }

    public void setAlpha(int alpha) {
        rightBar.setAlpha(alpha);
    }

    public void setShowCount(int count) {
        rightBar.setShowAppsCount(count);
    }

    public void setShowFromRight(boolean right) {
        rightBar.setShowAppFromRight(right);
    }

    public void setFavoriteApp(List<AppInfo> infos) {
        rightBar.setFavoriteApp(infos);
    }


    public class AppInstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.PACKAGE_ADDED") || action.equals("android.intent.action.PACKAGE_REPLACED") || action.equals("android.intent.action.PACKAGE_REMOVED")) {
                rightBar.setAppInfos(Utils.getPackageList(context));
            }
        }
    }


}
