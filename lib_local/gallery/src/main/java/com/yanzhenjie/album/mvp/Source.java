package com.yanzhenjie.album.mvp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

/**
 * Created by YanZhenjie on 2017/12/8.
 */
abstract class Source<Host> {

    private Host mHost;

    public Source(Host host) {
        mHost = host;
    }

    public Host getHost() {
        return mHost;
    }

    abstract void prepare();

    abstract void setActionBar(Toolbar actionBar);

    abstract MenuInflater getMenuInflater();

    abstract Menu getMenu();

    abstract void setMenuClickListener(MenuClickListener selectedListener);

    abstract void setDisplayHomeAsUpEnabled(boolean showHome);

    abstract void setHomeAsUpIndicator(@DrawableRes int icon);

    abstract void setHomeAsUpIndicator(Drawable icon);

    abstract void setTitle(CharSequence title);

    abstract void setTitle(@StringRes int title);

    abstract void setSubTitle(CharSequence title);

    abstract void setSubTitle(@StringRes int title);

    abstract Context getContext();

    abstract View getView();

    abstract void closeInputMethod();

    interface MenuClickListener {

        void onHomeClick();

        void onMenuClick(MenuItem item);
    }

}