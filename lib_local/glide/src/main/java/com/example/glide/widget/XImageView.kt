package com.example.glide.widget

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.dinuscxj.progressbar.CircleProgressBar
import com.dinuscxj.progressbar.CircleProgressBar.SOLID_LINE
import com.example.framework.utils.function.value.createCornerDrawable
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.clearClick
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.glide.ImageLoader
import com.example.glide.R

/**
 * @description 进度条的加载
 * @author yan
 */
class XImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val iv by lazy { ImageView(context) }
    private val progressBar by lazy { CircleProgressBar(context) }

    init {
        // 背景为灰色
        background = createCornerDrawable("#cf111111")
        // 加载的图片
        addView(iv)
        // 加载的进度条
        addView(progressBar)
        // 设置内部ui基础属性
        iv.let {
            it.scaleType = ImageView.ScaleType.FIT_XY
            it.size(MATCH_PARENT, MATCH_PARENT)
            it.gone()
        }
        progressBar.let {
            it.size(dip2px(40f), dip2px(40f))
            it.setDrawBackgroundOutsideProgress(false)
            it.setLineWidth(dip2px(4f).toSafeFloat())
            it.setProgressBackgroundColor(color(R.color.bgProgress))
            it.setProgressEndColor(color(R.color.bgProgressStart))
            it.setProgressStartColor(color(R.color.bgProgressEnd))
            it.setCap(Paint.Cap.ROUND)
            it.setProgressStrokeWidth(dip2px(4f).toSafeFloat())
            it.setStyle(SOLID_LINE)
            it.setProgressTextColor(color(R.color.textProgress))
            it.setProgressTextSize(dip2px(9f).toSafeFloat())
            it.layoutGravity = Gravity.CENTER
            it.max = 100
            it.progress = 0
            it.gone()
        }
    }

    fun load(url: String) {
        ImageLoader.instance.loadProgressFromUrl(iv, url, {
            disable()
            iv.gone()
            iv.clearClick()
            progressBar.visible()
            progressBar.progress = 0
        }, {
            progressBar.progress = it.orZero
        }, {
            enable()
            iv.appear()
            progressBar.gone()
            // 加载失败的话，点击可以再次加载
            if (null == it) {
                iv.click {
                    load(url)
                }
            }
        })
    }

    /**
     * dip转px
     */
    fun dip2px(dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * px转dip
     */
    fun px2dip(pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

}