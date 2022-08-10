package com.example.common.widget.dialog;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

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
        initialize(true, true);
    }

    public AppDialog setParams(String title, String message, String positiveText) {
        return setParams(title, message, positiveText, "");
    }

    public AppDialog setParams(String title, String message, String positiveText, String negativeText) {
        return setParams(title, message, positiveText, negativeText, true);
    }

    //App统一提示框
    public AppDialog setParams(String title, String message, String positiveText, String negativeText, boolean center) {
        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(title)) {
            binding.tvTip.setVisibility(View.GONE);
        }
        //如果没有传入取消字段,则隐藏取消view
        if (TextUtils.isEmpty(negativeText)) {
            binding.viewLine.setVisibility(View.GONE);
            binding.tvCancel.setVisibility(View.GONE);
        }
        //文案方向
        binding.tvContainer.setGravity(center ? Gravity.CENTER : Gravity.LEFT);

        //对控件赋值
        binding.tvTip.setText(TextUtils.isEmpty(title) ? "" : title);
        binding.tvContainer.setText(TextUtils.isEmpty(message) ? "" : message);
        binding.tvSure.setText(TextUtils.isEmpty(positiveText) ? "" : positiveText);
        binding.tvCancel.setText(TextUtils.isEmpty(negativeText) ? "" : negativeText);

        //点击了取消按钮的回调
        binding.tvCancel.setOnClickListener(v -> {
            dismiss();
            if (null != onDialogListener) {
                onDialogListener.onCancel();
            }
        });

        //点击了确定按钮的回调
        binding.tvSure.setOnClickListener(v -> {
            dismiss();
            if (null != onDialogListener) {
                onDialogListener.onConfirm();
            }
        });
        return this;
    }

    public AppDialog setType() {
        getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
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