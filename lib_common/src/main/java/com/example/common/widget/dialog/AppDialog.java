package com.example.common.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.common.base.BaseDialog;
import com.example.common.databinding.ViewDialogBinding;
import com.example.common.widget.dialog.callback.OnDialogListener;

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
public class AppDialog extends BaseDialog<ViewDialogBinding> {
    private OnDialogListener onDialogListener;

    public AppDialog(@NonNull Context context) {
        super(context);
        initialize(true, false);
    }

    public AppDialog setParams(String tipText, String contentText, String sureText) {
        return setParams(tipText, contentText, sureText, "");
    }

    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText) {
        return setParams(tipText, contentText, sureText, cancelText, true);
    }

    //App统一提示框
    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText, boolean center) {
        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipText)) {
            binding.tvDialogTip.setVisibility(View.GONE);
        }
        //如果没有传入取消字段,则隐藏取消view
        if (TextUtils.isEmpty(cancelText)) {
            binding.viewLine.setVisibility(View.GONE);
            binding.tvDialogCancel.setVisibility(View.GONE);
        }
        //文案方向
        binding.tvDialogContainer.setGravity(center ? Gravity.CENTER : Gravity.LEFT);

        //对控件赋值
        binding.tvDialogTip.setText(TextUtils.isEmpty(tipText) ? "" : tipText);
        binding.tvDialogContainer.setText(TextUtils.isEmpty(contentText) ? "" : contentText);
        binding.tvDialogSure.setText(TextUtils.isEmpty(sureText) ? "" : sureText);
        binding.tvDialogCancel.setText(TextUtils.isEmpty(cancelText) ? "" : cancelText);

        //点击了取消按钮的回调
        binding.tvDialogCancel.setOnClickListener(v -> {
            dismiss();
            if (null != onDialogListener) {
                onDialogListener.onDialogCancel();
            }
        });

        //点击了确定按钮的回调
        binding.tvDialogSure.setOnClickListener(v -> {
            dismiss();
            if (null != onDialogListener) {
                onDialogListener.onDialogConfirm();
            }
        });
        return this;
    }

    public AppDialog setOnDialogListener(OnDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
        return this;
    }

    public static AppDialog with(Context context) {
        return new AppDialog(context);
    }

}