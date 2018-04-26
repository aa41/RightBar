package com.xiaoma.mzrightbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.TypedValue;

import com.xiaoma.mzrightbar.rightbar.model.AppInfo;

/**
 * author: mxc
 * date: 2018/4/25.
 */

public class AppView {
    private final Canvas canvas;
    private final Context context;
    private static Paint paint=new Paint();

    public AppView(Canvas canvas, Context context) {
        this.canvas = canvas;
        this.context = context;
    }

    public void setAppData(AppInfo info,String tempPackageName){
        Rect location = info.getLocation();
        paint.setTextSize(sp2px(9));
        canvas.save();
        Bitmap bitmap = ((BitmapDrawable) info.getIcon()).getBitmap();
        Bitmap newBitmap =null;
        if(!TextUtils.isEmpty(tempPackageName)&& tempPackageName.equals(info.getPackageName())){
            paint.setColor(Color.RED);
            newBitmap= scaleBitmap(bitmap, 0.9f);
        }else {
            paint.setColor(Color.WHITE);
            newBitmap=  scaleBitmap(bitmap, 0.7f);
        }


        int width = newBitmap.getWidth();
        int height = newBitmap.getHeight();
        canvas.drawBitmap(newBitmap,location.left+(location.width()-width)/2,location.top+(location.height()-height)/2,paint);
        float v = paint.measureText(info.getAppName());
        canvas.drawText(info.getAppName(), location.left+(location.width()-v)/2,location.top+(location.height()-height)/2+height+30,paint);
        canvas.restore();
    }


    private float sp2px(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,px,context.getResources().getDisplayMetrics());
    }

    private   Bitmap scaleBitmap(Bitmap bm, float scale){
        int width = bm.getWidth();
        int height = bm.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newBm;
    }


}
