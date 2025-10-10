package com.yanzhenjie.album.mvp;

import static com.yanzhenjie.album.mvp.BaseActivity.setSupportToolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;

/**
 * Created by YanZhenjie on 2017/12/8.
 */
@SuppressLint("RestrictedApi")
class ViewSource extends Source<View> {
    private Toolbar mActionBar;
    private Drawable mActionBarIcon;
    private MenuClickListener mMenuItemSelectedListener;

    ViewSource(View view) {
        super(view);
    }

    @Override
    void prepare() {
        Toolbar toolbar = getHost().findViewById(R.id.toolbar);
        setActionBar(toolbar);
        setSupportToolbar(toolbar);
    }

    @Override
    void setActionBar(Toolbar actionBar) {
        this.mActionBar = actionBar;

        if (mActionBar != null) {
            mActionBar.setOnMenuItemClickListener(item -> {
                if (mMenuItemSelectedListener != null) {
                    mMenuItemSelectedListener.onMenuClick(item);
                }
                return true;
            });
            mActionBar.setNavigationOnClickListener(v -> {
                if (mMenuItemSelectedListener != null) {
                    mMenuItemSelectedListener.onHomeClick();
                }
            });
            mActionBarIcon = mActionBar.getNavigationIcon();
        }
    }

    @Override
    MenuInflater getMenuInflater() {
        return new SupportMenuInflater(getContext());
    }

    @Override
    Menu getMenu() {
        return mActionBar == null ? null : mActionBar.getMenu();
    }

    @Override
    void setMenuClickListener(MenuClickListener selectedListener) {
        this.mMenuItemSelectedListener = selectedListener;
    }

    @Override
    void setDisplayHomeAsUpEnabled(boolean showHome) {
        if (mActionBar != null) {
            if (showHome) {
                mActionBar.setNavigationIcon(mActionBarIcon);
            } else {
                mActionBar.setNavigationIcon(null);
            }
        }
    }

    @Override
    void setHomeAsUpIndicator(@DrawableRes int icon) {
        setHomeAsUpIndicator(ContextCompat.getDrawable(getContext(), icon));
    }

    @Override
    void setHomeAsUpIndicator(Drawable icon) {
        this.mActionBarIcon = icon;
        if (mActionBar != null)
            mActionBar.setNavigationIcon(icon);
    }

    @Override
    final void setTitle(CharSequence title) {
        if (mActionBar != null)
            mActionBar.setTitle(title);
    }

    @Override
    final void setTitle(@StringRes int title) {
        if (mActionBar != null)
            mActionBar.setTitle(title);
    }

    @Override
    final void setSubTitle(CharSequence title) {
        if (mActionBar != null)
            mActionBar.setSubtitle(title);
    }

    @Override
    final void setSubTitle(@StringRes int title) {
        if (mActionBar != null)
            mActionBar.setSubtitle(title);
    }

    @Override
    Context getContext() {
        return getHost().getContext();
    }

    @Override
    View getView() {
        return getHost();
    }

    @Override
    void closeInputMethod() {
        View focusView = getView().findFocus();
        if (focusView != null) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }
    }

}