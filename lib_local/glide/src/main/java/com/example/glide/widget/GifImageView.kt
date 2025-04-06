package com.example.glide.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.glide.ImageLoader
import com.example.glide.R
import androidx.core.content.withStyledAttributes

/**
 * @description 加载gif动图
 * @author yan
 *   <com.example.glide.widget.GifImageView
 *    android:id="@+id/gf_quick"
 *    android:layout_width="46pt"
 *    android:layout_height="46pt"
 *    android:src="@drawable/bg_quick_pass"/>
 */
class GifImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        context.withStyledAttributes(attrs, R.styleable.GifImageView) {
            val res = getResourceId(R.styleable.GifImageView_android_src, -1)
            if (res != -1) ImageLoader.instance.displayGif(this@GifImageView, res)
        }
    }

}