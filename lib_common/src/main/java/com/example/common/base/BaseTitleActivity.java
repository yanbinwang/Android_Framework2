package com.example.common.base;

import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.example.common.databinding.ActivityBaseBinding;
import com.example.common.utils.builder.TitleBuilder;

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 */
public abstract class BaseTitleActivity<VDB extends ViewDataBinding> extends BaseActivity<VDB> {
    protected TitleBuilder titleBuilder;//标题工具类

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    public void setContentView(View view) {
        ActivityBaseBinding baseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        baseBinding.flBaseContainer.addView(binding.getRoot());
        super.setContentView(baseBinding.getRoot());
        titleBuilder = new TitleBuilder(this, baseBinding.titleContainer);
    }
    // </editor-fold>

}
