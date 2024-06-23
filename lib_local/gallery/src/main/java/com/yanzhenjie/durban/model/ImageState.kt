package com.yanzhenjie.durban.model

import android.graphics.RectF

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
data class ImageState(
    var cropRect: RectF? = null,
    var currentImageRect: RectF? = null,
    var currentScale: Float? = null,
    var currentAngle: Float? = null
)