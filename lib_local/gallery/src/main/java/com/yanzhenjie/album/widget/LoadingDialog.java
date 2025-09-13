package com.yanzhenjie.album.widget;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;
import com.yanzhenjie.album.api.widget.Widget;

/**
 * Created by YanZhenjie on 2018/4/10.
 */
public class LoadingDialog extends Dialog {
    private ColorProgressBar mProgressBar;
    private TextView mTvMessage;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.Album_Dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.album_dialog_loading);
        mProgressBar = findViewById(R.id.progress_bar);
        mTvMessage = findViewById(R.id.tv_message);
    }

    /**
     * Set some properties of the view.
     *
     * @param widget widget.
     */
    public void setupViews(Widget widget) {
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            int color = ContextCompat.getColor(getContext(), R.color.albumLoadingDark);
            mProgressBar.setColorFilter(color);
        } else {
            int color = ContextCompat.getColor(getContext(), widget.getStatusBarColor());
            mProgressBar.setColorFilter(color);
        }
    }

    /**
     * Set the message.
     *
     * @param message message resource id.
     */
    public void setMessage(@StringRes int message) {
        mTvMessage.setText(message);
    }

    /**
     * Set the message.
     *
     * @param message message.
     */
    public void setMessage(String message) {
        mTvMessage.setText(message);
    }

}