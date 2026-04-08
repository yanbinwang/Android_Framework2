package com.example.gallery.base.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.common.utils.function.orEmpty
import com.example.framework.utils.function.drawable
import com.example.gallery.R
import com.example.gallery.base.BaseActivity.Companion.setSupportToolbar
import com.example.gallery.base.bridge.BaseSource

/**
 * Activity 载体实现类
 * 用于将 BaseView 与 Activity 绑定，提供页面、Toolbar、菜单、输入法等能力
 */
@SuppressLint("RestrictedApi")
class ActivitySource(activity: Activity) : BaseSource<Activity>(activity) {
    // 页面标题栏 Toolbar
    private lateinit var mActionBar: Toolbar
    // 导航返回图标
    private var mActionBarIcon: Drawable? = null
    // 菜单/返回按钮点击监听
    private var mMenuItemSelectedListener: MenuClickListener? = null
    // 页面根视图
    private val mView = activity.findViewById<View>(R.id.content)

    /**
     * 设置是否显示返回按钮
     */
    override fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        if (showHome) {
            mActionBar.setNavigationIcon(mActionBarIcon)
        } else {
            mActionBar.setNavigationIcon(null)
        }
    }

    /**
     * 设置返回图标（资源ID/Drawable）
     */
    override fun setHomeAsUpIndicator(id: Int) {
        setHomeAsUpIndicator(getContext().drawable(id).orEmpty())
    }

    override fun setHomeAsUpIndicator(icon: Drawable) {
        mActionBarIcon = icon
        mActionBar.setNavigationIcon(icon)
    }

    /**
     * 设置菜单/返回按钮监听
     */
    override fun setMenuClickListener(listener: MenuClickListener) {
        mMenuItemSelectedListener = listener
    }

    /**
     * 获取上下文
     */
    override fun getContext(): Context {
        return mHost
    }

    /**
     * 获取主视图
     */
    override fun getView(): View {
        return mView
    }

    /**
     * 获取菜单加载器
     */
    override fun getMenuInflater(): MenuInflater {
        return SupportMenuInflater(getContext())
    }

    /**
     * 获取 Toolbar 菜单
     */
    override fun getMenu(): Menu {
        return mActionBar.menu
    }

    /**
     * 初始化：自动查找并绑定 Toolbar
     * 1) BaseView 层在构造方法的实现里先调取 prepare() 再调取 mSource.getMenu()
     * 2) mSource.getMenu() 获取的是 Toolbar 的菜单 , Toolbar 并未走系统的 setSupportActionBar() 方法,属于自己创建菜单
     * 3) 由于未调取系统 setSupportActionBar() 方法,故而页面的 onCreateOptionsMenu() 是不会执行的 , 由 BaseView 层的构造方法内执行
     */
    override fun prepare() {
        // 全局 Toolbar 使用了 lateinit 此处必定赋值,对应页面也必须写 Toolbar
        val toolbar = mHost.findViewById<Toolbar>(R.id.toolbar)
        setSupportToolbar(toolbar)
        mActionBar = toolbar
        // 菜单点击
        mActionBar.setOnMenuItemClickListener {
            mMenuItemSelectedListener?.onMenuClick(it)
            true
        }
        // 返回按钮点击
        mActionBar.setNavigationOnClickListener {
            mMenuItemSelectedListener?.onHomeClick()
        }
        // 保存默认返回图标
        mActionBarIcon = mActionBar.navigationIcon
    }

    /**
     * 关闭输入法
     */
    override fun openInputMethod(view: View) {
        view.requestFocus()
        val manager = ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        manager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun closeInputMethod() {
        val manager = ContextCompat.getSystemService(mHost, InputMethodManager::class.java)
        manager?.hideSoftInputFromWindow(mHost.currentFocus?.windowToken, 0)
    }

}