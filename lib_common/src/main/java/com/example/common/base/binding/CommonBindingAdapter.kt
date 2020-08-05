package com.example.common.base.binding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.common.imageloader.ImageLoader.Companion.instance

/**
 * Created by WangYanBin on 2020/6/10.
 * 基础全局xml绑定
 */
object CommonBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["adjustWidth"])
    fun setAdjustWidth(view: View, adjustWidth: Int) {
        val params = view.layoutParams
        params.width = adjustWidth
        view.layoutParams = params
    }

    @JvmStatic
    @BindingAdapter(value = ["app:adjustHeight"])
    fun setAdjustHeight(view: View, adjustHeight: Int) {
        val params = view.layoutParams
        params.height = adjustHeight
        view.layoutParams = params
    }

}