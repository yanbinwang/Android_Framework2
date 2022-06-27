package com.example.common.base.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.common.imageloader.ImageLoader

/**
 * Created by WangYanBin on 2020/8/5.
 * 图片的加载方法，写成自定义属性直接viewmodel赋值
 */
object ImageViewBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["imageUrl"])
    fun setImageDisplay(view: ImageView, url: String) {
        ImageLoader.instance.displayImage(view, url)
    }

    @JvmStatic
    @BindingAdapter(value = ["imageZoomUrl"])
    fun setImageZoomDisplay(view: ImageView, url: String) {
        ImageLoader.instance.displayZoomImage(view, url)
    }

    @JvmStatic
    @BindingAdapter(value = ["imageRoundUrl"])
    fun setImageRoundDisplay(view: ImageView, url: String) {
        setImageRoundDisplay(view, url, 5)
    }

    @JvmStatic
    @BindingAdapter(value = ["imageRoundUrl", "imageRoundRadius"])
    fun setImageRoundDisplay(view: ImageView, url: String, roundingRadius: Int) {
        ImageLoader.instance.displayRoundImage(view, url, roundingRadius)
    }

    @JvmStatic
    @BindingAdapter(value = ["imageCircleUrl"])
    fun setImageCircleDisplay(view: ImageView, url: String) {
        ImageLoader.instance.displayCircleImage(view, url)
    }

}