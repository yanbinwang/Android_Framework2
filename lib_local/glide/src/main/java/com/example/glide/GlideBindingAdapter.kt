package com.example.glide

import android.graphics.drawable.Drawable
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
    @BindingAdapter(value = ["scaled_imageUrl"])
    fun bindingScaledImage(view: ImageView, imageUrl: String) {
        ImageLoader.instance.loadScaledImage(view, imageUrl, { view.disable() }, { view.enable() })
    }

    /**
     * 加载gif图片
     */
    @JvmStatic
    @BindingAdapter(value = ["gif_gifUrl"])
    fun bindingGifFromUrl(view: ImageView, gifUrl: String) {
        ImageLoader.instance.loadGifFromUrl(view, gifUrl)
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "errorResource"], requireAll = false)
    fun bindingImageFromUrl(view: ImageView, imageUrl: String, errorResource: Int?) {
        ImageLoader.instance.loadImageFromUrl(view, imageUrl, errorResource, { view.disable() }, { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["imageResource"])
    fun bindingImageFromResource(view: ImageView, imageResource: Int) {
        ImageLoader.instance.loadImageFromResource(view, imageResource, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["rounded_imageUrl", "rounded_errorResource", "rounded_cornerRadius"], requireAll = false)
    fun bindingRoundedImageFromUrl(view: ImageView, imageUrl: String, errorResource: Int?, cornerRadius: Int?) {
        ImageLoader.instance.loadRoundedImageFromUrl(view, imageUrl, errorResource, cornerRadius.toSafeInt(5))
    }

    @JvmStatic
    @BindingAdapter(value = ["rounded_imageResource", "rounded_cornerRadius"], requireAll = false)
    fun bindingRoundedImageFromResource(view: ImageView, imageResource: Int, cornerRadius: Int?) {
        ImageLoader.instance.loadRoundedImageFromResource(view, imageResource, cornerRadius = cornerRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["circular_imageUrl", "circular_errorResource"], requireAll = false)
    fun bindingCircularImageFromUrl(view: ImageView, imageUrl: String, errorResource: Int?) {
        ImageLoader.instance.loadCircularImageFromUrl(view, imageUrl, errorResource)
    }

    @JvmStatic
    @BindingAdapter(value = ["circular_imageResource"])
    fun bindingCircularImageFromResource(view: ImageView, imageResource: Int) {
        ImageLoader.instance.loadCircularImageFromResource(view, imageResource)
    }
    // </editor-fold>

}