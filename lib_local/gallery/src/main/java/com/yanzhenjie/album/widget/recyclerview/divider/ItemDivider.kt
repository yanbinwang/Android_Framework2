package com.yanzhenjie.album.widget.recyclerview.divider;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Android 5.0+ 专用的简单分割线
 * 功能：给每个条目四周都绘制均匀的分割线（全屏网格样式）
 */
public class ItemDivider extends Divider {
    // 分割线宽度/高度
    private final int mWidth;
    private final int mHeight;
    // 分割线绘制器
    private final Drawer mDrawer;

    /**
     * 构造方法：使用默认宽高 4px
     * @param color 分割线颜色
     */
    public ItemDivider(@ColorInt int color) {
        this(color, 4, 4);
    }

    /**
     * 构造方法：自定义宽高
     * @param color  分割线颜色
     * @param width  分割线总宽度
     * @param height 分割线总高度
     */
    public ItemDivider(@ColorInt int color, int width, int height) {
        // 宽高取一半，让分割线均匀分布在条目四周
        this.mWidth = Math.round(width / 2F);
        this.mHeight = Math.round(height / 2F);
        // 创建纯色分割线绘制器
        this.mDrawer = new ColorDrawer(color, mWidth, mHeight);
    }

    /**
     * 给所有条目设置相同的四周偏移
     */
    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // 左、上、右、下 都留出分割线空间
        outRect.set(mWidth, mHeight, mWidth, mHeight);
    }

    /**
     * 给所有条目绘制：左、上、右、下 四周分割线
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, @NonNull RecyclerView.State state) {
        canvas.save();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        // 遍历所有可见条目，绘制四周分割线
        for (int i = 0; i < childCount; i++) {
            final View view = layoutManager.getChildAt(i);
            mDrawer.drawLeft(view, canvas);
            mDrawer.drawTop(view, canvas);
            mDrawer.drawRight(view, canvas);
            mDrawer.drawBottom(view, canvas);
        }
        canvas.restore();
    }

    /**
     * 获取分割线高度
     */
    @Override
    public int getHeight() {
        return mHeight;
    }

    /**
     * 获取分割线宽度
     */
    @Override
    public int getWidth() {
        return mWidth;
    }

}