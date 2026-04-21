package com.yanzhenjie.durban.model;

import android.graphics.RectF;

/**
 * 图片裁剪状态实体类
 * 作用：记录裁剪界面中图片的实时状态（位置、缩放、角度、裁剪框）
 */
public class ImageState {
    // 当前图片缩放比例
    private final float mCurrentScale;
    // 当前图片旋转角度
    private final float mCurrentAngle;
    // 裁剪框矩形区域
    private final RectF mCropRect;
    // 当前图片所在矩形区域
    private final RectF mCurrentImageRect;

    public ImageState(RectF cropRect, RectF currentImageRect, float currentScale, float currentAngle) {
        mCropRect = cropRect;
        mCurrentImageRect = currentImageRect;
        mCurrentScale = currentScale;
        mCurrentAngle = currentAngle;
    }

    public RectF getCropRect() {
        return mCropRect;
    }

    public RectF getCurrentImageRect() {
        return mCurrentImageRect;
    }

    public float getCurrentScale() {
        return mCurrentScale;
    }

    public float getCurrentAngle() {
        return mCurrentAngle;
    }

}