package com.yanzhenjie.album.app.album;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.gallery.R;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.utils.AlbumUtil;

/**
 * 空页面 View 层
 * 功能：当手机里没有图片/视频时，显示这个页面
 * 提供：拍照、录像按钮，纯 UI 展示
 */
public class NullView extends Contract.NullView implements View.OnClickListener {
    // 标题栏
    private final Toolbar mToolbar;
    // 标题文字
    private final TextView mTitle;
    // 空页面提示文字
    private final TextView mTvMessage;
    // 拍照按钮
    private final Button mBtnTakeImage;
    // 录像按钮
    private final Button mBtnTakeVideo;

    /**
     * 构造方法：绑定控件
     */
    public NullView(Activity activity, Contract.NullPresenter presenter) {
        super(activity, presenter);
        // 设置Toolbar
        this.mToolbar = activity.findViewById(R.id.toolbar);
        this.mTitle = activity.findViewById(R.id.tv_title);
        this.mTvMessage = activity.findViewById(R.id.tv_message);
        this.mBtnTakeImage = activity.findViewById(R.id.btn_camera_image);
        this.mBtnTakeVideo = activity.findViewById(R.id.btn_camera_video);
        // 按钮点击事件
        this.mBtnTakeImage.setOnClickListener(this);
        this.mBtnTakeVideo.setOnClickListener(this);
    }

    /**
     * 初始化页面样式（颜色、主题、图标）
     */
    @Override
    public void setupViews(Widget widget) {
        int mStatusColor = widget.getStatusBarColor();
        mToolbar.setBackgroundColor(getColor(mStatusColor));
        mTitle.setText(widget.getTitle());
        // 设置返回箭头
        Drawable navigationIcon = getDrawable(R.mipmap.album_ic_back_white);
        // 浅色 / 深色 主题切换
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            mTitle.setTextColor(getColor(R.color.textBlack));
            AlbumUtil.setDrawableTint(navigationIcon, getColor(R.color.albumIconDark));
            setHomeAsUpIndicator(navigationIcon);
        } else {
            mTitle.setTextColor(getColor(R.color.textWhite));
            setHomeAsUpIndicator(navigationIcon);
        }
        // 按钮样式：颜色、背景
        Widget.ButtonStyle buttonStyle = widget.getButtonStyle();
        ColorStateList buttonSelector = buttonStyle.getButtonSelector();
        mBtnTakeImage.setBackgroundTintList(buttonSelector);
        mBtnTakeVideo.setBackgroundTintList(buttonSelector);
        // 浅色主题下，按钮图标/文字 改为深色
        if (buttonStyle.getUiStyle() == Widget.STYLE_LIGHT) {
            Drawable drawable = mBtnTakeImage.getCompoundDrawables()[0];
            AlbumUtil.setDrawableTint(drawable, getColor(R.color.albumIconDark));
            mBtnTakeImage.setCompoundDrawables(drawable, null, null, null);
            drawable = mBtnTakeVideo.getCompoundDrawables()[0];
            AlbumUtil.setDrawableTint(drawable, getColor(R.color.albumIconDark));
            mBtnTakeVideo.setCompoundDrawables(drawable, null, null, null);
            mBtnTakeImage.setTextColor(getColor(R.color.albumFontDark));
            mBtnTakeVideo.setTextColor(getColor(R.color.albumFontDark));
        }
    }

    /**
     * 设置空页面提示文字
     */
    @Override
    public void setMessage(int message) {
        mTvMessage.setText(message);
    }

    /**
     * 显示 / 隐藏 拍照按钮
     */
    @Override
    public void setMakeImageDisplay(boolean display) {
        mBtnTakeImage.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示 / 隐藏 录像按钮
     */
    @Override
    public void setMakeVideoDisplay(boolean display) {
        mBtnTakeVideo.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    /**
     * 点击拍照 / 录像，交给 Presenter 处理逻辑
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_camera_image) {
            getPresenter().takePicture();
        } else if (id == R.id.btn_camera_video) {
            getPresenter().takeVideo();
        }
    }

}