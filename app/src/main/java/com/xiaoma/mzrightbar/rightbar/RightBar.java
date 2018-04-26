package com.xiaoma.mzrightbar.rightbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.xiaoma.mzrightbar.AppView;
import com.xiaoma.mzrightbar.R;
import com.xiaoma.mzrightbar.rightbar.model.AppInfo;
import com.xiaoma.mzrightbar.rightbar.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * author: mxc
 * date: 2018/4/25.
 */

public class RightBar extends View {

    private int showAppsCount = 5;

    private boolean ShowAppFromRight = true;

    private boolean isTouching = false;


    private boolean isShowRight = false;

    private boolean isShowAPPs = false;

    private Paint textPaint;


    private LinkedHashMap<String, List<AppInfo>> infoMap = new LinkedHashMap<>();
    private HashMap<String, Rect> barLocation = new HashMap<>();
    private int height;
    private int width;
    private int itemHeight;
    private int itemWidth;
    private int barTextHeight;
    private int barTextWidth;
    private Rect bounds = new Rect();
    private int top;
    private String tempKey;
    private String tempPackageName;
    private int lastX;
    private int lastY;
    private int nowX;
    private int nowY;
    private Bitmap gakki;
    private int alpha;

    public RightBar(Context context) {
        this(context, null);
    }

    public RightBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        gakki = BitmapFactory.decodeResource(getResources(), R.mipmap.gakki);
    }

    private void initPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setAppInfos(List<AppInfo> infos) {
        if (infos == null || infos.isEmpty()) return;
        infoMap.clear();
        for (AppInfo info : infos) {
            String firstLetter = info.getFirstLetter();
            List<AppInfo> infoList = infoMap.get(firstLetter);
            if (infoList == null) {
                infoList = new ArrayList<>();
                infoList.add(info);
                infoMap.put(firstLetter, infoList);
            } else {
                infoList.add(info);
            }
        }
        invalidate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        //a-z # 27
        itemHeight = (height/5*3) / infoMap.size();
        barLocation.clear();
        top = (height - itemHeight * infoMap.size()) / 2;
        int topTemp = top;
        for (Map.Entry<String, List<AppInfo>> e : infoMap.entrySet()) {
            String key = e.getKey();
            List<AppInfo> value = e.getValue();
            if (itemWidth == 0) {
                textPaint.setTextSize(sp2px(14));
                textPaint.getTextBounds(key, 0, key.length(), bounds);
                itemWidth = bounds.width() * 3;
                barTextHeight = bounds.height();
                barTextWidth = bounds.width();
            }
            barLocation.put(key, new Rect(width - itemWidth, topTemp, width, topTemp + itemHeight));

            int appLeftTemp = ShowAppFromRight ? width - itemWidth * 2 : itemWidth * 2;
            int appItemWidth = (width - itemWidth * 4) / showAppsCount;
            int appTopTemp = topTemp - itemHeight;
            if (appTopTemp + (Math.ceil(value.size() / (float) showAppsCount)) * appItemWidth * 1.2f >= height) {
                appTopTemp = (int) (height - (Math.ceil(value.size() / (float) showAppsCount)) * appItemWidth * 1.2f - appItemWidth * 0.3f);
            }
            for (AppInfo info : value) {
                Rect rect = new Rect();
                if (ShowAppFromRight) {
                    rect.set(appLeftTemp - appItemWidth, appTopTemp, appLeftTemp, (int) (appTopTemp + appItemWidth * 1.2f));
                } else {
                    rect.set(appLeftTemp, appTopTemp, appLeftTemp + appItemWidth, (int) (appTopTemp + appItemWidth * 1.2f));
                }

                appLeftTemp = ShowAppFromRight ? appLeftTemp - appItemWidth : appLeftTemp + appItemWidth;
                if (ShowAppFromRight && appLeftTemp - itemWidth * 2 < appItemWidth) {
                    appLeftTemp = width - itemWidth * 2;
                    appTopTemp += appItemWidth * 1.2f;
                } else if (!ShowAppFromRight && appLeftTemp + appItemWidth > width - itemWidth * 2) {
                    appLeftTemp = itemWidth * 2;
                    appTopTemp += appItemWidth * 1.2f;
                }

                info.setLocation(rect);
            }
            topTemp += itemHeight;
        }
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.gakki = bitmap;
        invalidate();
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidate();
    }

    public void setShowAppsCount(int showAppsCount) {
        this.showAppsCount = showAppsCount;
        invalidate();
    }

    public void setShowAppFromRight(boolean showAppFromRight) {
        ShowAppFromRight = showAppFromRight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isTouching) {
            Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setAlpha(alpha);
            canvas.drawBitmap(gakki, new Rect(0, 0, gakki.getWidth(), gakki.getHeight()), new Rect(0, 0, width, height), backgroundPaint);
            // canvas.drawColor(Color.parseColor("#33000000"));
            //  drawRight(canvas);
            drawLeft(canvas);
            drawBottom(canvas);
        }
        if (isShowAPPs) {
            drawApps(canvas);
        }


    }

    private void drawBottom(Canvas canvas) {

    }

    private void drawLeft(Canvas canvas) {
        for (Map.Entry<String, Rect> entry : barLocation.entrySet()) {
            String key = entry.getKey();
            Rect value = entry.getValue();
            if (key.equals(tempKey)) {
                textPaint.setColor(Color.RED);
            } else {
                textPaint.setColor(Color.WHITE);
            }
            canvas.drawText(key, barTextWidth, value.top + (value.height() - barTextHeight) / 2, textPaint);
        }
    }

    private void drawApps(Canvas canvas) {
        List<AppInfo> appInfos = infoMap.get(tempKey);
        for (AppInfo info : appInfos) {
            AppView appView = new AppView(canvas, getContext());
            appView.setAppData(info, tempPackageName);
        }
    }

    private void drawRight(Canvas canvas) {
        for (Map.Entry<String, Rect> entry : barLocation.entrySet()) {
            String key = entry.getKey();
            Rect value = entry.getValue();
            if (key.equals(tempKey)) {
                textPaint.setColor(Color.RED);
            } else {
                textPaint.setColor(Color.WHITE);
            }
            canvas.drawText(key, value.left + barTextWidth, value.top + (value.height() - barTextHeight) / 2, textPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                isTouching = true;
                break;

            case MotionEvent.ACTION_MOVE:
                isTouching = true;
                nowX = (int) event.getX();
                nowY = (int) event.getY();
                int offsetX = Math.abs(nowX - lastX);
                int offsetY = Math.abs(nowY - lastY);

                for (Map.Entry<String, List<AppInfo>> entry : infoMap.entrySet()) {
                    String key = entry.getKey();
                    Rect rect = barLocation.get(key);

                    if (rect != null) {
                        if (rect.contains(nowX, nowY) && offsetY > offsetX) {
                            tempKey = key;
                            tempPackageName = "";
                            isShowAPPs = true;
                            break;
                        }
                    }
                }
                if((nowY<height/5 || nowY>height/5*4) && nowX<width && nowX>width-itemWidth){
                    tempKey="";
                    isShowAPPs=false;
                    tempPackageName="";
                }

                if (isShowAPPs) {
                    boolean isTouchingApp=false;
                    List<AppInfo> appInfos = infoMap.get(tempKey);
                    for (AppInfo info : appInfos) {
                        Rect location = info.getLocation();
                        if (location.contains(nowX, nowY)) {
                            tempPackageName = info.getPackageName();
                            isTouchingApp=true;
                            break;
                        }
                    }
                    if(!isTouchingApp){
                        tempPackageName="";
                    }
                }

                lastX = nowX;
                lastY = nowY;


                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                nowX = (int) event.getX();
                nowY = (int) event.getY();
                if (isShowAPPs) {
                    List<AppInfo> appInfos = infoMap.get(tempKey);
                    for (AppInfo info : appInfos) {
                        Rect location = info.getLocation();
                        if (location.contains(nowX, nowY)) {
                            Utils.openPackage(getContext(), info.getPackageName());
                            break;
                        }
                    }
                }

                isTouching = false;
                tempKey = "";
                tempPackageName = "";
                isShowAPPs = false;

                break;

        }
        if (onTouchListener != null) {
            onTouchListener.touch(isTouching);
        }
        invalidate();
        return true;
    }

    private float sp2px(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, getContext().getResources().getDisplayMetrics());
    }

    private OnTouchListener onTouchListener;

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public interface OnTouchListener {
        void touch(boolean isTouching);
    }

}
