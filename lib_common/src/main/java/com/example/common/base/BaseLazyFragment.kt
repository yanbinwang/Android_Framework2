package com.example.common.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.common.bean.interf.LazyOwner
import com.example.framework.utils.function.value.hasAnnotation

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法
 * 使用时需要用于判断生命周期是否展示用onHiddenChanged方法是否是false判断
 */
abstract class BaseLazyFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {
    private var hasLoad = false//页面是否被加载
    private var canLoad = true//数据是否允许加载
    private var loaded = false//数据是否被加载

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lazyData = true
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (needLazyOwner) onHiddenChanged(false)
    }

    override fun onResume() {
        super.onResume()
        if (!hasLoad) {
            if (canLoad) {
                initData()
                loaded = true
            }
            hasLoad = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (needLazyOwner) {
            if (hidden) onPause() else onResume()
        }
    }

    /**
     * 禁止页面展示后加载数据，使用eventbug去刷新对应接口
     * 适用于Manager管理fragment的情况
     */
    open fun setCanLoadData(flag: Boolean) {
        canLoad = flag
        if (canLoad && hasLoad && !loaded) {
            initData()
            loaded = true
        }
    }
    // </editor-fold>

}

/**
 * 如果activity中的fragment不在onCreate中初始化，而是在onResume中或者接口请求完成后的代码中产生，可能会导致生命周期调用不正常，此时使用此注解
 */
val BaseLazyFragment<*>.needLazyOwner get() = hasAnnotation(LazyOwner::class.java)