package com.example.common.widget.xrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * author: wyb
 * date: 2017/8/25.
 * 带检测数据是否为空的recyclerview
 */
public class DetectionRecyclerView extends RecyclerView {
    private View mEmptyView;//adapter没有数据的时候显示,类似于listView的emptyView

    public DetectionRecyclerView(Context context) {
        this(context, null);
    }

    public DetectionRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetectionRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //设置数据为空时候显示的view
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        checkIfEmpty();
    }

    //检测内容是否为空
    private void checkIfEmpty() {
        if (mEmptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            mEmptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    //当给与recyclerview内容时做检测
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

}
