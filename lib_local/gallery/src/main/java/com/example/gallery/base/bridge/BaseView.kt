package com.example.gallery.base.bridge

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
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
import com.example.gallery.base.bridge.BaseSource

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
        // 初始化载体 -> Source层已执行setActionBar
        mSource.prepare()
        // 初始化菜单
        invalidateOptionsMenu()
        // 设置菜单点击事件
        mSource.setMenuClickListener(object : BaseSource.MenuClickListener {
            override fun onHomeClick() {
                getPresenter().bye()
            }

            override fun onMenuClick(item: MenuItem) {
                if (item.itemId == R.id.home) {
                    if (!onInterceptToolbarBack()) {
                        getPresenter().bye()
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
        view.requestFocus()
        val manager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun closeInputMethod() {
        mSource.closeInputMethod()
    }

//    /**
//     * 设置 ActionBar/Toolbar
//     */
//    protected fun setActionBar(toolbar: Toolbar) {
//        mSource.setActionBar(toolbar)
//        invalidateOptionsMenu()
//    }

    /**
     * 刷新菜单
     */
    protected fun invalidateOptionsMenu() {
        val menu = mSource.getMenu() ?: return
        onCreateOptionsMenu(menu)
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

    protected fun getContext(): Context {
        return mSource.getContext()
    }

    protected fun getResources(): Resources {
        return getContext().resources
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

    fun getPresenter(): Presenter {
        return mPresenter
    }

    /**
     * 返回带粗体、斜体、颜色等富文本样式
     */
    fun getText(@StringRes resId: Int): CharSequence {
        return getContext().getText(resId)
    }

    /**
     * 返回纯文本，不带任何样式
     */
    fun getString(@StringRes resId: Int): String {
        return getContext().getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getContext().getString(resId, *formatArgs)
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable {
        return getContext().drawable(resId).orEmpty()
    }

    @ColorInt
    fun getColor(@ColorRes resId: Int): Int {
        return getContext().color(resId)
    }

    fun getStringArray(@ArrayRes resId: Int): Array<String> {
        return getResources().getStringArray(resId)
    }

    fun getIntArray(@ArrayRes resId: Int): IntArray {
        return getResources().getIntArray(resId)
    }

//    fun showMessageDialog(@StringRes title: Int, @StringRes message: Int) {
//        showMessageDialog(getText(title), getText(message))
//    }
//
//    fun showMessageDialog(@StringRes title: Int, message: CharSequence) {
//        showMessageDialog(getText(title), message)
//    }
//
//    fun showMessageDialog(title: CharSequence, @StringRes message: Int) {
//        showMessageDialog(title, getText(message))
//    }
//
//    fun showMessageDialog(title: CharSequence, message: CharSequence) {
//        val alertDialog = AlertDialog.Builder(getContext())
//            .setTitle(title)
//            .setMessage(message)
//            .setPositiveButton(R.string.album_ok) { _, _ -> }
//            .create()
//        alertDialog.show()
//    }
//
//    fun showConfirmDialog(@StringRes title: Int, @StringRes message: Int, confirmClickListener: OnDialogClickListener) {
//        showConfirmDialog(getText(title), getText(message), confirmClickListener)
//    }
//
//    fun showConfirmDialog(@StringRes title: Int, message: CharSequence, confirmClickListener: OnDialogClickListener) {
//        showConfirmDialog(getText(title), message, confirmClickListener)
//    }
//
//    fun showConfirmDialog(title: CharSequence, @StringRes message: Int, confirmClickListener: OnDialogClickListener) {
//        showConfirmDialog(title, getText(message), confirmClickListener)
//    }
//
//    fun showConfirmDialog(title: CharSequence, message: CharSequence, confirmClickListener: OnDialogClickListener) {
//        val alertDialog = AlertDialog.Builder(getContext())
//            .setTitle(title)
//            .setMessage(message)
//            .setNegativeButton(R.string.album_cancel) { _, _ -> }
//            .setPositiveButton(R.string.album_confirm) { _, which ->
//                confirmClickListener.onClick(which)
//            }
//            .create()
//        alertDialog.show()
//    }
//
//    fun showMessageDialog(@StringRes title: Int, @StringRes message: Int, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
//        showMessageDialog(getText(title), getText(message), cancelClickListener, confirmClickListener)
//    }
//
//    fun showMessageDialog(@StringRes title: Int, message: CharSequence, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
//        showMessageDialog(getText(title), message, cancelClickListener, confirmClickListener)
//    }
//
//    fun showMessageDialog(title: CharSequence, @StringRes message: Int, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
//        showMessageDialog(title, getText(message), cancelClickListener, confirmClickListener)
//    }
//
//    fun showMessageDialog(title: CharSequence, message: CharSequence, cancelClickListener: OnDialogClickListener, confirmClickListener: OnDialogClickListener) {
//        val alertDialog = AlertDialog.Builder(getContext())
//            .setTitle(title)
//            .setMessage(message)
//            .setNegativeButton(R.string.album_cancel) { _, which ->
//                cancelClickListener.onClick(which)
//            }
//            .setPositiveButton(R.string.album_confirm) { _, which ->
//                confirmClickListener.onClick(which)
//            }
//            .create()
//        alertDialog.show()
//    }

    fun toast(@StringRes message: Int) {
        toast(getText(message))
    }

    fun toast(message: CharSequence) {
//        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
        message.toString().shortToast()
    }

//    fun snackBar(@StringRes message: Int) {
//        snackBar(getText(message))
//    }
//
//    fun snackBar(message: CharSequence) {
//        val snackBar = Snackbar.make(mSource.getView(), message, Snackbar.LENGTH_SHORT)
//        val view = snackBar.getView()
//        view.setBackgroundColor(getColor(R.color.albumColorPrimaryBlack).orZero)
//        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//        textView.setTextColor(Color.WHITE)
//        snackBar.show()
//    }

    /**
     * 对话框点击回调接口
     */
    interface OnDialogClickListener {
        fun onClick(which: Int)
    }

}