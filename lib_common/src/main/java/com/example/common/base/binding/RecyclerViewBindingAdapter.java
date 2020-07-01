package com.example.common.base.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.widget.xrecyclerview.XRecyclerView;
import com.example.common.widget.xrecyclerview.callback.OnRefreshListener;

/**
 * Create by wyb at 20/4/18
 * 定义一些默认的属性，最好控制在一个传入变量，多了不如直接写方法调用
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

    @BindingAdapter(value = {"app:adapter"})
    public static void setAdapter(XRecyclerView recyclerView, RecyclerView.Adapter adapter) {
        if (recyclerView != null && adapter != null) {
            recyclerView.getRecyclerView().setAdapter(adapter);
        }
    }

//    @BindingAdapter(value = {"app:horizontalSpace", "app:verticalSpace", "app:hasHorizontalEdge", "app:hasVerticalEdge"})
//    public static void setItemDecoration(XRecyclerView recyclerView, int horizontalSpace, int verticalSpace, boolean hasHorizontalEdge, boolean hasVerticalEdge) {
//        if (recyclerView != null) {
//            recyclerView.addItemDecoration(horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge);
//        }
//    }

    @BindingAdapter(value = {"app:itemNormalSpace"})
    public static void setNormalItemDecoration(XRecyclerView recyclerView, int itemNormalSpace) {
        if (recyclerView != null) {
            recyclerView.addItemDecoration(itemNormalSpace, 0, true, false);
        }
    }

    @BindingAdapter(value = {"app:itemAroundSpace"})
    public static void setAroundItemDecoration(XRecyclerView recyclerView, int itemAroundSpace) {
        if (recyclerView != null) {
            recyclerView.addItemDecoration(itemAroundSpace, itemAroundSpace, true, true);
        }
    }

    @BindingAdapter(value = {"app:emptyBackgroundColor"})
    public static void setEmptyBackgroundColor(XRecyclerView recyclerView, int color) {
        if (recyclerView != null) {
            recyclerView.setEmptyBackgroundColor(color);
        }
    }

    @BindingAdapter(value = {"app:refreshListener"})
    public static void setOnRefreshListener(XRecyclerView recyclerView, OnRefreshListener onRefreshListener) {
        if (recyclerView != null) {
            recyclerView.setOnRefreshListener(onRefreshListener);
        }
    }

}
