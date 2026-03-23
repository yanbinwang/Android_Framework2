package com.example.gallery.base.bridge

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.orZero
import com.example.gallery.R
import com.example.gallery.base.source.ActivitySource
import com.example.gallery.base.source.Source
import com.example.gallery.base.source.ViewSource
import com.google.android.material.snackbar.Snackbar

/**
 * MVP 架构中所有 View 层的基类
 * 统一管理生命周期、Toolbar、菜单、对话框、Toast、SnackBar、输入法等通用功能
 * 支持绑定 Activity / View / 自定义 Source 作为载体
 */
abstract class BaseView<Presenter : BasePresenter> {
    // View 载体（Activity/View 包装类）
    private var mSource: Source<*>? = null
    // 当前 View 绑定的 Presenter
    private var mPresenter: Presenter? = null

    /**
     * 绑定 Activity 作为载体
     * @param activity 页面
     * @param presenter 绑定的 Presenter
     */
    constructor(activity: Activity, presenter: Presenter) : this(ActivitySource(activity), presenter)

    /**
     * 绑定 View 作为载体
     * @param view 视图
     * @param presenter 绑定的 Presenter
     */
    constructor(view: View, presenter: Presenter) : this(ViewSource(view), presenter)

    /**
     * 绑定自定义 Source 作为载体
     * @param source 视图载体包装类
     * @param presenter 绑定的 Presenter
     */
    constructor(source: Source<*>, presenter: Presenter) {
        this.mSource = source
        this.mPresenter = presenter
        // 初始化载体
        this.mSource?.prepare()
        // 初始化菜单
        invalidateOptionsMenu()
        // 设置菜单点击事件
        mSource?.setMenuClickListener(object : Source.MenuClickListener {
            override fun onHomeClick() {
                getPresenter()?.bye()
            }

            override fun onMenuClick(item: MenuItem?) {
                item ?: return
                if (item.itemId == R.id.home) {
                    if (!onInterceptToolbarBack()) {
                        getPresenter()?.bye()
                    }
                } else {
                    onOptionsItemSelected(item)
                }
            }
        })
        // 监听 Presenter 生命周期，绑定 View 生命周期
        getPresenter()?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
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
        view.requestFocus()
        val manager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun closeInputMethod() {
        mSource?.closeInputMethod()
    }

    /**
     * 刷新菜单
     */
    protected fun invalidateOptionsMenu() {
        val menu = mSource?.getMenu() ?: return
        onCreateOptionsMenu(menu)
    }

    /**
     * 设置 ActionBar/Toolbar
     */
    protected fun setActionBar(actionBar: Toolbar) {
        mSource?.setActionBar(actionBar)
        invalidateOptionsMenu()
    }

    /**
     * 设置是否显示返回按钮
     */
    protected fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        mSource?.setDisplayHomeAsUpEnabled(showHome)
    }

    /**
     * 设置返回按钮图标（资源/Drawable）
     */
    protected fun setHomeAsUpIndicator(@DrawableRes icon: Int) {
        mSource?.setHomeAsUpIndicator(icon)
    }

    protected fun setHomeAsUpIndicator(icon: Drawable) {
        mSource?.setHomeAsUpIndicator(icon)
    }

    /**
     * 获取菜单加载器
     */
    protected fun getMenuInflater(): MenuInflater? {
        return mSource?.getMenuInflater()
    }

    protected fun getContext(): Context? {
        return mSource?.getContext()
    }

    protected fun getResources(): Resources? {
        return getContext()?.resources
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
     * 子类获取的参数回调
     */
    fun setTitle(title: String) {
        mSource?.setTitle(title)
    }

    fun setTitle(@StringRes title: Int) {
        mSource?.setTitle(title)
    }

    fun setSubTitle(title: String) {
        mSource?.setSubTitle(title)
    }

    fun setSubTitle(@StringRes title: Int) {
        mSource?.setSubTitle(title)
    }

    fun getPresenter(): Presenter? {
        return mPresenter
    }

    fun getText(@StringRes id: Int): CharSequence? {
        return getContext()?.getText(id)
    }

    fun getString(@StringRes id: Int): String? {
        return getContext()?.getString(id)
    }

    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String? {
        return getContext()?.getString(id, *formatArgs)
    }

    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return getContext()?.let { ContextCompat.getDrawable(it, id) }
    }

    @ColorInt
    fun getColor(@ColorRes id: Int): Int? {
        return getContext()?.let { ContextCompat.getColor(it, id) }
    }

    fun getStringArray(@ArrayRes id: Int): Array<String>? {
        return getResources()?.getStringArray(id)
    }

    fun getIntArray(@ArrayRes id: Int): IntArray? {
        return getResources()?.getIntArray(id)
    }

    fun showMessageDialog(@StringRes title: Int, @StringRes message: Int) {
        showMessageDialog(getText(title), getText(message))
    }

    fun showMessageDialog(@StringRes title: Int, message: CharSequence?) {
        showMessageDialog(getText(title), message)
    }

    fun showMessageDialog(title: CharSequence?, @StringRes message: Int) {
        showMessageDialog(title, getText(message))
    }

    fun showMessageDialog(title: CharSequence?, message: CharSequence?) {
        getContext()?.let {
            val alertDialog = AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.album_ok) { _, _ -> }
                .create()
            alertDialog.show()
        }
    }

    fun showConfirmDialog(@StringRes title: Int, @StringRes message: Int, confirmClickListener: OnDialogClickListener) {
        showConfirmDialog(getText(title), getText(message), confirmClickListener)
    }

    fun showConfirmDialog(@StringRes title: Int, message: CharSequence?, confirmClickListener: OnDialogClickListener) {
        showConfirmDialog(getText(title), message, confirmClickListener)
    }

    fun showConfirmDialog(title: CharSequence?, @StringRes message: Int, confirmClickListener: OnDialogClickListener) {
        showConfirmDialog(title, getText(message), confirmClickListener)
    }

    fun showConfirmDialog(title: CharSequence?, message: CharSequence?, confirmClickListener: OnDialogClickListener) {
        getContext()?.let {
            val alertDialog = AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.album_cancel) { _, _ -> }
                .setPositiveButton(R.string.album_confirm) { _, which -> confirmClickListener.onClick(which) }
                .create()
            alertDialog.show()
        }

    }

    fun showMessageDialog(@StringRes title: Int, @StringRes message: Int, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
        showMessageDialog(getText(title), getText(message), cancelClickListener, confirmClickListener)
    }

    fun showMessageDialog(@StringRes title: Int, message: CharSequence?, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
        showMessageDialog(getText(title), message, cancelClickListener, confirmClickListener)
    }

    fun showMessageDialog(title: CharSequence?, @StringRes message: Int, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
        showMessageDialog(title, getText(message), cancelClickListener, confirmClickListener)
    }

    fun showMessageDialog(title: CharSequence?, message: CharSequence?, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
        getContext()?.let {
            val alertDialog = AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.album_cancel) { _, which -> cancelClickListener.onClick(which) }
                .setPositiveButton(R.string.album_confirm) { _, which -> confirmClickListener.onClick(which) }
                .create()
            alertDialog.show()
        }
    }

    fun toast(message: CharSequence?) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
    }

    fun toast(@StringRes message: Int) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
    }

    fun snackBar(message: CharSequence?) {
        message ?: return
        mSource?.getView()?.let {
            val snackBar = Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
            val view = snackBar.getView()
            view.setBackgroundColor(getColor(R.color.albumColorPrimaryBlack).orZero)
            val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            snackBar.show()
        }
    }

    fun snackBar(@StringRes message: Int) {
        mSource?.getView()?.let {
            val snackBar = Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
            val view = snackBar.getView()
            view.setBackgroundColor(getColor(R.color.albumColorPrimaryBlack).orZero)
            val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            snackBar.show()
        }
    }

    /**
     * 对话框点击回调接口
     */
    interface OnDialogClickListener {
        fun onClick(which: Int)
    }

}