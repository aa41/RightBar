package com.xiaoma.mzrightbar.rightbar.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;

import com.xiaoma.mzrightbar.rightbar.RightBar;
import com.xiaoma.mzrightbar.rightbar.model.AppInfo;


import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * author: mxc
 * date: 2018/4/23.
 */

public class Utils {

    private static WindowManager.LayoutParams layoutParams;
    private static WindowManager windowManager;
    private static RightBar view;

    public static List<AppInfo> getPackageList(Context context) {
        List<AppInfo> infos = new ArrayList<>();
        PackageManager manager = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> aciInfos = manager.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo aciInfo : aciInfos) {
            String packageName = aciInfo.activityInfo.packageName;
            Drawable icon = aciInfo.loadIcon(manager);
            String appName = aciInfo.loadLabel(manager).toString();
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setAppName(appName);
            if (!TextUtils.isEmpty(appName)) {
                String[] arrays = PinyinHelper.toHanyuPinyinStringArray(appName.charAt(0));
                if (arrays != null && arrays.length != 0) {
                    appInfo.setFirstLetter(arrays[0].substring(0, 1).toUpperCase());
                } else {
                    //太长，显示效果不好，去除数字
                    appInfo.setFirstLetter(appName.substring(0, 1).toUpperCase());
                   // appInfo.setFirstLetter("#");
                }
            } else {
                appInfo.setFirstLetter("#");
            }
            appInfo.setIcon(icon);
            infos.add(appInfo);
        }
        Collections.sort(infos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                char[] o1Letters = o1.getFirstLetter().toCharArray();
                char[] o2Letters = o2.getFirstLetter().toCharArray();
                return new Character(o1Letters[0]).compareTo(new Character(o2Letters[0]));
            }
        });

        return infos;
    }


    public static void showRightBar(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if(view==null ){
            view = new RightBar(context);
            view.setAppInfos(getPackageList(context));
            // view.setFocusableInTouchMode(true);
            view.setOnTouchListener(new RightBar.OnTouchListener() {
                @Override
                public void touch(boolean isTouching) {
                    if (isTouching) {
                        if (layoutParams.width != WindowManager.LayoutParams.MATCH_PARENT) {
                            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            windowManager.updateViewLayout(view, layoutParams);
                        }
                    } else {
                        if (layoutParams.width != 25) {
                            layoutParams.width = 25;
                            windowManager.updateViewLayout(view, layoutParams);
                        }
                    }
                }
            });

            layoutParams = new WindowManager.LayoutParams();
          //  layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
            // 不设置这个弹出框的透明遮罩显示为黑色
            layoutParams.format = PixelFormat.TRANSLUCENT;
            // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
            // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
            // 不设置这个flag的话，home页的划屏会有问题

            layoutParams.width = 25;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

            layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;

            windowManager.addView(view, layoutParams);
        }

    }
    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            // 创建第三方应用的上下文环境
            try {
                pkgContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }

    public static boolean openPackage(Context context, String packageName) {
        Context pkgContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (pkgContext != null && intent != null) {
            pkgContext.startActivity(intent);
            return true;
        }
        return false;
    }


    public static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        // MainActivity完整名
        String mainAct = null;
        // 根据包名寻找MainActivity
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }


}
