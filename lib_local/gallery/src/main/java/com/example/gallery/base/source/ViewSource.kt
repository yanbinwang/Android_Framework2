package com.example.gallery.base.source

import android.annotation.SuppressLint
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
class ViewSource(view: View) : Source<View>(view) {
    private var mActionBar: Toolbar? = null
    private var mActionBarIcon: Drawable? = null
    private var mMenuItemSelectedListener: MenuClickListener? = null

    override fun prepare() {
        getHost()?.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setActionBar(toolbar)
            setSupportToolbar(toolbar)
        }
    }

    override fun setActionBar(actionBar: Toolbar?) {
        this.mActionBar = actionBar
        if (mActionBar != null) {
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
        if (mActionBar != null) {
            if (showHome) {
                mActionBar?.setNavigationIcon(mActionBarIcon)
            } else {
                mActionBar?.setNavigationIcon(null)
            }
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
        return getHost()?.context
    }

    override fun getView(): View? {
        return getHost()
    }

    override fun closeInputMethod() {
        val focusView = getView()?.findFocus()
        if (focusView != null) {
            val manager = getContext()?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            manager?.hideSoftInputFromWindow(focusView.windowToken, 0)
        }
    }

}