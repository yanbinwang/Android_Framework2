package com.yanzhenjie.durban.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * 高效 Bitmap 绘制 Drawable
 * 作用：裁剪界面中专门用来显示图片的高性能组件
 * 比系统 BitmapDrawable 更轻、更快、无内存抖动
 */
public class FastBitmapDrawable extends Drawable {
    private int mAlpha;
    private int mWidth, mHeight;
    private Bitmap mBitmap;
    // 图片抗锯齿画笔
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public FastBitmapDrawable(Bitmap b) {
        mAlpha = 255;
        setBitmap(b);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, null, getBounds(), mPaint);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mPaint.setAlpha(alpha);
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mWidth;
    }

    @Override
    public int getMinimumHeight() {
        return mHeight;
    }

    public void setFilterBitmap(boolean filterBitmap) {
        mPaint.setFilterBitmap(filterBitmap);
    }

    public int getAlpha() {
        return mAlpha;
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
        if (b != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        } else {
            mWidth = mHeight = 0;
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}