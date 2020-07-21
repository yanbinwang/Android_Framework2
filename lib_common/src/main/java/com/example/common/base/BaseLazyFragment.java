package com.example.common.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法
 * 适配器构造方法中加入行为参数BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
 */
public abstract class BaseLazyFragment<VDB extends ViewDataBinding> extends BaseFragment<VDB> {
    private boolean isLoaded;//是否被加载

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded && !isHidden()) {
            initData();
            isLoaded = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoaded = false;
    }
    // </editor-fold>

}
