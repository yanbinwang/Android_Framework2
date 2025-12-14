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
 * <p>View of MVP.</p>
 * Created by YanZhenjie on 2017/7/17.
 */
abstract class BaseView<Presenter : BasePresenter> {
    private var mSource: Source<*>? = null
    private var mPresenter: Presenter? = null

    constructor(activity: Activity, presenter: Presenter) : this(ActivitySource(activity), presenter)

    constructor(view: View, presenter: Presenter) : this(ViewSource(view), presenter)

    constructor(source: Source<*>, presenter: Presenter) {
        this.mSource = source
        this.mPresenter = presenter
        this.mSource?.prepare()
        invalidateOptionsMenu()
        mSource?.setMenuClickListener(object : Source.MenuClickListener {
            override fun onHomeClick() {
                getPresenter()?.bye()
            }

            override fun onMenuClick(item: MenuItem?) {
                optionsItemSelected(item)
            }
        })
        getPresenter()?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                resume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                pause()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                stop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                destroy()
            }
        })
    }

    private fun resume() {
        onResume()
    }

    private fun pause() {
        onPause()
    }

    private fun stop() {
        onStop()
    }

    private fun destroy() {
        closeInputMethod()
        onDestroy()
    }

    private fun optionsItemSelected(item: MenuItem?) {
        if (item?.itemId == R.id.home) {
            if (!onInterceptToolbarBack()) {
                getPresenter()?.bye()
            }
        } else {
            onOptionsItemSelected(item)
        }
    }

    /**
     * Create menu.
     */
    protected open fun onCreateOptionsMenu(menu: Menu?) {
    }

    /**
     * When the menu is clicked.
     */
    protected open fun onOptionsItemSelected(item: MenuItem?) {
    }

    /**
     * Intercept the return button.
     */
    protected open fun onInterceptToolbarBack(): Boolean {
        return false
    }

    /**
     * 生命周期
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
     * Set actionBar.
     */
    protected fun setActionBar(actionBar: Toolbar?) {
        mSource?.setActionBar(actionBar)
        invalidateOptionsMenu()
    }

    /**
     * ReCreate menu.
     */
    protected fun invalidateOptionsMenu() {
        val menu = mSource?.getMenu()
        if (menu != null) {
            onCreateOptionsMenu(menu)
        }
    }

    /**
     * Get menu inflater.
     */
    protected fun getMenuInflater(): MenuInflater? {
        return mSource?.getMenuInflater()
    }

    protected fun openInputMethod(view: View) {
        view.requestFocus()
        val manager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun closeInputMethod() {
        mSource?.closeInputMethod()
    }

    protected fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        mSource?.setDisplayHomeAsUpEnabled(showHome)
    }

    protected fun setHomeAsUpIndicator(@DrawableRes icon: Int) {
        mSource?.setHomeAsUpIndicator(icon)
    }

    protected fun setHomeAsUpIndicator(icon: Drawable?) {
        mSource?.setHomeAsUpIndicator(icon)
    }

    protected fun getContext(): Context? {
        return mSource?.getContext()
    }

    protected fun getResources(): Resources? {
        return getContext()?.resources
    }

    fun getPresenter(): Presenter? {
        return mPresenter
    }

    fun setTitle(title: String?) {
        mSource?.setTitle(title)
    }

    fun setTitle(@StringRes title: Int) {
        mSource?.setTitle(title)
    }

    fun setSubTitle(title: String?) {
        mSource?.setSubTitle(title)
    }

    fun setSubTitle(@StringRes title: Int) {
        mSource?.setSubTitle(title)
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
        return mSource?.getContext()?.let { ContextCompat.getDrawable(it, id) }
    }

    @ColorInt
    fun getColor(@ColorRes id: Int): Int? {
        return mSource?.getContext()?.let { ContextCompat.getColor(it, id) }
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

    fun snackBar(message: CharSequence) {
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

    interface OnDialogClickListener {

        fun onClick(which: Int)

    }

}