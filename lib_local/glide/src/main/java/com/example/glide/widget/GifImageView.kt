package com.example.glide.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.example.framework.utils.function.value.orFalse
import com.example.glide.R
import java.lang.ref.WeakReference

/**
 * @description 加载gif动图
 * @author yan
 *   <com.example.glide.widget.GifImageView
 *    android:id="@+id/gf_quick"
 *    android:layout_width="46pt"
 *    android:layout_height="46pt"
 *    app:gifUrl="@drawable/bg_quick_pass"/>
 */
class GifImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var gifDrawable: GifDrawable? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.GifImageView) {
            // 先处理网络 URL（优先级不变）
            val gifUrl = getString(R.styleable.GifImageView_gifUrl)
            if (!gifUrl.isNullOrEmpty()) {
                loadGifFromUrl(gifUrl)
            } else {
                // 处理 gifSrc：区分资源引用/颜色值
                val gifResId = getResourceId(R.styleable.GifImageView_gifSrc, -1)
                if (gifResId != -1) {
                    // 资源ID有效，说明是本地drawable/mipmap资源
                    loadGifFromResource(gifResId)
                } else {
                    // 资源ID无效，尝试获取颜色值
                    val color = getColor(R.styleable.GifImageView_gifSrc, Color.TRANSPARENT)
                    setBackgroundColor(color)
                }
            }
        }
    }

    /**
     * 资源释放
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGifAnimation()
        Glide.with(this).clear(this)
        gifDrawable = null
    }

    /**
     * 加载本地资源中的 GIF（drawable/mipmap）
     */
    fun loadGifFromResource(resId: Int) {
        Glide.with(this)
            .asGif()
            .load(resId)
            .apply(RequestOptions().fitCenter())
            // Glide 的 ImageViewTarget 中 setResource 方法的回调是在主线程执行的
            .into(GifTarget(this))
    }

    /**
     * 加载服务器 URL 中的 GIF
     */
    fun loadGifFromUrl(url: String) {
        Glide.with(this)
            .asGif()
            .load(url)
            .apply(RequestOptions().fitCenter())
            .into(GifTarget(this))
    }

    /**
     * 开始 GIF 动画
     */
    fun startGifAnimation() {
        gifDrawable?.apply {
            // 如果当前正在播放，先停止再从第一帧启动
            if (isRunning) {
                stop()
                startFromFirstFrame()
            } else {
                // 如果未播放，直接从第一帧启动
                startFromFirstFrame()
            }
        }
    }

    /**
     * 停止 GIF 动画
     */
    fun stopGifAnimation() {
        gifDrawable?.stop()
    }

    /**
     * 判断 GIF 是否正在播放
     */
    fun isGifPlaying(): Boolean {
        return gifDrawable?.isRunning.orFalse
    }

    /**
     * 静态内部类 + 弱引用，避免强引用泄漏
     */
    private class GifTarget(view: GifImageView) : ImageViewTarget<GifDrawable>(view) {
        // 用弱引用持有 GifImageView，不影响 GC 回收
        private val weakView = WeakReference(view)

        override fun setResource(resource: GifDrawable?) {
            // 若 View 已回收，直接返回
            val gifView = weakView.get() ?: return
            gifView.gifDrawable = resource
            gifView.setImageDrawable(resource)
            gifView.startGifAnimation()
        }
    }

}