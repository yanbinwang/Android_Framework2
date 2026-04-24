package com.example.gallery.base.bridge

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.orEmpty
import com.example.framework.utils.function.color
import com.example.framework.utils.function.drawable
import com.example.gallery.R
import com.example.gallery.base.proxy.ActivitySource

/**
 * MVP 架构中所有 View 层的基类
 * 统一管理生命周期、Toolbar、菜单、对话框、Toast、SnackBar、输入法等通用功能
 * 支持绑定 Activity / View / 自定义 Source 作为载体
 */
abstract class BaseView<Presenter : BasePresenter> {
    // View 载体（Activity/View 包装类）
    private var mSource: BaseSource<*>
    // 当前 View 绑定的 Presenter
    private var mPresenter: Presenter

    /**
     * 绑定 Activity 作为载体
     * @param activity 页面
     * @param presenter 绑定的 Presenter
     */
    constructor(activity: Activity, presenter: Presenter) : this(ActivitySource(activity), presenter)

    /**
     * 绑定自定义 Source 作为载体
     * @param source 视图载体包装类
     * @param presenter 绑定的 Presenter
     */
    constructor(source: BaseSource<*>, presenter: Presenter) {
        mSource = source
        mPresenter = presenter
        // 初始化载体
        mSource.prepare()
        // 初始化载体右侧菜单
        onCreateOptionsMenu(mSource.getMenu())
        // 设置菜单点击事件
        mSource.setMenuClickListener(object : BaseSource.MenuClickListener {
            override fun onHomeClick() {
                getPresenter().navigateBack()
            }

            override fun onMenuClick(item: MenuItem) {
                if (item.itemId == R.id.home) {
                    if (!onInterceptToolbarBack()) {
                        getPresenter().navigateBack()
                    }
                } else {
                    onOptionsItemSelected(item)
                }
            }
        })
        // 监听 Presenter 生命周期，绑定 View 生命周期
        getPresenter().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                closeInputMethod()
                onDestroy()
            }
        })
    }

    /**
     * 生命周期（子类重写）
     */
    protected fun onResume() {
    }

    protected fun onPause() {
    }

    protected fun onStop() {
    }

    protected fun onDestroy() {
    }

    /**
     * 打开/关闭输入法
     */
    protected fun openInputMethod(view: View) {
        mSource.openInputMethod(view)
    }

    protected fun closeInputMethod() {
        mSource.closeInputMethod()
    }

    /**
     * 设置是否显示返回按钮
     */
    protected fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        mSource.setDisplayHomeAsUpEnabled(showHome)
    }

    /**
     * 设置返回按钮图标（资源/Drawable）
     */
    protected fun setHomeAsUpIndicator(@DrawableRes id: Int) {
        mSource.setHomeAsUpIndicator(id)
    }

    protected fun setHomeAsUpIndicator(icon: Drawable) {
        mSource.setHomeAsUpIndicator(icon)
    }

    /**
     * 获取菜单加载器
     */
    protected fun getMenuInflater(): MenuInflater {
        return mSource.getMenuInflater()
    }

    /**
     * 获取基础类型
     */
    protected fun getContext(): Context {
        return mSource.getContext()
    }

    protected fun getResources(): Resources {
        return getContext().resources
    }

    @ColorInt
    protected fun getColor(@ColorRes resId: Int): Int {
        return getContext().color(resId)
    }

    protected fun getDrawable(@DrawableRes resId: Int): Drawable {
        return getContext().drawable(resId).orEmpty()
    }

    /**
     * 返回纯文本，不带任何样式
     */
    protected fun getString(@StringRes resId: Int): String {
        return getContext().getString(resId)
    }

    protected fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getContext().getString(resId, *formatArgs)
    }

    /**
     * 获取P层
     */
    protected fun getPresenter(): Presenter {
        return mPresenter
    }

    /**
     * 菜单点击事件（子类可重写）
     */
    protected open fun onOptionsItemSelected(item: MenuItem) {
    }

    /**
     * 创建菜单（子类可重写）
     */
    protected open fun onCreateOptionsMenu(menu: Menu) {
    }

    /**
     * 拦截 Toolbar 返回按钮点击事件
     * @return true 表示拦截，false 不拦截
     */
    protected open fun onInterceptToolbarBack(): Boolean {
        return false
    }

    /**
     * 全局Toast
     */
    fun toast(@StringRes message: Int) {
        toast(getString(message))
    }

    fun toast(message: CharSequence) {
        message.toString().shortToast()
    }

}