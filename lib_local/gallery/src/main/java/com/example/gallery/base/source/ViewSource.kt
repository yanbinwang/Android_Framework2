package com.example.gallery.base.source

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.Toolbar
import com.example.common.utils.function.orEmpty
import com.example.framework.utils.function.drawable
import com.example.gallery.R
import com.example.gallery.base.BaseActivity.Companion.setSupportToolbar

/**
 * 普通 View 载体实现类
 * 用于将 BaseView 与 View 绑定，提供视图、Toolbar、菜单、输入法等能力
 * 适用于非 Activity 场景（如 Fragment、Dialog、自定义View 中使用）
 */
@SuppressLint("RestrictedApi")
class ViewSource(view: View) : Source<View>(view) {
    // 标题栏 Toolbar
    private var mActionBar: Toolbar? = null
    // 导航返回图标
    private var mActionBarIcon: Drawable? = null
    // 菜单/返回按钮点击监听
    private var mMenuItemSelectedListener: MenuClickListener? = null

    /**
     * 设置 Toolbar 并绑定点击事件
     */
    override fun setActionBar(toolbar: Toolbar) {
        setSupportToolbar(toolbar)
        mActionBar = toolbar
        // 菜单点击
        mActionBar?.setOnMenuItemClickListener {
            mMenuItemSelectedListener?.onMenuClick(it)
            true
        }
        // 返回按钮点击
        mActionBar?.setNavigationOnClickListener {
            mMenuItemSelectedListener?.onHomeClick()
        }
        // 保存默认返回图标
        mActionBarIcon = mActionBar?.navigationIcon
    }

    /**
     * 设置是否显示返回按钮
     */
    override fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        if (showHome) {
            mActionBar?.setNavigationIcon(mActionBarIcon)
        } else {
            mActionBar?.setNavigationIcon(null)
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
        mActionBar?.setNavigationIcon(icon)
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
        return mHost.context
    }

    /**
     * 获取当前根视图
     */
    override fun getView(): View {
        return mHost
    }

    /**
     * 获取 Toolbar 菜单
     */
    override fun getMenu(): Menu? {
        return if (mActionBar == null) null else mActionBar?.getMenu()
    }

    /**
     * 获取菜单加载器
     */
    override fun getMenuInflater(): MenuInflater {
        return SupportMenuInflater(getContext())
    }

    /**
     * 初始化：自动查找并绑定 Toolbar
     */
    override fun prepare() {
        setActionBar(mHost.findViewById(R.id.toolbar))
    }

    /**
     * 关闭输入法
     */
    override fun closeInputMethod() {
        val focusView = getView().findFocus()
        val manager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.hideSoftInputFromWindow(focusView?.windowToken, 0)
    }

}