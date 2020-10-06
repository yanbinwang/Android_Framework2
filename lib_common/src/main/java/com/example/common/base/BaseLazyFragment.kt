package com.example.common.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法
 * 适配器构造方法中加入行为参数BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
 */
abstract class BaseLazyFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {
    private var isLoaded = false//是否被加载

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initEvent()
    }

    override fun onResume() {
        super.onResume()
        if (!isLoaded && !isHidden) {
            initData()
            isLoaded = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }
    // </editor-fold>

}