package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.common.R;


/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
@SuppressLint("InflateParams")
public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.loadingStyle);
        setContentView(LayoutInflater.from(context).inflate(R.layout.view_dialog_loading, null), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public void show(boolean flag) {
        setCancelable(flag);
        if (!isShowing()) {
            show();
        }
    }

    public void hide() {
        if (isShowing()) {
            dismiss();
        }
    }

}