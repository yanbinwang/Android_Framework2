package com.example.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.common.R;
import com.example.framework.utils.AnimationLoader;

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
public abstract class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.dialogStyle);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected void setDialogContentView(View view) {
        setDialogContentView(view, false, false);
    }

    /**
     * 设置布局
     *
     * @param view  布局文件
     * @param anim  是否有进入动画
     * @param close 是否可以关闭
     */
    protected void setDialogContentView(View view, boolean anim, boolean close) {
        setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (anim) {
            AnimationSet mAnimIn = AnimationLoader.getInAnimation(getContext());
            AnimationSet mAnimOut = AnimationLoader.getOutAnimation(getContext());
            //当布局show出来的时候执行开始动画
            setOnShowListener(dialog -> view.startAnimation(mAnimIn));
            //当布局销毁时执行结束动画
            setOnDismissListener(dialog -> view.startAnimation(mAnimOut));
        }
        if (close) {
            setOnKeyListener((dialog, keyCode, event) -> true);
            setCancelable(true);
        }
    }

}
