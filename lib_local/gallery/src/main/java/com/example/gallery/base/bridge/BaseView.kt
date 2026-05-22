package com.example.gallery.base.bridge

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.orEmpty
import com.example.common.widget.AppToolbar
import com.example.framework.utils.function.color
import com.example.framework.utils.function.drawable

/**
 * MVP 架构中所有 View 层的基类
 * 统一管理生命周期、Toolbar、菜单、对话框、Toast、SnackBar、输入法等通用功能
 * 支持绑定 Activity / View / 自定义 Source 作为载体
 * @mSource View 载体（Activity/View 包装类）
 * @mPresenter 当前 View 绑定的 Presenter
 */
abstract class BaseView<Presenter : BasePresenter>(private val mSource: BaseSource, private val mPresenter: Presenter) : DefaultLifecycleObserver {

    /**
     * 初始化
     */
    init {
        // 初始化标题控件载体
        mSource.prepare()
        // 监听 Presenter 生命周期，绑定 View 生命周期
        getPresenter().lifecycle.addObserver(this)
    }

    /**
     * 获取P层
     */
    protected fun getPresenter(): Presenter {
        return mPresenter
    }

    /**
     * 获取基础类型
     */
    protected fun getToolbar(): AppToolbar {
        return mSource.getToolbar()
    }

    protected fun getObserver(): LifecycleOwner {
        return mSource.getObserver()
    }

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

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
    }
    // </editor-fold>

}