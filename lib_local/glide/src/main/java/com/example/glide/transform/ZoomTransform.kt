package com.example.glide.transform

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.doOnceAfterLayout

/**
 *  Created by wangyanbin
 *  图片等比缩放
 *   Glide.with(this)
 *  .load(newActiviteLeftBannerUrl)
 *  .asBitmap()
 *  .placeholder(R.drawable.placeholder)
 *  .into(new TransformationUtils(target));
 */
class ZoomTransform(private var target: ImageView) : ImageViewTarget<Bitmap>(target) {

    override fun setResource(resource: Bitmap?) {
        resource ?: return
        // 显式使用 target 调用扩展函数，与类成员变量保持一致
        target.doOnceAfterLayout { imageView ->
            // 获取原图宽高
            val originalWidth = resource.width
            val originalHeight = resource.height
            // 获取ImageView宽高（此时已确保布局完成）
            val targetWidth = imageView.width
            // 安全校验：避免原图宽高为0导致的异常
            if (originalWidth <= 0 || originalHeight <= 0 || targetWidth <= 0) {
                target.setImageBitmap(resource)
                return@doOnceAfterLayout
            }
            // 计算缩放比例
            val scale = targetWidth.toFloat() / originalWidth.toFloat()
            // 计算目标高度（保持比例）
            val targetHeight = (originalHeight * scale).toInt()
            // 设置图片和调整高度
            target.setImageBitmap(resource)
            target.layoutParams?.height = targetHeight
//            // 某些场景下需要显式触发布局更新
//            target.requestLayout()
        }
    }

}