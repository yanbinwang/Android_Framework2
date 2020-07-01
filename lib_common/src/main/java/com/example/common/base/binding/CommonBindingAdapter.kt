package com.example.common.base.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.common.imageloader.ImageLoader.Companion.instance

/**
 * Created by WangYanBin on 2020/6/10.
 * 基础全局xml绑定
 */
object CommonBindingAdapter {

    //---------------------------------------------图片注入开始---------------------------------------------
    @BindingAdapter(value = ["app:imageRes"])
    fun setImageRes(imageView: ImageView, imageRes: Int) {
        imageView.setImageResource(imageRes)
    }

    @BindingAdapter(value = ["app:imageUrl"])
    fun setImageDisplay(view: ImageView?, url: String?) {
        instance.displayImage(view, url)
    }

    @BindingAdapter(value = ["app:imageRoundUrl", "app:imageRoundRadius"])
    fun setImageRoundDisplay(view: ImageView?, url: String?, roundingRadius: Int) {
        instance.displayRoundImage(view, url, roundingRadius)
    }

    @BindingAdapter(value = ["app:imageCircleUrl"])
    fun setImageCircleDisplay(view: ImageView?, url: String?) {
        instance.displayCircleImage(view, url)
    }
    //---------------------------------------------图片注入结束---------------------------------------------

    //---------------------------------------------系统方法注入开始---------------------------------------------
    @BindingAdapter(value = ["app:textColorRes"])
    fun setTextColor(textView: TextView, textColorRes: Int) {
        textView.setTextColor(textView.resources.getColor(textColorRes))
    }

    @BindingAdapter(value = ["app:visible"])
    fun setVisible(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @BindingAdapter(value = ["app:selected"])
    fun setSelected(view: View, select: Boolean) {
        view.isSelected = select
    }

    @BindingAdapter(value = ["adjustWidth"])
    fun setAdjustWidth(view: View, adjustWidth: Int) {
        val params = view.layoutParams
        params.width = adjustWidth
        view.layoutParams = params
    }

    @BindingAdapter(value = ["app:adjustHeight"])
    fun setAdjustHeight(view: View, adjustHeight: Int) {
        val params = view.layoutParams
        params.height = adjustHeight
        view.layoutParams = params
    }
    //---------------------------------------------系统方法注入结束---------------------------------------------

}