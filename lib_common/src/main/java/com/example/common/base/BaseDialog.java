package com.example.common.base;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.example.base.utils.AnimationLoader;
import com.example.common.R;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
public abstract class BaseDialog<VDB extends ViewDataBinding> extends Dialog {
    protected VDB binding;

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.appDialogStyle);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected void initialize() {
        initialize(false, false);
    }

    /**
     * 设置布局
     *
     * @param anim  是否有进入动画
     * @param close 是否可以关闭
     */
    protected void initialize(boolean anim, boolean close) {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            try {
                Class<VDB> vbClass = (Class<VDB>) ((ParameterizedType) type).getActualTypeArguments()[0];
                Method method = vbClass.getMethod("inflate", LayoutInflater.class);
                binding = (VDB) method.invoke(null, getLayoutInflater());
            } catch (Exception e) {
                e.printStackTrace();
            }
            setContentView(binding.getRoot(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            if (anim) {
                AnimationSet mAnimIn = AnimationLoader.getInAnimation(getContext());
                AnimationSet mAnimOut = AnimationLoader.getOutAnimation(getContext());
                //当布局show出来的时候执行开始动画
                setOnShowListener(dialog -> binding.getRoot().startAnimation(mAnimIn));
                //当布局销毁时执行结束动画
                setOnDismissListener(dialog -> binding.getRoot().startAnimation(mAnimOut));
            }
            if (close) {
                setOnKeyListener((dialog, keyCode, event) -> true);
                setCancelable(true);
            }
        }
    }

}
