package com.example.common.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.dinuscxj.progressbar.CircleProgressBar
import com.dinuscxj.progressbar.CircleProgressBar.SOLID_LINE
import com.example.common.R
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.glide.ImageLoader

/**
 * @description 进度条的加载
 * @author yan
 */
class XImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val iv by lazy { ImageView(context) }
    private val progressBar by lazy { CircleProgressBar(context) }

    init {
        //背景为灰色
        size(MATCH_PARENT, MATCH_PARENT)
        background = GradientDrawable().apply { setColor("#cf111111".parseColor()) }
        //加载的图片
        addView(iv)
        //加载的进度条
        addView(progressBar)
        //设置内部ui基础属性
        iv.apply {
            scaleType = ImageView.ScaleType.FIT_XY
            size(MATCH_PARENT, MATCH_PARENT)
            gone()
        }
        progressBar.apply {
            size(40.pt, 40.pt)
            setDrawBackgroundOutsideProgress(false)
            setLineWidth(4.ptFloat)
            setProgressBackgroundColor(color(R.color.bgProgress))
            setProgressEndColor(color(R.color.bgProgressStart))
            setProgressStartColor(color(R.color.bgProgressEnd))
            setCap(Paint.Cap.ROUND)
            setProgressStrokeWidth(4.ptFloat)
            setStyle(SOLID_LINE)
            setProgressTextColor(color(R.color.textWhite))
            setProgressTextSize(9.ptFloat)
            layoutGravity = Gravity.CENTER
            max = 100
            progress = 0
            gone()
        }
    }

    fun load(url: String) {
        ImageLoader.instance.loadImageWithProgress(iv, url, {
            disable()
            iv.gone()
            iv.click {}
            progressBar.visible()
            progressBar.progress = 0
        }, {
            progressBar.progress = it.orZero
        }, {
            enable()
            iv.appear()
            progressBar.gone()
            //加载失败的话，点击可以再次加载
            if (!it) {
                iv.click {
                    load(url)
                }
            }
        })
    }

}