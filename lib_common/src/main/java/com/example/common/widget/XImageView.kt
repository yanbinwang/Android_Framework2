package com.example.common.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.dinuscxj.progressbar.CircleProgressBar
import com.dinuscxj.progressbar.CircleProgressBar.SOLID_LINE
import com.example.common.R
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import com.example.glide.ImageLoader

/**
 * @description 进度条的加载
 * @author yan
 */
class XImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var root: FrameLayout? = null
    private val iv by lazy { ImageView(context) }
    private val cover by lazy { View(context) }
    private val progressBar by lazy { CircleProgressBar(context) }

    init {
        root = FrameLayout(context)
        root.size(MATCH_PARENT, MATCH_PARENT)
        iv.scaleType = ImageView.ScaleType.FIT_XY
        root?.addView(iv)
        cover.apply {
            background = GradientDrawable().apply { setColor(Color.parseColor("#cf111111")) }
            size(MATCH_PARENT, MATCH_PARENT)
        }
        root?.addView(cover)
        progressBar.apply {
            size(40.pt, 40.pt)
            setDrawBackgroundOutsideProgress(false)
            setLineWidth(4.ptFloat)
            setProgressBackgroundColor(color(R.color.blue_aac6f4))
            setProgressEndColor(color(R.color.bgWhite))
            setProgressStartColor(color(R.color.bgWhite))
            setCap(Paint.Cap.ROUND)
            setProgressStrokeWidth(4.ptFloat)
            setStyle(SOLID_LINE)
            setProgressTextColor(color(R.color.textWhite))
            setProgressTextSize(9.ptFloat)
        }
        root?.addView(progressBar)
        progressBar.apply {
            layoutGravity = Gravity.CENTER
            max = 100
            progress = 0
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(root)
    }

    fun load(url: String) {
        ImageLoader.instance.displayProgress(iv, url, {
            root.disable()
            cover.visible()
            progressBar.progress = 0
        }, {
            progressBar.progress = it.orZero
        }, {
            root.enable()
            cover.gone()
            progressBar.gone()
        })
    }

}