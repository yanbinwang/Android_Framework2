package com.example.common.base.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Create by wyb at 20/4/18
 */
public class RecyclerViewBindingAdapter {

    @BindingAdapter(value = {"adapter"})
    public static void setAdapter(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        if (recyclerView != null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    @BindingAdapter(value = {"refreshList"})
    public static void refreshList(RecyclerView recyclerView, List list) {
        if (recyclerView != null && list != null) {
            ((BaseBindingAdapter) recyclerView.getAdapter()).setList(list);
        }
    }
}
