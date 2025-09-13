package com.yanzhenjie.durban.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.gallery.R;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public class CropView extends FrameLayout {
    private GestureCropImageView mGestureCropImageView;
    private final OverlayView mViewOverlay;

    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.durban_crop_view, this, true);
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.durban_CropView);
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a);
        a.recycle();
        setListenersToViews();
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(mViewOverlay::setTargetAspectRatio);
        mViewOverlay.setOverlayViewChangeListener(cropRect -> mGestureCropImageView.setCropRect(cropRect));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

}