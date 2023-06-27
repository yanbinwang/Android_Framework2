package com.example.glide.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.glide.ImageLoader
import com.example.glide.R

/**
 * @description 加载动图
 * @author yan
 */
class GifImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GifImageView)
        val res = typedArray.getResourceId(R.styleable.GifImageView_android_src, -1)
        if (res != -1) ImageLoader.instance.displayGif(this, res)
        typedArray.recycle()
    }

}