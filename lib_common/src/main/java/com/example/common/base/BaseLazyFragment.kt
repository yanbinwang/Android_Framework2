package com.example.common.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法
 * 使用时需要用于判断生命周期是否展示用onHiddenChanged方法是否是false判断
 */
abstract class BaseLazyFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {
    private var isLoaded = false//是否被加载

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lazyData = true
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!isLoaded && !isHidden) {
            isLoaded = true
            initData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }
    // </editor-fold>

}