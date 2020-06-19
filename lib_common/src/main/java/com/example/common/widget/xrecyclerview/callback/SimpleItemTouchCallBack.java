package com.example.common.widget.xrecyclerview.callback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.widget.xrecyclerview.callback.OnItemTouchListener;

/**
 * author:wyb
 */
public class SimpleItemTouchCallBack extends ItemTouchHelper.Callback {
    private OnItemTouchListener mCallBack;
    private boolean mSwipeEnable = true;//左滑删除的

    public SimpleItemTouchCallBack(OnItemTouchListener mCallBack) {
        this.mCallBack = mCallBack;
    }

    /**
     * 返回可以滑动的方向,一般使用makeMovementFlags(int,int)
     * 或makeFlag(int, int)来构造我们的返回值
     *
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        //允许上下拖拽
        int drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        //允许向左滑动
        int swipe = ItemTouchHelper.LEFT;
        //设置
        return makeMovementFlags(drag, swipe);
    }

    /**
     * 上下拖动item时回调,可以调用Adapter的notifyItemMoved方法来交换两个ViewHolder的位置，
     * 最后返回true，
     * 表示被拖动的ViewHolder已经移动到了目的位置
     *
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        //通知适配器,两个子条目位置发生改变
        mCallBack.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * 当用户左右滑动item时达到删除条件就会调用,一般为一半,条目继续滑动删除,否则弹回
     *
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mCallBack.onItemDelete(viewHolder.getAdapterPosition());
    }

    /**
     * 支持长按拖动,默认是true
     *
     * @return
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return super.isLongPressDragEnabled();
    }

    /**
     * 支持滑动,即可以调用到onSwiped()方法,默认是true
     *
     * @return
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return mSwipeEnable;
    }

    /**
     * 设置是否支持左滑删除
     *
     * @param enable
     */
    public void setmSwipeEnable(boolean enable) {
        this.mSwipeEnable = enable;
    }

}
