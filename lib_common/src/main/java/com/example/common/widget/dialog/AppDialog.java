package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.common.R;
import com.example.common.databinding.ViewDialogConfirmBinding;
import com.example.common.databinding.ViewDialogConfirmOrCancelBinding;
import com.example.common.widget.dialog.callback.OnConfirmDialogListener;
import com.example.common.widget.dialog.callback.OnConfirmOrCancelDialogListener;
import com.example.framework.utils.AnimationLoader;


/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
@SuppressLint("InflateParams")
public class AppDialog extends Dialog {
    private AnimationSet mAnimIn, mAnimOut;
    private OnConfirmOrCancelDialogListener onConfirmOrCancelDialogListener;
    private OnConfirmDialogListener onConfirmDialogListener;

    public AppDialog(@NonNull Context context) {
        super(context, R.style.appDialogStyle);
        //定义开始和退出的动画
        mAnimIn = AnimationLoader.getInAnimation(context);
        mAnimOut = AnimationLoader.getOutAnimation(context);
    }

    //包含確定取消的提示框
    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText) {
        ViewDialogConfirmOrCancelBinding binding = DataBindingUtil.bind(LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_confirm_or_cancel, null));
        setContentView(binding.getRoot(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        setOnShowListener(dialog -> binding.getRoot().startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        setOnDismissListener(dialog -> binding.getRoot().startAnimation(mAnimOut));

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipText)) {
            binding.tvDialogTip.setVisibility(View.GONE);
        }
        //对控件赋值
        binding.tvDialogTip.setText(TextUtils.isEmpty(tipText) ? "" : tipText);
        binding.tvDialogContainer.setText(TextUtils.isEmpty(contentText) ? "" : contentText);
        binding.tvDialogSure.setText(TextUtils.isEmpty(sureText) ? "" : sureText);
        binding.tvDialogCancel.setText(TextUtils.isEmpty(cancelText) ? "" : cancelText);

//        if (tipText.contains("发现新版本") || tipText.equals("安装应用")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了取消按钮的回调
        binding.tvDialogCancel.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmOrCancelDialogListener) {
                onConfirmOrCancelDialogListener.onDialogCancel();
            }
        });

        //点击了确定按钮的回调
        binding.tvDialogSure.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmOrCancelDialogListener) {
                onConfirmOrCancelDialogListener.onDialogConfirm();
            }
        });
        return this;
    }

    //包含確定的提示框
    public AppDialog setParams(String tipText, String contentText, String sureText) {
        ViewDialogConfirmBinding binding = DataBindingUtil.bind(LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_confirm, null));
        setContentView(binding.getRoot(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        setOnShowListener(dialog -> binding.getRoot().startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        setOnDismissListener(dialog -> binding.getRoot().startAnimation(mAnimOut));

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipText)) {
            binding.tvDialogTip.setVisibility(View.GONE);
        }
        //对控件赋值
        binding.tvDialogTip.setText(TextUtils.isEmpty(tipText) ? "" : tipText);
        binding.tvDialogContainer.setText(TextUtils.isEmpty(contentText) ? "" : contentText);
        binding.tvDialogSure.setText(TextUtils.isEmpty(sureText) ? "" : sureText);

//        if (tipText.contains("发现新版本")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了确定按钮的回调
        binding.tvDialogSure.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmDialogListener) {
                onConfirmDialogListener.onDialogConfirm();
            }
        });
        return this;
    }

    public AppDialog setOnConfirmOrCancelDialogListener(OnConfirmOrCancelDialogListener onConfirmOrCancelDialogListener) {
        this.onConfirmOrCancelDialogListener = onConfirmOrCancelDialogListener;
        return this;
    }

    public AppDialog setOnConfirmDialogListener(OnConfirmDialogListener onConfirmDialogListener) {
        this.onConfirmDialogListener = onConfirmDialogListener;
        return this;
    }

    public static AppDialog with(Context context) {
        return new AppDialog(context);
    }

}