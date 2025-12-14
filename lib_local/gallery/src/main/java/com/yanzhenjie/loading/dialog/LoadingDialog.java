package com.yanzhenjie.loading.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.example.gallery.R;
import com.yanzhenjie.loading.LoadingView;

/**
 * <p>Default loading dialog.</p>
 * Created by Yan Zhenjie on 2017/5/17.
 */
public class LoadingDialog extends Dialog {
    private LoadingView mLoadingView;
    private TextView mTvMessage;

    public LoadingDialog(Context context) {
        super(context, R.style.loadingDialog_Loading);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.loading_wait_dialog);
        mLoadingView = findViewById(R.id.loading_view);
        mTvMessage = findViewById(R.id.loading_tv_message);
    }

    /**
     * Set several colors of the circle.
     */
    public void setCircleColors(int r1, int r2, int r3) {
        mLoadingView.setCircleColors(r1, r2, r3);
    }

    /**
     * Set message.
     */
    public void setMessage(int resId) {
        mTvMessage.setText(resId);
    }

    /**
     * Set message.
     */
    public void setMessage(String message) {
        mTvMessage.setText(message);
    }

}