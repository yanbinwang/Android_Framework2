package com.example.gallery.base.bridge

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.widget.AppToolbar
import com.example.gallery.R

/**
 * 视图载体抽象基类
 * 用于统一封装 Activity / View 的公共能力，为 BaseView 提供页面支撑
 * @param mHost 载体宿主（Activity / View）
 */
class BaseSource(private val mHost: AppCompatActivity) {
    // 页面标题栏 Toolbar
    private lateinit var mActionBar: AppToolbar

    /**
     * 获取标题控件
     */
    fun getToolbar(): AppToolbar {
        return mActionBar
    }

    /**
     * 获取生命周期订阅 Observer
     */
    fun getObserver(): LifecycleOwner {
        return mHost
    }

    /**
     * 获取上下文
     */
    fun getContext(): Context {
        return mHost
    }

    /**
     * 初始化准备工作（如：绑定Toolbar、初始化视图）
     * BaseView 层在构造方法的实现里先调取 prepare() 再调取 mSource.onCreateOptionsMenu()
     */
    fun prepare() {
        mActionBar = mHost.findViewById(R.id.toolbar)
        mActionBar.bind(mHost)
    }

}