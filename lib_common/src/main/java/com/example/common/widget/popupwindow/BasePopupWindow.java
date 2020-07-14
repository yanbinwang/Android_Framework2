package com.example.common.widget.popupwindow;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.example.common.R;

import java.lang.ref.WeakReference;

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
public abstract class BasePopupWindow extends PopupWindow {
    private boolean dark;
    private WeakReference<Activity> weakActivity;
    private WindowManager.LayoutParams layoutParams;

    public BasePopupWindow(Activity activity) {
        this(activity, false);
    }

    public BasePopupWindow(Activity activity, boolean dark) {
        this.weakActivity = new WeakReference<>(activity);
        this.layoutParams = weakActivity.get().getWindow().getAttributes();
        this.dark = dark;
    }

    protected void setPopupWindowContentView(View view) {
        setContentView(view);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setAnimationStyle(R.style.pushBottomAnimStyle);//默认底部弹出，可重写
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setHideAttributes();
    }

    private void setShowAttributes() {
        if (dark) {
            layoutParams.alpha = 0.7f;
            weakActivity.get().getWindow().setAttributes(layoutParams);
        }
    }

    private void setHideAttributes() {
        if (dark) {
            setOnDismissListener(() -> {
                layoutParams.alpha = 1f;
                weakActivity.get().getWindow().setAttributes(layoutParams);
            });
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        setShowAttributes();
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        setShowAttributes();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        setShowAttributes();
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        setShowAttributes();
        super.showAtLocation(parent, gravity, x, y);
    }

    public Activity getWeakActivity() {
        return weakActivity.get();
    }

}