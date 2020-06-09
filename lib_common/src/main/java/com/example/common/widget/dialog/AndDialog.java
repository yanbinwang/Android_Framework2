package com.example.common.widget.dialog;


import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.example.common.R;
import com.example.common.widget.dialog.callback.OnAndDialogListener;


/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
public class AndDialog {

    public static void show(Context context, String tipStr, String contentStr, String sureStr, String cancelStr, OnAndDialogListener onAndDialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialogStyle);
        if (!TextUtils.isEmpty(tipStr)) {
            builder.setTitle(tipStr);
        }
        builder.setMessage(TextUtils.isEmpty(contentStr) ? "" : contentStr);
        builder.setNegativeButton(sureStr, (dialog, which) -> {
            if (null != onAndDialogListener) {
                onAndDialogListener.onDialogConfirm();
            }
        });
        builder.setPositiveButton(cancelStr, (dialog, which) -> {
            if (null != onAndDialogListener) {
                onAndDialogListener.onDialogCancel();
            }
        });
        builder.show();
    }

}
