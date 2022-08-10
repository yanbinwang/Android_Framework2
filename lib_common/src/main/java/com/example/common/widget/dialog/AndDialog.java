package com.example.common.widget.dialog;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.common.R;
import com.example.common.widget.dialog.callback.OnDialogListener;

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
public class AndDialog extends AlertDialog.Builder {
    private OnDialogListener onDialogListener;

    public AndDialog(@NonNull Context context) {
        super(context, R.style.dialogStyle);
    }

    public AndDialog setParams(String title, String message, String positiveText, String negativeText) {
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        setMessage(TextUtils.isEmpty(message) ? "" : message);
        setPositiveButton(positiveText, (dialog, which) -> {
            if (null != onDialogListener) {
                onDialogListener.onConfirm();
            }
        });
        //如果没有传入取消字段,则隐藏取消
        if (!TextUtils.isEmpty(negativeText)) {
            setNegativeButton(negativeText, (dialog, which) -> {
                if (null != onDialogListener) {
                    onDialogListener.onCancel();
                }
            });
        }
        return this;
    }

    public AndDialog setOnDialogListener(OnDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
        return this;
    }

    public static AndDialog with(Context context) {
        return new AndDialog(context);
    }

}