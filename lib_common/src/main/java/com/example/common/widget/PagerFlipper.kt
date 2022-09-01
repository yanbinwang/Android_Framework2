package com.example.common.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.base.widget.SimpleViewGroup

/**
 * 自定义viewpage2,替换首页或一些底部切换页时使用
 */
class PagerFlipper @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SimpleViewGroup(context, attrs, defStyleAttr) {
    private var pager: ViewPager2? = null//广告容器

    init {
        pager = ViewPager2(context)
        pager?.getChildAt(0)?.overScrollMode = OVER_SCROLL_NEVER //去除水波纹
    }

    override fun drawView() {
        if (onFinish()) addView(pager)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        pager?.adapter = adapter
        pager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager?.offscreenPageLimit = adapter.itemCount //预加载数量
        pager?.isUserInputEnabled = false //禁止左右滑动
    }

    fun registerOnPageChangeCallback(callback: OnPageChangeCallback) = pager?.registerOnPageChangeCallback(callback)

    @JvmOverloads
    fun setCurrentItem(item: Int, smoothScroll: Boolean = false) = pager?.setCurrentItem(item, smoothScroll)

}