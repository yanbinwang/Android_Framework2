package com.yanzhenjie.durban.model

import android.graphics.RectF

/**
 * 图片裁剪状态实体类
 * 作用：记录裁剪界面中图片的实时状态（位置、缩放、角度、裁剪框）
 */
data class ImageState(
    var cropRect: RectF? = null, // 裁剪框矩形区域
    var currentImageRect: RectF? = null, // 当前图片所在矩形区域
    var currentScale: Float = 0f, // 当前图片缩放比例
    var currentAngle: Float = 0f // 当前图片旋转角度
)