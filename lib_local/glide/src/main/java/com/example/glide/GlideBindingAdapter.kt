package com.example.glide

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.example.framework.utils.function.value.areDrawablesSame
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.glide.ImageLoader.Companion.DEFAULT_CIRCULAR_RESOURCE
import com.example.glide.ImageLoader.Companion.DEFAULT_CORNER_RADIUS
import com.example.glide.ImageLoader.Companion.DEFAULT_RESOURCE
import com.example.glide.ImageLoader.Companion.DEFAULT_ROUNDED_RESOURCE
import java.lang.ref.WeakReference

/**
 * Created by WangYanBin on 2020/6/10.
 * 图片加载工具类
 */
object GlideBindingAdapter {

    // <editor-fold defaultstate="collapsed" desc="imageview绑定方法">
    /**
     * 图片样式
     * 问题一：相同资源可能生成不同的 Drawable 实例
     * 当你通过 Context.getDrawable() 或 Resources.getDrawable() 获取同一个资源 ID 的 Drawable 时，每次获取的可能是不同的实例
     * val drawable1 = context.getDrawable(R.drawable.my_drawable)
     * val drawable2 = context.getDrawable(R.drawable.my_drawable)
     * // 很可能返回 false
     * println(drawable1 == drawable2)
     *
     * 问题二：状态可变性导致的比较问题
     * 即使两个 Drawable 来源于同一个资源，它们的状态也可能不同
     * val drawable1 = context.getDrawable(R.drawable.my_drawable).mutate()
     * val drawable2 = context.getDrawable(R.drawable.my_drawable).mutate()
     * drawable1.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
     * // 即使资源相同，状态不同也会导致比较失败
     * println(drawable1 == drawable2) // false
     *
     * 方法一：比较资源 ID（如果适用）
     * 如果你存储的是资源 ID 而非 Drawable 实例，可以直接比较资源 ID
     *
     * 方法二：比较 Drawable 的恒定属性
     * 如果你确实需要比较 Drawable 实例，可以比较其内在属性
     */
    @JvmStatic
    @BindingAdapter(value = ["res", "drawable", "visibility"], requireAll = false)
    fun bindingImageViewTheme(view: ImageView, @DrawableRes res: Int?, drawable: Drawable?, visibility: Int?) {
        res?.let { newRes ->
            val resKey = R.id.glide_res_tag
            val oldSrcRes = view.getTag(resKey) as? Int
            if (oldSrcRes != newRes) {
                view.setImageResource(newRes)
                view.setTag(resKey, newRes)
            }
        }
        drawable?.let { newDrawable ->
            val drawableKey = R.id.glide_drawable_tag
            val oldDrawable = (view.getTag(drawableKey) as? WeakReference<Drawable>)?.get()
            if (oldDrawable == null || !areDrawablesSame(oldDrawable, newDrawable)) {
                view.setImageDrawable(newDrawable)
                view.setTag(drawableKey, WeakReference(newDrawable))
            }
        }
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
     * 加载gif图片
     */
    @JvmStatic
    @BindingAdapter(value = ["gif_gifUrl"])
    fun bindingGifFromUrl(view: ImageView, gifUrl: String?) {
        ImageLoader.instance.loadGifFromUrl(view, gifUrl)
    }

    @JvmStatic
    @BindingAdapter(value = ["gif_gifResource"])
    fun bindingGifFromResource(view: ImageView, @RawRes @DrawableRes gifResource: Int?) {
        ImageLoader.instance.loadGifFromResource(view, gifResource)
    }

    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["scaled_imageUrl"])
    fun bindingScaledFromUrl(view: ImageView, imageUrl: String?) {
        ImageLoader.instance.loadScaledFromUrl(view, imageUrl, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "errorResource"], requireAll = false)
    fun bindingImageFromUrl(view: ImageView, imageUrl: String?, @DrawableRes errorResource: Int?) {
        val effectiveErrorResource = errorResource ?: DEFAULT_RESOURCE
        ImageLoader.instance.loadImageFromUrl(view, imageUrl, effectiveErrorResource, { view.disable() }, { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["imageResource"])
    fun bindingImageFromResource(view: ImageView, @RawRes @DrawableRes imageResource: Int?) {
        ImageLoader.instance.loadImageFromResource(view, imageResource, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["cardview_imageUrl", "cardview_errorResource"], requireAll = false)
    fun bindingCardViewFromUrl(view: CardView, imageUrl: String?, @DrawableRes errorResource: Int?) {
        val effectiveErrorResource = errorResource ?: DEFAULT_RESOURCE
        ImageLoader.instance.loadCardViewFromUrl(view, imageUrl, effectiveErrorResource, { view.disable() }, { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["cardview_imageResource"])
    fun bindingCardViewFromResource(view: CardView, @RawRes @DrawableRes imageResource: Int?) {
        ImageLoader.instance.loadCardViewFromResource(view, imageResource, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["rounded_imageUrl", "rounded_errorResource", "rounded_cornerRadius", "rounded_overrideColor"], requireAll = false)
    fun bindingRoundedImageFromUrl(view: ImageView, imageUrl: String?, errorResource: Int?, cornerRadius: Int?, overrideColor: Int?) {
        val effectiveErrorResource = errorResource ?: DEFAULT_ROUNDED_RESOURCE
        val effectiveCornerRadius = cornerRadius ?: DEFAULT_CORNER_RADIUS
        val effectiveOverrideColor = overrideColor ?: Color.WHITE
        ImageLoader.instance.loadRoundedImageFromUrl(view, imageUrl, effectiveErrorResource, effectiveCornerRadius.toSafeInt(), overrideColor = effectiveOverrideColor, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["rounded_imageResource", "rounded_cornerRadius", "rounded_overrideColor"], requireAll = false)
    fun bindingRoundedImageFromResource(view: ImageView, @RawRes @DrawableRes imageResource: Int?, cornerRadius: Int?, overrideColor: Int?) {
        val effectiveCornerRadius = cornerRadius ?: DEFAULT_CORNER_RADIUS
        val effectiveOverrideColor = overrideColor ?: Color.WHITE
        ImageLoader.instance.loadRoundedImageFromResource(view, imageResource, cornerRadius = effectiveCornerRadius.toSafeInt(), overrideColor = effectiveOverrideColor, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["circular_imageUrl", "circular_errorResource"], requireAll = false)
    fun bindingCircularImageFromUrl(view: ImageView, imageUrl: String?, errorResource: Int?) {
        val effectiveErrorResource = errorResource ?: DEFAULT_CIRCULAR_RESOURCE
        ImageLoader.instance.loadCircularImageFromUrl(view, imageUrl, effectiveErrorResource, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }

    @JvmStatic
    @BindingAdapter(value = ["circular_imageResource"])
    fun bindingCircularImageFromResource(view: ImageView, @RawRes @DrawableRes imageResource: Int?) {
        ImageLoader.instance.loadCircularImageFromResource(view, imageResource, onLoadStart = { view.disable() }, onLoadComplete = { view.enable() })
    }
    // </editor-fold>

}