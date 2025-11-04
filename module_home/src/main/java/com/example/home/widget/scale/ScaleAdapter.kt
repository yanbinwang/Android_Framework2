package com.example.home.widget.scale

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import androidx.viewpager.widget.PagerAdapter
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.invisible
import com.example.glide.ImageLoader

/**
 * Created by wangyanbin
 * 伸缩图片适配器
 */
class ScaleAdapter(private val data: List<Pair<ScaleImageView, String>>) : PagerAdapter() {

    override fun getCount(): Int {
        return data.safeSize.orZero
    }

    /**
     * 1. Java 的 ==
     * 基本类型：比较值是否相等（如 int, double）
     * 引用类型：比较引用是否指向同一个对象（即内存地址是否相同）
     * 2. Kotlin 的 == 和 ===
     * ==：等价于调用 equals() 方法，比较值是否相等
     * ===：比较两个引用是否指向同一个对象（与 Java 的 == 对引用类型的行为一致）
     */
    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
//        container.removeView(data.safeGet(position)?.first)
        // 直接移除传入的对象
        container.removeView(any as? ScaleImageView)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val img = data.safeGet(position)?.first ?: return Any()
        val (img, imageUrl) = data.safeGet(position) ?: throw IndexOutOfBoundsException("Invalid position: $position")
        ImageLoader.instance.loadImageFromUrl(img, imageUrl, onLoadStart = {
            img.invisible()
        }, onLoadComplete = {
            img.appear(1000)
        })
        container.addView(img, MATCH_PARENT, MATCH_PARENT)
        return img
    }

}