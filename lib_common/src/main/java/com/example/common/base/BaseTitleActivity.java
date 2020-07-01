package com.example.common.base;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.example.common.R;
import com.example.common.databinding.ActivityBaseBinding;
import com.example.common.utils.bulider.TitleBuilder;

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 */
public abstract class BaseTitleActivity<VDB extends ViewDataBinding> extends BaseActivity<VDB> {
    protected TitleBuilder titleBuilder;//标题工具类

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    @Override
    public void initView() {
        super.initView();
        if (0 != getLayoutResID()) {
            binding = DataBindingUtil.bind(getLayoutInflater().inflate(getLayoutResID(), null));
            binding.setLifecycleOwner(this);
            ActivityBaseBinding baseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base);
            baseBinding.flBaseContainer.addView(binding.getRoot());
        }
        titleBuilder = new TitleBuilder(this);
    }
    // </editor-fold>

}
