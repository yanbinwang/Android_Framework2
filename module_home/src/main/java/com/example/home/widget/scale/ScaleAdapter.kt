package com.example.home.widget.scale

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import androidx.viewpager.widget.PagerAdapter
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.glide.ImageLoader

/**
 * Created by wangyanbin
 * 伸缩图片适配器
 */
class ScaleAdapter(private val data: List<Pair<ScaleImageView, String>>) : PagerAdapter() {

    override fun getCount(): Int {
        return data.safeSize.orZero
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(data.safeGet(position)?.first)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val img = data.safeGet(position)?.first ?: return Any()
        ImageLoader.instance.display(img, data.safeGet(position)?.second.orEmpty())
        container.addView(img, MATCH_PARENT, MATCH_PARENT)
        return img
    }

}