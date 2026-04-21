package com.example.gallery.album.widget.recyclerview.divider

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View

/**
 * 列表分割线绘制工具类
 * 负责：在 Item 的 左/上/右/下 四个方向绘制分割线
 * 封装了绘制边界、位置计算、Drawable 渲染逻辑
 * @param divider 分割线 Drawable
 * @param width   分割线宽度
 * @param height  分割线高度
 */
abstract class Drawer(private val divider: Drawable, private val width: Int, private val height: Int) {

    /**
     * 在条目【左侧】绘制分割线
     */
    fun drawLeft(view: View, c: Canvas) {
        val left = view.left - width
        val top = view.top - height
        val right = left + width
        val bottom = view.bottom + height
        divider.setBounds(left, top, right, bottom)
        divider.draw(c)
    }

    /**
     * 在条目【顶部】绘制分割线
     */
    fun drawTop(view: View, c: Canvas) {
        val left = view.left - width
        val top = view.top - height
        val right = view.right + width
        val bottom = top + height
        divider.setBounds(left, top, right, bottom)
        divider.draw(c)
    }

    /**
     * 在条目【右侧】绘制分割线
     */
    fun drawRight(view: View, c: Canvas) {
        val left = view.right
        val top = view.top - height
        val right = left + width
        val bottom = view.bottom + height
        divider.setBounds(left, top, right, bottom)
        divider.draw(c)
    }

    /**
     * 在条目【底部】绘制分割线
     */
    fun drawBottom(view: View, c: Canvas) {
        val left = view.left - width
        val top = view.bottom
        val right = view.right + width
        val bottom = top + height
        divider.setBounds(left, top, right, bottom)
        divider.draw(c)
    }

}