package com.example.gallery.feature.album.widget.recyclerview.divider

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.withSave
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Android 5.0+ 专用的简单分割线
 * 功能：给每个条目四周都绘制均匀的分割线（全屏网格样式）
 */
class ItemDivider : Divider {
    // 分割线宽度/高度
    private var mWidth = 0
    private var mHeight = 0
    // 分割线绘制器
    private var mDrawer: Drawer? = null

    /**
     * 构造方法：使用默认宽高 4px
     * @param color 分割线颜色
     */
    constructor(@ColorInt color: Int) : this(color, 4, 4)

    /**
     * 构造方法：自定义宽高
     * @param color  分割线颜色
     * @param width  分割线总宽度
     * @param height 分割线总高度
     */
    constructor(@ColorInt color: Int, width: Int, height: Int) {
        // 宽高取一半，让分割线均匀分布在条目四周
        mWidth = (width / 2f).roundToInt()
        mHeight = (height / 2f).roundToInt()
        // 创建纯色分割线绘制器
        mDrawer = ColorDrawer(color, mWidth, mHeight)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // 左、上、右、下 都留出分割线空间
        outRect.set(mWidth, mHeight, mWidth, mHeight)
    }

    /**
     * 给所有条目绘制：左、上、右、下 四周分割线
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.withSave {
            parent.layoutManager?.let { layoutManager ->
                val childCount = layoutManager.childCount
                // 遍历所有可见条目，绘制四周分割线
                for (i in 0..<childCount) {
                    layoutManager.getChildAt(i)?.let { view ->
                        mDrawer?.drawLeft(view, this)
                        mDrawer?.drawTop(view, this)
                        mDrawer?.drawRight(view, this)
                        mDrawer?.drawBottom(view, this)
                    }
                }
            }
        }
    }

    /**
     * 获取分割线宽度
     */
    override fun getWidth(): Int {
        return mWidth
    }

    /**
     * 获取分割线高度
     */
    override fun getHeight(): Int {
        return mHeight
    }

}