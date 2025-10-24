package com.example.gallery.base.source

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar

/**
 * Created by YanZhenjie on 2017/12/8.
 */
abstract class Source<Host>(private val mHost: Host) {

    fun getHost(): Host? {
        return mHost
    }

    abstract fun prepare()

    abstract fun setActionBar(actionBar: Toolbar?)

    abstract fun getMenuInflater(): MenuInflater?

    abstract fun getMenu(): Menu?

    abstract fun setMenuClickListener(selectedListener: MenuClickListener?)

    abstract fun setDisplayHomeAsUpEnabled(showHome: Boolean)

    abstract fun setHomeAsUpIndicator(@DrawableRes icon: Int)

    abstract fun setHomeAsUpIndicator(icon: Drawable?)

    abstract fun setTitle(title: CharSequence?)

    abstract fun setTitle(@StringRes title: Int)

    abstract fun setSubTitle(title: CharSequence?)

    abstract fun setSubTitle(@StringRes title: Int)

    abstract fun getContext(): Context?

    abstract fun getView(): View?

    abstract fun closeInputMethod()

    interface MenuClickListener {
        /**
         * 左侧返回按钮点击
         */
        fun onHomeClick()

        /**
         * 右侧菜单点击
         */
        fun onMenuClick(item: MenuItem?)
    }

}