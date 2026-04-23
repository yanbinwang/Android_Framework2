package com.example.gallery.feature.durban.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.example.gallery.R

/**
 * 裁剪总布局（组合View）
 * 内部包含：
 * 1) GestureCropImageView → 负责图片旋转、缩放、裁剪
 * 2) OverlayView → 负责裁剪框、九宫格、阴影遮罩
 * 作用：把图片层 + 遮罩层 组合在一起，互相通信
 */
@SuppressLint("CustomViewStyleable")
class CropView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    // 图片裁剪View（旋转、缩放、手势、保存）
    private var mGestureCropImageView: GestureCropImageView
    // 遮罩层View（裁剪框、九宫格、半透明背景）
    private var mViewOverlay: OverlayView

    init {
        // 加载布局：包含 图片View + 遮罩View (attachToRoot = true 自动添加当前布局)
        LayoutInflater.from(context).inflate(R.layout.durban_crop_view, this, true)
        // 绑定View
        mGestureCropImageView = findViewById(R.id.image_view_crop)
        mViewOverlay = findViewById(R.id.view_overlay)
        // 读取自定义属性（从XML）
        context.withStyledAttributes(attrs, R.styleable.durban_CropView) {
            // 把属性分发给 图片View 和 遮罩View
            mViewOverlay.processStyledAttributes(this)
            mGestureCropImageView.processStyledAttributes(this)
        }
        // 图片的裁剪比例变化 → 通知遮罩层更新
        mGestureCropImageView.setCropBoundsChangeListener(object : CropImageView.OnCropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                mViewOverlay.setTargetAspectRatio(cropRatio)
            }
        })
        // 遮罩层的裁剪框变化 → 通知图片层更新
        mViewOverlay.setOverlayViewChangeListener(object : OverlayView.OnOverlayChangeListener {
            override fun onCropRectUpdated(cropRect: RectF) {
                mGestureCropImageView.setCropRect(cropRect)
            }
        })
    }

    /**
     * 不需要延迟按压（优化触摸响应）
     */
    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * 外部获取内部View
     */
    fun getCropImageView(): GestureCropImageView {
        return mGestureCropImageView
    }

    fun getOverlayView(): OverlayView {
        return mViewOverlay
    }

}