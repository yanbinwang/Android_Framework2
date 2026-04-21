package com.example.album.widget.recyclerview.divider

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toDrawable

/**
 * 纯色分割线绘制器
 * 继承自 Drawer，专门用于绘制【纯色】分割线
 * 会自动把颜色处理为不透明（去除透明度）
 * @param color  分割线颜色（会自动转为不透明）
 * @param width  分割线宽度
 * @param height 分割线高度
 */
class ColorDrawer(@ColorInt color: Int, width: Int, height: Int) : Drawer(opaqueColor(color).toDrawable(), width, height) {

    companion object {
        /**
         * 将颜色处理为【完全不透明】 强制把透明度 alpha 设置为 255
         * @param color 原始颜色
         * @return 不透明的纯色
         */
        @ColorInt
        fun opaqueColor(@ColorInt color: Int): Int {
            val alpha = Color.alpha(color)
            // 如果已经透明，直接返回
            if (alpha == 0) return color
            // 取出 RGB 分量
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            // 强制设置透明度为 255（不透明）
            return Color.argb(255, red, green, blue)
        }
    }

}