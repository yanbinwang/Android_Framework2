package com.yanzhenjie.durban.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.gallery.R;

/**
 * 裁剪总布局（核心组合View）
 * 内部包含：
 * 1) GestureCropImageView → 负责图片旋转、缩放、裁剪
 * 2) OverlayView → 负责裁剪框、九宫格、阴影遮罩
 * 作用：把图片层 + 遮罩层 组合在一起，互相通信
 */
@SuppressLint("CustomViewStyleable")
public class CropView extends FrameLayout {
    // 图片裁剪View（旋转、缩放、手势、保存）
    private final GestureCropImageView mGestureCropImageView;
    // 遮罩层View（裁剪框、九宫格、半透明背景）
    private final OverlayView mViewOverlay;

    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 加载布局：包含 图片View + 遮罩View
        LayoutInflater.from(context).inflate(R.layout.durban_crop_view, this, true);
        // 绑定View
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);
        // 读取自定义属性（从XML）
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.durban_CropView);
        // 把属性分发给 图片View 和 遮罩View
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a);
        // 回收TypedArray
        a.recycle();
        // 设置两个子View的互相监听
        setListenersToViews();
    }

    private void setListenersToViews() {
        // 图片的裁剪比例变化 → 通知遮罩层更新
        mGestureCropImageView.setCropBoundsChangeListener(mViewOverlay::setTargetAspectRatio);
        // 遮罩层的裁剪框变化 → 通知图片层更新
        mViewOverlay.setOverlayViewChangeListener(mGestureCropImageView::setCropRect);
    }

    /**
     * 不需要延迟按压（优化触摸响应）
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * 外部获取内部View
     */
    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

}