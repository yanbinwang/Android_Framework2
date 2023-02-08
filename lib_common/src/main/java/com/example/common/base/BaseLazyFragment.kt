package com.example.common.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.common.bean.interf.FragmentOwner
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

    override fun initView() {
        super.initView()
        if (needFragmentOwner) onHiddenChanged(false)
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
        if (needFragmentOwner) {
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

val BaseLazyFragment<*>.needFragmentOwner get() = hasAnnotation(FragmentOwner::class.java)