package com.example.common.imageloader.glide.transform

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
class ZoomTransform(target: ImageView) : ImageViewTarget<Bitmap>(target) {
    private var target: ImageView? = target

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
        //获取原图的宽高
        val width = resource?.width
        val height = resource?.height
        //获取imageView的宽
        val imageViewWidth = target?.width
        //计算缩放比例
        val sy = (imageViewWidth!! * 0.1).toFloat() / (width!! * 0.1).toFloat()
        //计算图片等比例放大后的高
        val imageViewHeight = (height!! * sy).toInt()
        val params = target?.layoutParams
        params?.height = imageViewHeight
        target?.layoutParams = params
    }

}