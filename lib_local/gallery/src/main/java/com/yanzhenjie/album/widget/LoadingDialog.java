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
 * 相册专用加载对话框
 * 用途：扫描图片、加载视频时显示的等待弹窗
 */
public class LoadingDialog extends Dialog {
    // 加载进度条
    private final ColorProgressBar mProgressBar;
    // 加载提示文字
    private final TextView mTvMessage;

    public LoadingDialog(@NonNull Context context) {
        // 用相册的Dialog样式
        super(context, R.style.Album_Dialog);
        // 不能返回键取消
        setCancelable(false);
        // 点击外部不消失
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.album_dialog_loading);
        mProgressBar = findViewById(R.id.progress_bar);
        mTvMessage = findViewById(R.id.tv_message);
    }

    /**
     * 根据主题配置 加载条颜色（亮色/暗色模式）
     */
    public void setupViews(Widget widget) {
        int color;
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            // 浅色模式 → 深色加载条
            color = ContextCompat.getColor(getContext(), R.color.albumLoadingDark);
        } else {
            // 深色模式 → 用主题色
            color = ContextCompat.getColor(getContext(), widget.getStatusBarColor());
        }
        mProgressBar.setColorFilter(color);
    }

    /**
     * 设置提示文字（资源ID）
     */
    public void setMessage(@StringRes int message) {
        mTvMessage.setText(message);
    }

    /**
     * 设置提示文字（字符串）
     */
    public void setMessage(String message) {
        mTvMessage.setText(message);
    }

}