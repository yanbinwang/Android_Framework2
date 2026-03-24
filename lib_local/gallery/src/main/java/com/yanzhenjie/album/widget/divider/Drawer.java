package com.yanzhenjie.album.widget.divider;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 列表分割线绘制工具类
 * 负责：在 Item 的 左/上/右/下 四个方向绘制分割线
 * 封装了绘制边界、位置计算、Drawable 渲染逻辑
 */
public class Drawer {
    // 分割线宽度/高度
    private final int mWidth;
    private final int mHeight;
    // 分割线的图片/样式资源
    private final Drawable mDivider;

    /**
     * 构造方法
     *
     * @param divider 分割线 Drawable
     * @param width   分割线宽度
     * @param height  分割线高度
     */
    public Drawer(Drawable divider, int width, int height) {
        this.mDivider = divider;
        this.mWidth = width;
        this.mHeight = height;
    }

    /**
     * 在条目【左侧】绘制分割线
     */
    public void drawLeft(View view, Canvas c) {
        int left = view.getLeft() - mWidth;
        int top = view.getTop() - mHeight;
        int right = left + mWidth;
        int bottom = view.getBottom() + mHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    /**
     * 在条目【顶部】绘制分割线
     */
    public void drawTop(View view, Canvas c) {
        int left = view.getLeft() - mWidth;
        int top = view.getTop() - mHeight;
        int right = view.getRight() + mWidth;
        int bottom = top + mHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    /**
     * 在条目【右侧】绘制分割线
     */
    public void drawRight(View view, Canvas c) {
        int left = view.getRight();
        int top = view.getTop() - mHeight;
        int right = left + mWidth;
        int bottom = view.getBottom() + mHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    /**
     * 在条目【底部】绘制分割线
     */
    public void drawBottom(View view, Canvas c) {
        int left = view.getLeft() - mWidth;
        int top = view.getBottom();
        int right = view.getRight() + mWidth;
        int bottom = top + mHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

}