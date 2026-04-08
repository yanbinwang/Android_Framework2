package com.example.gallery.base.bridge

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar

/**
 * 视图载体抽象基类
 * 用于统一封装 Activity / View 的公共能力，为 BaseView 提供页面支撑
 * 所有页面载体 ActivitySource 都必须继承此类
 * @param Host 载体宿主（Activity / View）
 */
abstract class BaseSource<Host>(protected val mHost: Host) {

    /**
     * 设置是否显示返回按钮
     */
    abstract fun setDisplayHomeAsUpEnabled(showHome: Boolean)

    /**
     * 设置返回箭头图标（资源ID）
     */
    abstract fun setHomeAsUpIndicator(@DrawableRes id: Int)

    /**
     * 设置返回箭头图标（Drawable）
     */
    abstract fun setHomeAsUpIndicator(icon: Drawable)

    /**
     * 设置菜单/返回按钮点击监听 (代码new监听)
     */
    abstract fun setMenuClickListener(listener: MenuClickListener)

    /**
     * 获取上下文 Context
     */
    abstract fun getContext(): Context

    /**
     * 获取当前视图
     */
    abstract fun getView(): View

    /**
     * 获取菜单加载器 (代码创建SupportMenuInflater)
     */
    abstract fun getMenuInflater(): MenuInflater

    /**
     * 获取菜单对象 (可空)
     */
    abstract fun getMenu(): Menu

    /**
     * 初始化准备工作（如：绑定Toolbar、初始化视图）
     */
    abstract fun prepare()

    /**
     * 打开输入法
     */
    abstract fun openInputMethod(view: View)

    /**
     * 关闭输入法
     */
    abstract fun closeInputMethod()

    /**
     * 菜单 & 返回按钮 点击回调接口
     */
    interface MenuClickListener {
        /**
         * 左侧返回按钮点击
         */
        fun onHomeClick()

        /**
         * 菜单条目点击
         */
        fun onMenuClick(item: MenuItem)
    }

}