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

    public AndDialog setParams(String tipText, String contentText, String sureText, String cancelText) {
        if (!TextUtils.isEmpty(tipText)) {
            setTitle(tipText);
        }
        setMessage(TextUtils.isEmpty(contentText) ? "" : contentText);
        setNegativeButton(sureText, (dialog, which) -> {
            if (null != onDialogListener) {
                onDialogListener.onDialogConfirm();
            }
        });
        setPositiveButton(cancelText, (dialog, which) -> {
            if (null != onDialogListener) {
                onDialogListener.onDialogCancel();
            }
        });
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
