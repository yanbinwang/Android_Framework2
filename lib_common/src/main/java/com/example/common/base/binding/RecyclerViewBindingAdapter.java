package com.example.common.base.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.common.widget.xrecyclerview.XRecyclerView;
import com.example.common.widget.xrecyclerview.callback.OnRefreshListener;

import java.util.List;

/**
 * Create by wyb at 20/4/18
 */
public class RecyclerViewBindingAdapter {

//    @BindingAdapter(value = {"adapter"})
//    public static void setAdapter(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
//        if (recyclerView != null && adapter != null) {
//            recyclerView.setAdapter(adapter);
//        }
//    }
//
//    @BindingAdapter(value = {"listItem"})
//    public static void setList(RecyclerView recyclerView, List list) {
//        if (recyclerView != null && list != null) {
//            ((BaseQuickAdapter) recyclerView.getAdapter()).setList(list);
//        }
//    }
//
//    @BindingAdapter(value = {"listItem"})
//    public static void setList(XRecyclerView recyclerView, List list) {
//        if (recyclerView != null && list != null) {
//            ((BaseQuickAdapter) recyclerView.getRecyclerView().getAdapter()).setList(list);
//        }
//    }

    @BindingAdapter(value = {"adapter"})
    public static void setAdapter(XRecyclerView recyclerView, RecyclerView.Adapter adapter) {
        if (recyclerView != null && adapter != null) {
            recyclerView.getRecyclerView().setAdapter(adapter);
        }
    }

    @BindingAdapter(value = {"horizontalSpace", "verticalSpace", "hasHorizontalEdge", "hasVerticalEdge"})
    public static void setItemDecoration(XRecyclerView recyclerView, int horizontalSpace, int verticalSpace, boolean hasHorizontalEdge, boolean hasVerticalEdge) {
        if (recyclerView != null) {
            recyclerView.addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge);
        }
    }

    @BindingAdapter(value = {"emptyBackgroundColor"})
    public static void setEmptyBackgroundColor(XRecyclerView recyclerView, int color) {
        if (recyclerView != null) {
            recyclerView.setEmptyBackgroundColor(color);
        }
    }

    @BindingAdapter(value = {"refreshListener"})
    public static void setOnRefreshListener(XRecyclerView recyclerView, OnRefreshListener onRefreshListener) {
        if (recyclerView != null) {
            recyclerView.setOnRefreshListener(onRefreshListener);
        }
    }

}
