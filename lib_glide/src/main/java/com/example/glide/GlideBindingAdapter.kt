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
        ImageLoader.instance.displayZoom(view, url, onStart = { view.disable() }, onComplete = { view.enable() })
    }

    /**
     * 加载图片
     */
    @BindingAdapter(value = ["display", "placeholder_id"], requireAll = false)
    fun bindingDisplay(view: ImageView, url: String, placeholderId: Int?) {
        ImageLoader.instance.display(view, url, placeholderId.toSafeInt(R.drawable.shape_glide_loading), onStart = { view.disable() }, onComplete = { view.enable() })
    }

    @BindingAdapter(value = ["display_resource", "placeholder_id"], requireAll = false)
    fun bindingDisplayResource(view: ImageView, resource: Int, placeholderId: Int?) {
        ImageLoader.instance.display(view, resource, placeholderId.toSafeInt(R.drawable.shape_glide_loading), onStart = { view.disable() }, onComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @BindingAdapter(value = ["display_round", "rounding_radius"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundingRadius: Int?) {
        ImageLoader.instance.displayRound(view, url, roundingRadius.toSafeInt(5))
    }

    @BindingAdapter(value = ["display_round_resource", "rounding_radius"], requireAll = false)
    fun bindingDisplayRoundResource(view: ImageView, resource: Int, roundingRadius: Int?) {
        ImageLoader.instance.displayRound(view, resource, roundingRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @BindingAdapter(value = ["display_circle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircle(view, url)
    }

    @BindingAdapter(value = ["display_circle_resource"])
    fun bindingDisplayCircleResource(view: ImageView, resource: Int) {
        ImageLoader.instance.displayCircle(view, resource)
    }
    // </editor-fold>

}