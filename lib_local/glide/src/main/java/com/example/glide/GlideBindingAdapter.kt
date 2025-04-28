package com.example.glide

import android.graphics.drawable.Drawable
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
     * 图片样式
     */
    @JvmStatic
    @BindingAdapter(value = ["res", "drawable", "visibility"], requireAll = false)
    fun bindingImageViewTheme(view: ImageView, res: Int?, drawable: Drawable?, visibility: Int?) {
        if (res != null) {
            val resKey = R.id.glide_res_tag
            val oldSrcRes = view.getTag(resKey) as? Int
            if (oldSrcRes != res) {
                view.setImageResource(res)
                view.setTag(resKey, res)
            }
        } else if (drawable != null) {
            val drawableKey = R.id.glide_drawable_tag
            val oldDrawable = view.getTag(drawableKey) as? Drawable
            if (oldDrawable != drawable) {
                view.setImageDrawable(drawable)
                view.setTag(drawableKey, drawable)
            }
        }
        //处理可见性设置
        visibility?.let { newVisibility ->
            val visibilityKey = R.id.glide_visibility_tag
            val oldVisibility = view.getTag(visibilityKey) as? Int
            if (oldVisibility != newVisibility) {
                view.visibility = newVisibility
                view.setTag(visibilityKey, newVisibility)
            }
        }
    }

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
    @BindingAdapter(value = ["display", "display_error"], requireAll = false)
    fun bindingDisplay(view: ImageView, url: String, error: Int?) = view.context.execute {
        ImageLoader.instance.display(view, url, error, { view.disable() }, onComplete = { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["display_resource"])
    fun bindingDisplayResource(view: ImageView, resource: Int) = view.context.execute {
        ImageLoader.instance.display(view, resource, onStart = { view.disable() }, onComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_round", "round_radius", "round_error"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundRadius: Int?, roundError: Int?) = view.context.execute {
        ImageLoader.instance.displayRound(view, url, roundError, roundRadius.toSafeInt(5))
    }

    @JvmStatic
    @BindingAdapter(value = ["display_round_resource", "round_resource_radius"], requireAll = false)
    fun bindingDisplayRoundResource(view: ImageView, resource: Int, roundRadius: Int?) = view.context.execute {
        ImageLoader.instance.displayRound(view, resource, radius = roundRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_circle", "circle_error"], requireAll = false)
    fun bindingDisplayCircle(view: ImageView, url: String, circleError: Int?) {
        ImageLoader.instance.displayCircle(view, url, circleError)
    }

    @JvmStatic
    @BindingAdapter(value = ["display_circle_resource"])
    fun bindingDisplayCircleResource(view: ImageView, resource: Int) = view.context.execute {
        ImageLoader.instance.displayCircle(view, resource)
    }
    // </editor-fold>

}