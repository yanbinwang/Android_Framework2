package com.yanzhenjie.durban.widget.dialog.loading;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.example.gallery.R;

/**
 * 通用加载对话框
 * 功能：显示 LoadingView + 提示文字，外部可设置文字、颜色
 */
public class LoadingDialog extends Dialog {
    // 加载动画View
    private final LoadingView mLoadingView;
    // 提示文字
    private final TextView mTvMessage;

    /**
     * 构造方法
     */
    public LoadingDialog(Context context) {
        super(context, R.style.Durban_Dialog_Loading);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.durban_dialog_loading);
        mLoadingView = findViewById(R.id.loading_view);
        mTvMessage = findViewById(R.id.loading_tv_message);
    }

    /**
     * 设置提示文字（资源ID）
     */
    public void setMessage(int resId) {
        mTvMessage.setText(resId);
    }

    /**
     * 设置提示文字（字符串）
     */
    public void setMessage(String message) {
        mTvMessage.setText(message);
    }

    /**
     * 设置加载圈三段颜色
     */
    public void setCircleColors(int r1, int r2, int r3) {
        mLoadingView.setCircleColors(r1, r2, r3);
    }

}