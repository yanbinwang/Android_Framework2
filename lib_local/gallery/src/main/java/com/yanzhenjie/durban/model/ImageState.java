package com.yanzhenjie.durban.model;

import android.graphics.RectF;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public class ImageState {

    private RectF mCropRect;
    private RectF mCurrentImageRect;

    private float mCurrentScale, mCurrentAngle;

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