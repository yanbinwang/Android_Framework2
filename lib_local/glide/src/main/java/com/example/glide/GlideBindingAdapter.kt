package com.example.glide

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable

/**
 * Created by WangYanBin on 2020/6/10.
 * 图片加载工具类
 */
@Suppress("HasPlatformType")
object GlideBindingAdapter {

    // <editor-fold defaultstate="collapsed" desc="imageview绑定方法">
    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_zoom"])
    fun bindingDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoom(view, url, { view.disable() }, { view.enable() })
    }

    /**
     * 加载gif图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display_gif"])
    fun bindingDisplayGif(view: ImageView, url: String) {
        ImageLoader.instance.displayGif(view, url)
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display", "display_errorId"], requireAll = false)
    fun bindingDisplay(view: ImageView, url: String, errorId: Int?) = view.context.execute {
        ImageLoader.instance.display(view, url, errorId, { view.disable() }, onComplete = { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["display_resourceId"])
    fun bindingDisplayResource(view: ImageView, resourceId: Int) = view.context.execute {
        ImageLoader.instance.display(view, resourceId, onStart = { view.disable() }, onComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_round", "round_radius", "display_round_errorId"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundRadius: Int?, roundErrorId: Int?) = view.context.execute {
        ImageLoader.instance.displayRound(view, url, roundErrorId, roundRadius.toSafeInt(5))
    }

    @JvmStatic
    @BindingAdapter(value = ["display_round_resourceId", "round_radius"], requireAll = false)
    fun bindingDisplayRoundResource(view: ImageView, resourceId: Int, roundRadius: Int?) = view.context.execute {
        ImageLoader.instance.displayRound(view, resourceId, radius = roundRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_circle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircle(view, url)
    }

    @JvmStatic
    @BindingAdapter(value = ["display_circle_resourceId", "display_circle_errorId"], requireAll = false)
    fun bindingDisplayCircleResource(view: ImageView, resourceId: Int, circleErrorId: Int?) = view.context.execute {
        ImageLoader.instance.displayCircle(view, resourceId, circleErrorId)
    }
    // </editor-fold>

}