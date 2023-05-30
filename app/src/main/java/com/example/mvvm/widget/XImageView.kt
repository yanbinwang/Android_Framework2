package com.example.mvvm.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import com.example.glide.ImageLoader
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewXimageBinding

/**
 * @description 进度条的加载
 * @author yan
 */
class XImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewXimageBinding.bind(context.inflate(R.layout.view_ximage)) }

    init {
        binding.root.layoutParams = LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT) //设置LayoutParams
        binding.viewCover.background = GradientDrawable().apply { setColor(Color.parseColor("#cf111111")) }
        binding.progressBar.apply {
            max = 100
            progress = 0
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    fun load(url: String) {
        ImageLoader.instance.displayProgress(binding.iv, url, {
            binding.root.disable()
            binding.viewCover.visible()
            binding.progressBar.progress = 0
        }, {
            binding.progressBar.progress = it.orZero
        }, {
            binding.root.enable()
            binding.viewCover.gone()
        })
    }

}