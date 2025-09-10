package com.example.common.widget.advertising

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils.calculateLuminance
import androidx.core.graphics.get
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.framework.utils.function.defTypeMipmap
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.click
import com.example.glide.ImageLoader
import kotlin.math.max


/**
 *  Created by wangyanbin
 *  广告适配器
 */
@SuppressLint("NotifyDataSetChanged")
class AdvertisingAdapter : RecyclerView.Adapter<AdvertisingAdapter.ViewHolder>() {
    private var radius = 0
    private var localAsset = false
    private var list = ArrayList<String>()
    private var onItemClick: ((position: Int) -> Unit)? = null

    companion object {
        /**
         * 根据传入的颜色阈值决定状态栏/导航栏电池图标的深浅（黑或白）
         */
        @JvmStatic
        fun getBatteryIcon(@ColorRes backgroundColor: Int): Boolean {
            // 使用系统API获取相对亮度（0.0-1.0之间）
            val luminance = calculateLuminance(color(backgroundColor))
            // 亮度阈值，这里使用0.5作为中间值
            // 白色的相对亮度（luminance）是 1.0（最高值），黑色是 0.0（最低值）
            return if (luminance < 0.5) true else false
        }

        /**
         * 通过调色盘Palette提取广告背景覆盖物颜色
         * 部分广告可能直接连同导航栏,故而需要增加一段背景
         */
        fun getAdvCover(originalBitmap: Bitmap?): Int {
            originalBitmap ?: return 0
            // 获取顶部中心位置的像素颜色
            val centerX = originalBitmap.getWidth().orZero / 2
            // 顶部y坐标（0表示最顶部）
            val topY = 0
            // 确保坐标在有效范围内
            return if (centerX >= 0 && centerX < originalBitmap.getWidth() && topY < originalBitmap.getHeight()) {
                originalBitmap[centerX, topY]
            } else {
                0
            }
        }

        /**
         * 渐隐切换背景色
         */
        @JvmStatic
        fun fadeBackgroundToTopColor(imageView: ImageView?, targetView: View?, duration: Int) {
            // 1. 获取图片的Bitmap
            val drawable = imageView?.getDrawable() as? BitmapDrawable
            drawable ?: return
            val bitmap = drawable.bitmap
            if (bitmap == null) return
            // 2. 截取顶部区域（取顶部1/10高度）
            val topHeight = max(1, bitmap.getHeight() / 10)
            val topBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), topHeight)
            // 3. 从顶部区域提取主色调
            Palette.from(topBitmap).generate { palette ->
                if (palette == null) return@generate
                // 优先获取有活力的色调，没有则用其他类型
                var swatch: Palette.Swatch? = palette.vibrantSwatch
                if (swatch == null) {
                    swatch = palette.mutedSwatch
                }
                if (swatch == null) {
                    swatch = palette.dominantSwatch // 主色调
                }
                if (swatch != null) {
                    val targetColor: Int = swatch.rgb
                    // 4. 执行颜色渐变动画
                    startColorTransition(targetView, targetColor, duration)
                }
            }
        }

        @JvmStatic
        private fun startColorTransition(view: View?, targetColor: Int, duration: Int) {
            view ?: return
            // 获取当前背景色
            var startColor = view.solidColor
            // 如果当前没有背景色，使用透明色作为起点
            if (startColor == 0) {
                // 透明
                startColor = 0x00000000
            }
            // 创建颜色过渡动画
            val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, targetColor)
            // 动画持续时间
            colorAnimator.setDuration(duration.toLong())
            // 动画更新时设置新颜色
            colorAnimator.addUpdateListener { animator: ValueAnimator? ->
                val color = animator!!.getAnimatedValue() as Int
                view.setBackgroundColor(color)
            }
            // 启动动画
            colorAnimator.start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            click { onItemClick?.invoke(position.mod(list.safeSize)) }
            val bean = list.safeGet(position.mod(list.safeSize)) ?: return
            val image = (this as? ImageView) ?: return
            if (localAsset) {
                ImageLoader.instance.loadRoundedDrawableFromResource(image, context.defTypeMipmap(bean), cornerRadius = radius.pt)
            } else {
                ImageLoader.instance.loadRoundedImageFromUrl(image, bean, cornerRadius = radius.pt)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < 2) list.safeSize else Int.MAX_VALUE
    }

    fun refresh(list: List<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setParams(radius: Int = 0, localAsset: Boolean) {
        this.radius = radius
        this.localAsset = localAsset
    }

    fun setOnItemClickListener(onItemClick: ((position: Int) -> Unit)) {
        this.onItemClick = onItemClick
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        init {
            //设置缩放方式
            itemView.scaleType = ImageView.ScaleType.FIT_XY
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT)
        }
    }

}
