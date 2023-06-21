package com.example.glide.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide

/**
 * @description 加载本地gif图的view，需要注意gif图放在drawable文件中
 * @author yan
 */
class GifImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    override fun setImageDrawable(drawable: Drawable?) {
        Glide.with(context).asGif().load(drawable).into(this)
    }

}