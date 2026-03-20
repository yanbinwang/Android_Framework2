package com.yanzhenjie.album.widget.divider;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 通用列表分割线（兼容线性/网格/瀑布流布局）
 * 特点：不会在列表最外层绘制多余的分割线，只在条目之间绘制
 * 适配 Android 所有版本
 */
public class Api20ItemDivider extends Divider {
    // 分割线宽度/高度（实际一半）
    private final int mWidth;
    private final int mHeight;
    // 分割线绘制器
    private final Drawer mDrawer;

    /**
     * 构造方法：使用默认宽高（4px）
     *
     * @param color 分割线颜色
     */
    public Api20ItemDivider(@ColorInt int color) {
        this(color, 4, 4);
    }

    /**
     * 构造方法：自定义宽高
     *
     * @param color  分割线颜色
     * @param width  分割线总宽度
     * @param height 分割线总高度
     */
    public Api20ItemDivider(@ColorInt int color, int width, int height) {
        // 宽高取一半，让分割线均匀分布在两个条目之间
        this.mWidth = Math.round(width / 2F);
        this.mHeight = Math.round(height / 2F);
        // 创建纯色绘制器
        this.mDrawer = new ColorDrawer(color, mWidth, mHeight);
    }

    /**
     * 设置条目偏移量（给分割线留出空间）
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            // 线性/网格布局
            int orientation = getOrientation(layoutManager);
            int position = parent.getChildLayoutPosition(view);
            int spanCount = getSpanCount(layoutManager);
            int childCount = layoutManager.getItemCount();
            if (orientation == RecyclerView.VERTICAL) {
                // 垂直列表
                offsetVertical(outRect, position, spanCount, childCount);
            } else {
                // 水平列表
                offsetHorizontal(outRect, position, spanCount, childCount);
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            // 瀑布流：四周都留偏移
            outRect.set(mWidth, mHeight, mWidth, mHeight);
        }
    }

    /**
     * 计算【水平列表】条目偏移
     */
    private void offsetHorizontal(Rect outRect, int position, int spanCount, int childCount) {
        boolean firstRaw = isFirstRaw(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean lastRaw = isLastRaw(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean firstColumn = isFirstColumn(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean lastColumn = isLastColumn(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        // 单列列表
        if (spanCount == 1) {
            if (firstColumn && lastColumn) {
                outRect.set(0, 0, 0, 0);
            } else if (firstColumn) { // xx|x
                outRect.set(0, 0, mWidth, 0);
            } else if (lastColumn) { // |xxx
                outRect.set(mWidth, 0, 0, 0);
            } else { // |x|x
                outRect.set(mWidth, 0, mWidth, 0);
            }
        } else {
            // 多列网格
            if (firstColumn && firstRaw) {
                outRect.set(0, 0, mWidth, mHeight);
            } else if (firstColumn && lastRaw) {
                outRect.set(0, mHeight, mWidth, 0);
            } else if (lastColumn && firstRaw) {
                outRect.set(mWidth, 0, 0, mHeight);
            } else if (lastColumn && lastRaw) {
                outRect.set(mWidth, mHeight, 0, 0);
            } else if (firstColumn) {
                outRect.set(0, mHeight, mWidth, mHeight);
            } else if (lastColumn) {
                outRect.set(mWidth, mHeight, 0, mHeight);
            } else if (firstRaw) {
                outRect.set(mWidth, 0, mWidth, mHeight);
            } else if (lastRaw) {
                outRect.set(mWidth, mHeight, mWidth, 0);
            } else {
                outRect.set(mWidth, mHeight, mWidth, mHeight);
            }
        }
    }

    /**
     * 计算【垂直列表】条目偏移
     */
    private void offsetVertical(Rect outRect, int position, int spanCount, int childCount) {
        boolean firstRaw = isFirstRaw(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean lastRaw = isLastRaw(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean firstColumn = isFirstColumn(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean lastColumn = isLastColumn(RecyclerView.VERTICAL, position, spanCount, childCount);
        // 单列列表
        if (spanCount == 1) {
            if (firstRaw && lastRaw) {
                outRect.set(0, 0, 0, 0);
            } else if (firstRaw) {
                outRect.set(0, 0, 0, mHeight);
            } else if (lastRaw) {
                outRect.set(0, mHeight, 0, 0);
            } else {
                outRect.set(0, mHeight, 0, mHeight);
            }
        } else {
            // 多列网格
            if (firstRaw && firstColumn) {
                outRect.set(0, 0, mWidth, mHeight);
            } else if (firstRaw && lastColumn) {
                outRect.set(mWidth, 0, 0, mHeight);
            } else if (lastRaw && firstColumn) {
                outRect.set(0, mHeight, mWidth, 0);
            } else if (lastRaw && lastColumn) {
                outRect.set(mWidth, mHeight, 0, 0);
            } else if (firstRaw) {
                outRect.set(mWidth, 0, mWidth, mHeight);
            } else if (lastRaw) {
                outRect.set(mWidth, mHeight, mWidth, 0);
            } else if (firstColumn) {
                outRect.set(0, mHeight, mWidth, mHeight);
            } else if (lastColumn) {
                outRect.set(mWidth, mHeight, 0, mHeight);
            } else {
                outRect.set(mWidth, mHeight, mWidth, mHeight);
            }
        }
    }

    /**
     * 获取列表方向：垂直/水平
     */
    private int getOrientation(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        }
        return RecyclerView.VERTICAL;
    }

    /**
     * 获取网格列数
     */
    private int getSpanCount(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return 1;
    }

    /**
     * 判断是否是第一行
     */
    private boolean isFirstRaw(int orientation, int position, int columnCount, int childCount) {
        if (orientation == RecyclerView.VERTICAL) {
            return position < columnCount;
        } else {
            if (columnCount == 1) return true;
            return position % columnCount == 0;
        }
    }

    /**
     * 判断是否是最后一行
     */
    private boolean isLastRaw(int orientation, int position, int columnCount, int childCount) {
        if (orientation == RecyclerView.VERTICAL) {
            if (columnCount == 1) {
                return position + 1 == childCount;
            } else {
                int lastRawItemCount = childCount % columnCount;
                int rawCount = (childCount - lastRawItemCount) / columnCount + (lastRawItemCount > 0 ? 1 : 0);
                int rawPositionJudge = (position + 1) % columnCount;
                if (rawPositionJudge == 0) {
                    int positionRaw = (position + 1) / columnCount;
                    return rawCount == positionRaw;
                } else {
                    int rawPosition = (position + 1 - rawPositionJudge) / columnCount + 1;
                    return rawCount == rawPosition;
                }
            }
        } else {
            if (columnCount == 1) return true;
            return (position + 1) % columnCount == 0;
        }
    }

    /**
     * 判断是否是第一列
     */
    private boolean isFirstColumn(int orientation, int position, int columnCount, int childCount) {
        if (orientation == RecyclerView.VERTICAL) {
            if (columnCount == 1) return true;
            return position % columnCount == 0;
        } else {
            return position < columnCount;
        }
    }

    /**
     * 判断是否是最后一列
     */
    private boolean isLastColumn(int orientation, int position, int columnCount, int childCount) {
        if (orientation == RecyclerView.VERTICAL) {
            if (columnCount == 1) return true;
            return (position + 1) % columnCount == 0;
        } else {
            if (columnCount == 1) {
                return position + 1 == childCount;
            } else {
                int lastRawItemCount = childCount % columnCount;
                int rawCount = (childCount - lastRawItemCount) / columnCount + (lastRawItemCount > 0 ? 1 : 0);
                int rawPositionJudge = (position + 1) % columnCount;
                if (rawPositionJudge == 0) {
                    int positionRaw = (position + 1) / columnCount;
                    return rawCount == positionRaw;
                } else {
                    int rawPosition = (position + 1 - rawPositionJudge) / columnCount + 1;
                    return rawCount == rawPosition;
                }
            }
        }
    }

    /**
     * 绘制分割线
     */
    @Override
    public void onDraw(@NonNull Canvas canvas, RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int orientation = getOrientation(layoutManager);
        int spanCount = getSpanCount(layoutManager);
        int childCount = layoutManager.getChildCount();
        if (layoutManager instanceof LinearLayoutManager) {
            canvas.save();
            for (int i = 0; i < childCount; i++) {
                View view = layoutManager.getChildAt(i);
                int position = parent.getChildLayoutPosition(view);
                if (orientation == RecyclerView.VERTICAL) {
                    drawVertical(canvas, view, position, spanCount, childCount);
                } else {
                    drawHorizontal(canvas, view, position, spanCount, childCount);
                }
            }
            canvas.restore();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            // 瀑布流：四周都画
            canvas.save();
            for (int i = 0; i < childCount; i++) {
                View view = layoutManager.getChildAt(i);
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            }
            canvas.restore();
        }
    }

    /**
     * 绘制【水平列表】的分割线
     */
    private void drawHorizontal(Canvas canvas, View view, int position, int spanCount, int childCount) {
        boolean firstRaw = isFirstRaw(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean lastRaw = isLastRaw(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean firstColumn = isFirstColumn(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        boolean lastColumn = isLastColumn(RecyclerView.HORIZONTAL, position, spanCount, childCount);
        if (spanCount == 1) {
            if (firstRaw && lastColumn) {
            } else if (firstColumn) {
                mDrawer.drawRight(view, canvas);
            } else if (lastColumn) {
                mDrawer.drawLeft(view, canvas);
            } else {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawRight(view, canvas);
            }
        } else {
            if (firstColumn && firstRaw) {
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (firstColumn && lastRaw) {
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
            } else if (lastColumn && firstRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastColumn && lastRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
            } else if (firstColumn) {
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastColumn) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (firstRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
            } else {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            }
        }
    }

    /**
     * 绘制【垂直列表】的分割线
     */
    private void drawVertical(Canvas canvas, View view, int position, int spanCount, int childCount) {
        boolean firstRaw = isFirstRaw(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean lastRaw = isLastRaw(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean firstColumn = isFirstColumn(RecyclerView.VERTICAL, position, spanCount, childCount);
        boolean lastColumn = isLastColumn(RecyclerView.VERTICAL, position, spanCount, childCount);
        if (spanCount == 1) {
            if (firstRaw && lastRaw) {
            } else if (firstRaw) {
                mDrawer.drawBottom(view, canvas);
            } else if (lastRaw) {
                mDrawer.drawTop(view, canvas);
            } else {
                mDrawer.drawTop(view, canvas);
                mDrawer.drawBottom(view, canvas);
            }
        } else {
            if (firstRaw && firstColumn) {
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (firstRaw && lastColumn) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastRaw && firstColumn) {
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
            } else if (lastRaw && lastColumn) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
            } else if (firstRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastRaw) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
            } else if (firstColumn) {
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else if (lastColumn) {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawBottom(view, canvas);
            } else {
                mDrawer.drawLeft(view, canvas);
                mDrawer.drawTop(view, canvas);
                mDrawer.drawRight(view, canvas);
                mDrawer.drawBottom(view, canvas);
            }
        }
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

}