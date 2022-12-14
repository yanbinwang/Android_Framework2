package com.example.glide.transform

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget


/**
 *  Created by wangyanbin
 *  图片等比缩放
 *   Glide.with(this)
 *  .load(newActiviteLeftBannerUrl)
 *  .asBitmap()
 *  .placeholder(R.drawable.placeholder)
 *  .into(new TransformationUtils(target));
 */
class ZoomTransform(var target: ImageView) : ImageViewTarget<Bitmap>(target) {

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
        //获取原图的宽高
        val width = resource?.width ?: 0
        val height = resource?.height ?: 0
        //获取imageView的宽
        val targetWidth = target.width
        //计算缩放比例
        val sy = (targetWidth * 0.1).toFloat() / (width * 0.1).toFloat()
        //计算图片等比例放大后的高
        val targetHeight = (height * sy).toInt()
        val params = target.layoutParams
        params?.height = targetHeight
        target.layoutParams = params
    }

}