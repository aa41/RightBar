package com.xiaoma.mzrightbar.rightbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

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

    private boolean isShowLetter = true;

    private boolean isShowRight = false;

    private boolean isShowAPPs = false;

    private Paint textPaint;


    private LinkedHashMap<String, List<AppInfo>> infoMap = new LinkedHashMap<>();
    private List<AppInfo> infos = new ArrayList<>();
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
    private String tempBottomPackageName;
    private int lastX;
    private int lastY;
    private int nowX;
    private int nowY;
    private Bitmap gakki;
    private int alpha;
    private Paint backgroundPaint;
    private Bitmap mario;
    private Rect marioLocation;
    private boolean isJump = false;
    private boolean showBottomApp = false;
    private int jumpHeight = 50;
    private int jumpingHeight = 0;
    private ValueAnimator jumpAnimator = ValueAnimator.ofFloat(0, 1);
    private ValueAnimator showBottomAppAnimator = ValueAnimator.ofFloat(0, 1);
    private int coinAlpha = 255;
    private Bitmap coinBm;

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
        mario = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.mario), 0.8f);
        coinBm = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.toptop), 0.6f);
        jumpAnimator.setDuration(300);
        showBottomAppAnimator.setDuration(300);
        showBottomAppAnimator.setInterpolator(new BounceInterpolator());
        jumpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                jumpingHeight = (int) (value * jumpHeight);
                showBottomApp = true;
                invalidate();
            }
        });
        jumpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                showBottomApp = true;
                showBottomAppAnimator.start();
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showBottomApp = true;
                showBottomAppAnimator.start();
                invalidate();
            }
        });
        showBottomAppAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                jumpingHeight = (int) ((1 - value) * jumpHeight);
                coinAlpha = (int) ((1 - value) * 255);
                invalidate();
            }
        });
        showBottomAppAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                coinAlpha = 255;
                isJump = false;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                coinAlpha = 255;
                isJump = false;
                invalidate();
            }
        });
    }

    private void initPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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


    public void setFavoriteApp(List<AppInfo> infos) {
        if (infos == null) return;
        this.infos = infos;
        invalidate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        //a-z # 27
        itemHeight = (height / 5 * 3) / infoMap.size();
        barLocation.clear();
        top = (height - itemHeight * infoMap.size()) / 2;
        int topTemp = top;
        for (Map.Entry<String, List<AppInfo>> e : infoMap.entrySet()) {
            String key = e.getKey();
            List<AppInfo> value = e.getValue();

            if (itemWidth == 0) {
                textPaint.setTextSize(sp2px(12));
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
        //bottom app
        int appLeftTemp = ShowAppFromRight ? width - itemWidth * 2 : itemWidth * 2;
        int appItemWidth = (width - itemWidth * 4) / showAppsCount;
        int appHeightTemp = (int) (height - mario.getHeight() - jumpHeight - coinBm.getHeight() - 30 - appItemWidth * 1.2f);
        for (AppInfo info : infos) {
            Rect rect = new Rect();
            if (ShowAppFromRight) {
                rect.set(appLeftTemp - appItemWidth, appHeightTemp, appLeftTemp, (int) (appHeightTemp + appItemWidth * 1.2f));
            } else {
                rect.set(appLeftTemp, appHeightTemp, appLeftTemp + appItemWidth, (int) (appHeightTemp + appItemWidth * 1.2f));
            }
            appLeftTemp = ShowAppFromRight ? appLeftTemp - appItemWidth : appLeftTemp + appItemWidth;
            if (ShowAppFromRight && appLeftTemp - itemWidth * 2 < appItemWidth) {
                appLeftTemp = width - itemWidth * 2;
                appHeightTemp -= appItemWidth * 1.2f;
            } else if (!ShowAppFromRight && appLeftTemp + appItemWidth > width - itemWidth * 2) {
                appLeftTemp = itemWidth * 2;
                appHeightTemp -= appItemWidth * 1.2f;
            }
            info.setLocation(rect);

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
            backgroundPaint.setAlpha(alpha);
            canvas.drawBitmap(gakki, new Rect(0, 0, gakki.getWidth(), gakki.getHeight()), new Rect(0, 0, width, height), backgroundPaint);
            // canvas.drawColor(Color.parseColor("#33000000"));
            //  drawRight(canvas);
            drawLeft(canvas);
            drawBottom(canvas);
        }

        if (isShowAPPs) {
            if (isShowLetter) {
                backgroundPaint.setAlpha(100);
                backgroundPaint.setColor(Color.DKGRAY);
                canvas.drawCircle(width - itemWidth - itemWidth / 2, itemWidth + itemWidth / 2, itemWidth, backgroundPaint);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(sp2px(14));
                float v = textPaint.measureText(tempKey);
                canvas.drawText(tempKey, (width - itemWidth - itemWidth / 2), itemWidth + itemWidth / 2 + barTextHeight / 2, textPaint);
            }
            drawApps(canvas);
        }

        if (showBottomApp) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(sp2px(9));
            for (AppInfo info : infos) {
                Rect location = info.getLocation();
                Bitmap bitmap = ((BitmapDrawable) info.getIcon()).getBitmap();
                Bitmap newBitmap;
                float v = paint.measureText(info.getAppName());
                if (!TextUtils.isEmpty(tempBottomPackageName) && tempBottomPackageName.equals(info.getPackageName())) {
                    paint.setColor(Color.RED);
                    newBitmap = scaleBitmap(bitmap, 0.9f);
                } else {
                    paint.setColor(Color.WHITE);
                    newBitmap = scaleBitmap(bitmap, 0.7f);
                }
                canvas.drawBitmap(newBitmap, location.left + (location.width() - newBitmap.getWidth()) / 2, location.top + (location.height() - newBitmap.getHeight()) / 2, null);
                canvas.drawText(info.getAppName(), location.left + (location.width() - v) / 2, location.top + (location.height() - newBitmap.getHeight()) / 2 + newBitmap.getHeight() + 30, paint);
            }


        }


    }

    private void drawBottom(Canvas canvas) {
        int mW = mario.getWidth();
        int mH = mario.getHeight();
        marioLocation = new Rect((width - mW) / 2, height - mH, (width + mW) / 2, height);
        if (isJump) {
            canvas.drawBitmap(mario, (width - mW) / 2, height - mH - jumpingHeight, null);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setAlpha(coinAlpha);
            coinBm = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.toptop), 0.6f);
            canvas.drawBitmap(coinBm, (width - coinBm.getWidth()) / 2, height - mH - jumpHeight - coinBm.getHeight(), paint);
        }
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
                            isJump = false;
                            showBottomApp = false;
                            break;
                        }
                    }
                }
                if ((nowY < height / 5 || nowY > height / 5 * 4) && nowX < width && nowX > width - itemWidth) {
                    tempKey = "";
                    isShowAPPs = false;
                    tempPackageName = "";
                }

                if (!isShowAPPs && marioLocation.contains(nowX, nowY) && !isJump && !jumpAnimator.isStarted() && !showBottomApp) {
                    isJump = true;
                    jumpAnimator.start();
                }
                if (showBottomApp && !marioLocation.contains(nowX, nowY) && offsetY < offsetX && nowY > height - mario.getHeight()) {
                    isJump = false;
                    showBottomApp = false;
                }
                if (showBottomApp) {
                    boolean isTouchingApp = false;
                    for (AppInfo info : infos) {
                        if (info.getLocation().contains(nowX, nowY)) {
                            tempBottomPackageName = info.getPackageName();
                            isTouchingApp=true;
                            break;
                        }
                    }
                    if(!isTouchingApp){
                        tempBottomPackageName="";
                    }
                }

                if (isShowAPPs) {
                    boolean isTouchingApp = false;
                    List<AppInfo> appInfos = infoMap.get(tempKey);
                    for (AppInfo info : appInfos) {
                        Rect location = info.getLocation();
                        if (location.contains(nowX, nowY)) {
                            tempPackageName = info.getPackageName();
                            isTouchingApp = true;
                            break;
                        }
                    }
                    if (!isTouchingApp) {
                        tempPackageName = "";
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

                if(showBottomApp){
                    for (AppInfo info:infos){
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
                tempBottomPackageName="";
                isShowAPPs = false;
                showBottomApp = false;
                isJump = false;
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

    private Bitmap scaleBitmap(Bitmap bm, float scale) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newBm;
    }

    private OnTouchListener onTouchListener;

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public interface OnTouchListener {
        void touch(boolean isTouching);
    }

}
