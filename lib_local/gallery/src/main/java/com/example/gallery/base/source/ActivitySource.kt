package com.example.gallery.base.source

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.gallery.R
import com.example.gallery.base.BaseActivity.Companion.setSupportToolbar

/**
 * Created by YanZhenjie on 2017/12/8.
 */
class ActivitySource(activity: Activity) : Source<Activity>(activity) {
    private var mView: View? = null
    private var mActionBar: Toolbar? = null
    private var mActionBarIcon: Drawable? = null
    private var mMenuItemSelectedListener: MenuClickListener? = null

    init {
        mView = activity.findViewById<View>(R.id.content)
    }

    override fun prepare() {
        getHost()?.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setActionBar(toolbar)
            setSupportToolbar(toolbar)
        }
    }

    override fun setActionBar(actionBar: Toolbar?) {
        this.mActionBar = actionBar
        if (mActionBar != null) {
            setTitle(getHost()?.title)
            mActionBar?.setOnMenuItemClickListener { item: MenuItem? ->
                mMenuItemSelectedListener?.onMenuClick(item)
                true
            }
            mActionBar?.setNavigationOnClickListener { v: View? ->
                mMenuItemSelectedListener?.onHomeClick()
            }
            mActionBarIcon = mActionBar?.navigationIcon
        }
    }

    @SuppressLint("RestrictedApi")
    override fun getMenuInflater(): MenuInflater? {
        return SupportMenuInflater(getContext())
    }

    override fun getMenu(): Menu? {
        return if (mActionBar == null) null else mActionBar?.getMenu()
    }

    override fun setMenuClickListener(selectedListener: MenuClickListener?) {
        this.mMenuItemSelectedListener = selectedListener
    }

    override fun setDisplayHomeAsUpEnabled(showHome: Boolean) {
        if (showHome) {
            mActionBar?.setNavigationIcon(mActionBarIcon)
        } else {
            mActionBar?.setNavigationIcon(null)
        }
    }

    override fun setHomeAsUpIndicator(icon: Int) {
        setHomeAsUpIndicator(getContext()?.let { ContextCompat.getDrawable(it, icon) })
    }

    override fun setHomeAsUpIndicator(icon: Drawable?) {
        this.mActionBarIcon = icon
        mActionBar?.setNavigationIcon(icon)
    }

    override fun setTitle(title: CharSequence?) {
        mActionBar?.setTitle(title)
    }

    override fun setTitle(title: Int) {
        mActionBar?.setTitle(title)
    }

    override fun setSubTitle(title: CharSequence?) {
        mActionBar?.setSubtitle(title)
    }

    override fun setSubTitle(title: Int) {
        mActionBar?.setSubtitle(title)
    }

    override fun getContext(): Context? {
        return getHost()
    }

    override fun getView(): View? {
        return mView
    }

    override fun closeInputMethod() {
        val focusView = getHost()?.currentFocus
        if (focusView != null) {
            val manager = getHost()?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            manager?.hideSoftInputFromWindow(focusView.windowToken, 0)
        }
    }

}