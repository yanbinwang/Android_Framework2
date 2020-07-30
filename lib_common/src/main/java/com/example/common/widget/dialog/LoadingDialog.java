package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.common.R;


/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
@SuppressLint("InflateParams")
public class LoadingDialog extends BaseDialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.loadingStyle);
        createDataBinding(DataBindingUtil.bind(LayoutInflater.from(context).inflate(R.layout.view_dialog_loading, null)));
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