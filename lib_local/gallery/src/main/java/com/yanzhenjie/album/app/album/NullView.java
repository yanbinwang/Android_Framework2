package com.yanzhenjie.album.app.album;

import static com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.gallery.R;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.model.ButtonStyle;
import com.yanzhenjie.album.model.Widget;
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
     * 按钮样式：颜色、背景
     * .buttonStyle(Widget.ButtonStyle.newDarkBuilder(context)
     *       .setButtonSelector(normalColor, highlightColor)
     *       .build())
     */
    @Override
    public void setupViews(Widget widget) {
        // 设置返回箭头
        Drawable navigationIcon = getDrawable(R.mipmap.gallery_ic_back);
        // 浅色 / 深色 主题切换
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            AlbumUtil.setDrawableTint(navigationIcon, getColor(R.color.galleryIconDark));
            mTitle.setTextColor(getColor(R.color.galleryFontDark));
        } else {
            mTitle.setTextColor(getColor(R.color.galleryFontLight));
        }
        // 设置返回按钮
        setHomeAsUpIndicator(navigationIcon);
        // 标题同步状态栏颜色
        mToolbar.setBackgroundColor(getColor(widget.getStatusBarColor()));
        mTitle.setText(widget.getTitle());
        // 设置按钮颜色
        ButtonStyle buttonStyle = widget.getButtonStyle();
        ColorStateList buttonSelector = buttonStyle.getButtonSelector();
        mBtnTakeImage.setBackgroundTintList(buttonSelector);
        mBtnTakeVideo.setBackgroundTintList(buttonSelector);
        // 获取按钮主题色
        boolean isLight = shouldUseWhiteSystemBarsForColor(buttonSelector.getDefaultColor());
        // 如果需要深色主题,提取出绘制的图标并渲染成深色
        if (!isLight) {
            Drawable[] takeImageDraws = mBtnTakeImage.getCompoundDrawablesRelative();
            Drawable takeImageIcon = takeImageDraws[0];
            AlbumUtil.setDrawableTint(takeImageIcon, getColor(R.color.galleryIconDark));
            mBtnTakeImage.setCompoundDrawables(takeImageIcon, null, null, null);
            mBtnTakeImage.setTextColor(getColor(R.color.galleryFontDark));
            Drawable[] takeVideoDraws = mBtnTakeVideo.getCompoundDrawablesRelative();
            Drawable takeVideoIcon = takeVideoDraws[0];
            AlbumUtil.setDrawableTint(takeVideoIcon, getColor(R.color.galleryIconDark));
            mBtnTakeVideo.setCompoundDrawables(takeVideoIcon, null, null, null);
            mBtnTakeVideo.setTextColor(getColor(R.color.galleryFontDark));
        } else {
            mBtnTakeImage.setTextColor(getColor(R.color.galleryFontLight));
            mBtnTakeVideo.setTextColor(getColor(R.color.galleryFontLight));
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