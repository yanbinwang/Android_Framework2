package com.example.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.example.common.R;
import com.example.common.base.bridge.BaseViewModel;
import com.example.common.databinding.ActivityBaseBinding;
import com.example.common.utils.TitleBuilder;

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 *
 */
public abstract class BaseTitleActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseActivity<VM, VDB> {
    protected TitleBuilder titleBuilder;//标题工具类

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    public View initDataBinding(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (0 != getLayoutResID()) {
            //绑定的xml作为一个bind持有
            binding = DataBindingUtil.bind(getLayoutInflater().inflate(getLayoutResID(), null));
            binding.setLifecycleOwner(this);
            //根布局包含标题头，将绑定的xml添加进去
            ActivityBaseBinding baseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base);
            baseBinding.flBaseContainer.addView(binding.getRoot());
        }
        return null;
    }

    @Override
    public void initView() {
        super.initView();
        titleBuilder = new TitleBuilder(this);
    }
    // </editor-fold>

}
