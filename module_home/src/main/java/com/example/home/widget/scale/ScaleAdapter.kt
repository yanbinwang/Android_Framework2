package com.example.home.widget.scale

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import androidx.viewpager.widget.PagerAdapter
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.invisible
import com.example.glide.ImageLoader

/**
 * Created by wangyanbin
 * 伸缩图片适配器
 */
class ScaleAdapter(private val data: List<Pair<ScaleImageView, String>>) : PagerAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    /**
     * 1) Java 的 ==
     *  基本类型：比较值是否相等（如 int, double）
     *  引用类型：比较引用是否指向同一个对象（即内存地址是否相同）
     * 2) Kotlin 的 == 和 ===
     *  ==：比较值是否相等（等价于 Java 的 equals()）
     *  ===：比较两个引用是否指向同一个对象（等价于 Java 的 ==）
     */
    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
//        container.removeView(data.safeGet(position)?.first)
        // 直接移除传入的对象
        val view = any as? View ?: return
        container.removeView(view)
    }

    /**
     * instantiateItem 不是顺序一次性全部创建，在滑动、预加载、页面销毁重建、快速连续滑动时，position 参数会出现和当前 data 集合不匹配的场景
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val img = data.safeGet(position)?.first ?: return Any()
        // 边界校验，非法 position 直接返回一个临时 View 不往下走，destroyItem 拿到这个临时 View 然后 removeView 会正常处理
        if (position < 0 || position >= data.size) {
            return View(container.context)
        }
        val (img, imageUrl) = data.safeGet(position) ?: return View(container.context)
        ImageLoader.instance.loadImageFromUrl(img, imageUrl, onLoadStart = {
            img.invisible()
        }, onLoadComplete = {
            img.appear(1000)
        })
        container.addView(img, MATCH_PARENT, MATCH_PARENT)
        return img
    }

}