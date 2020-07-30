package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.common.R;
import com.example.common.databinding.ViewDialogBinding;
import com.example.common.widget.dialog.callback.OnDialogListener;

/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
@SuppressLint("InflateParams")
public class AppDialog extends BaseDialog {
    private OnDialogListener onDialogListener;

    public AppDialog(@NonNull Context context) {
        super(context);
        createDataBinding(DataBindingUtil.bind(LayoutInflater.from(getContext()).inflate(R.layout.view_dialog, null)), true, false);
    }

    public AppDialog setParams(String tipText, String contentText, String sureText) {
        return setParams(tipText, contentText, sureText, "");
    }

    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText) {
        return setParams(tipText, contentText, sureText, cancelText, TextGravityState.LEFT);
    }

    //App统一提示框
    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText, TextGravityState state) {
        ViewDialogBinding binding = getBinding();
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
        switch (state) {
            case LEFT:
                binding.tvDialogContainer.setGravity(Gravity.LEFT);
                break;
            case CENTER:
                binding.tvDialogContainer.setGravity(Gravity.CENTER);
                break;
            case RIGHT:
                binding.tvDialogContainer.setGravity(Gravity.RIGHT);
                break;
        }

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

    public enum TextGravityState {
        LEFT, CENTER, RIGHT
    }

}