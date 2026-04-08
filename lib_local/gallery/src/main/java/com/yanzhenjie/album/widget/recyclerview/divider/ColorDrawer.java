package com.yanzhenjie.album.widget.recyclerview.divider;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;

/**
 * 纯色分割线绘制器
 * 继承自 Drawer，专门用于绘制【纯色】分割线
 * 会自动把颜色处理为不透明（去除透明度）
 */
public class ColorDrawer extends Drawer {

    /**
     * 构造方法：创建纯色分割线
     * @param color  分割线颜色（会自动转为不透明）
     * @param width  分割线宽度
     * @param height 分割线高度
     */
    public ColorDrawer(int color, int width, int height) {
        // 把颜色包装成不透明的 ColorDrawable
        super(new ColorDrawable(opaqueColor(color)), width, height);
    }

    /**
     * 将颜色处理为【完全不透明】
     * 强制把透明度 alpha 设置为 255
     *
     * @param color 原始颜色
     * @return 不透明的纯色
     */
    @ColorInt
    public static int opaqueColor(@ColorInt int color) {
        int alpha = Color.alpha(color);
        // 如果已经透明，直接返回
        if (alpha == 0) return color;
        // 取出 RGB 分量
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        // 强制设置透明度为 255（不透明）
        return Color.argb(255, red, green, blue);
    }

}