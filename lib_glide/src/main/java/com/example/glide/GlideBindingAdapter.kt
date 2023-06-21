package com.example.glide

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable

/**
 * Created by WangYanBin on 2020/6/10.
 * 图片加载工具类
 */
object GlideBindingAdapter {

    // <editor-fold defaultstate="collapsed" desc="imageview绑定方法">
    /**
     * 加载图片（比例缩放）
     */
    @BindingAdapter(value = ["display_zoom"])
    fun bindingDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoom(view, url, { view.disable() }, { view.enable() })
    }

    /**
     * 加载图片
     */
    @BindingAdapter(value = ["display"], requireAll = false)
    fun bindingDisplay(view: ImageView, url: String) {
        ImageLoader.instance.display(view, url, onStart = { view.disable() }, onComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @BindingAdapter(value = ["display_round", "round_radius"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundRadius: Int?) {
        ImageLoader.instance.displayRound(view, url, roundRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @BindingAdapter(value = ["display_circle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircle(view, url)
    }
    // </editor-fold>

}