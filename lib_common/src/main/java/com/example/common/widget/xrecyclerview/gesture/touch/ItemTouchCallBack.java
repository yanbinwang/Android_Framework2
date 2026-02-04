package com.example.common.widget.xrecyclerview.gesture.touch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 用于RecyclerView的适配器
 */
public class ItemTouchCallBack extends ItemTouchHelper.Callback {
    private boolean mSwipeEnable = true; // 是否允许左滑删除
    private final OnItemTouchListener mCallBack;

    public ItemTouchCallBack(OnItemTouchListener mCallBack) {
        this.mCallBack = mCallBack;
    }

    /**
     * 返回可以滑动的方向,一般使用makeMovementFlags(int,int)
     * 或makeFlag(int, int)来构造我们的返回值
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 允许上下拖拽
        int drag = androidx.recyclerview.widget.ItemTouchHelper.UP | androidx.recyclerview.widget.ItemTouchHelper.DOWN;
        // 允许向左滑动
        int swipe = androidx.recyclerview.widget.ItemTouchHelper.LEFT;
        // 设置
        return makeMovementFlags(drag, swipe);
    }

    /**
     * 上下拖动item时回调,可以调用Adapter的notifyItemMoved方法来交换两个ViewHolder的位置，
     * 最后返回true，
     * 表示被拖动的ViewHolder已经移动到了目的位置
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 通知适配器,两个子条目位置发生改变
        mCallBack.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * 当用户左右滑动item时达到删除条件就会调用,一般为一半,条目继续滑动删除,否则弹回
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mCallBack.onItemDelete(viewHolder.getAdapterPosition());
    }

    /**
     * 支持长按拖动,默认是true
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return super.isLongPressDragEnabled();
    }

    /**
     * 支持滑动,即可以调用到onSwiped()方法,默认是true
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return mSwipeEnable;
    }

    /**
     * 设置是否支持左滑删除
     */
    public void setmSwipeEnable(boolean enable) {
        this.mSwipeEnable = enable;
    }

    /**
     * RecyclerView的Adapter适配器使用,继承并重写
     */
    public interface OnItemTouchListener {
        /**
         * 数据交换 -> move(fromPosition, toPosition)
         */
        void onItemMove(int fromPosition, int toPosition);

        /**
         * 数据删除 -> removed(position)
         */
        void onItemDelete(int position);
    }

}