package com.example.common.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法
 */
public abstract class BaseLazyFragment<VDB extends ViewDataBinding> extends BaseFragment<VDB> {
    private boolean isVisible, isInitView, isFirstLoad = true;//当前Fragment是否可见,是否与View建立起映射关系,是否是第一次加载数据
    private final String TAG = getClass().getSimpleName().toLowerCase();//额外数据，查看log，观察当前activity是否被销毁

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        log("context" + "   " + TAG);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initEvent();
        initLazyData();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log(TAG);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        log("isVisibleToUser " + isVisibleToUser + "   " + TAG);
        if (isVisibleToUser) {
            isVisible = true;
            initLazyData();
        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void initView() {
        super.initView();
        isInitView = true;
    }

    private void initLazyData() {
        if (isFirstLoad) {
            log("第一次加载 " + " isInitView  " + isInitView + "  isVisible  " + isVisible + "   " + TAG);
        } else {
            log("不是第一次加载" + " isInitView  " + isInitView + "  isVisible  " + isVisible + "   " + TAG);
        }
        if (!isFirstLoad || !isVisible || !isInitView) {
            log("不加载" + "   " + TAG);
            return;
        }

        log("完成数据第一次加载" + "   " + TAG);
        initData();
        isFirstLoad = false;
    }
    // </editor-fold>

}
