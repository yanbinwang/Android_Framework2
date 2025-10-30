package com.yanzhenjie.album.app.album;

import static com.example.gallery.base.BaseActivity.setSupportToolbar;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.example.gallery.R;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.util.AlbumUtils;

/**
 * Created by YanZhenjie on 2018/4/7.
 */
public class NullView extends Contract.NullView implements View.OnClickListener {
    private Activity mActivity;
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTvMessage;
    private AppCompatButton mBtnTakeImage;
    private AppCompatButton mBtnTakeVideo;

    public NullView(Activity activity, Contract.NullPresenter presenter) {
        super(activity, presenter);
        this.mActivity = activity;
        this.mToolbar = activity.findViewById(R.id.toolbar);
        setSupportToolbar(mToolbar);
        this.mTitle = activity.findViewById(R.id.tv_title);
        this.mTvMessage = activity.findViewById(R.id.tv_message);
        this.mBtnTakeImage = activity.findViewById(R.id.btn_camera_image);
        this.mBtnTakeVideo = activity.findViewById(R.id.btn_camera_video);
        this.mBtnTakeImage.setOnClickListener(this);
        this.mBtnTakeVideo.setOnClickListener(this);
    }

    @Override
    public void setupViews(Widget widget) {
        int mStatusColor = widget.getStatusBarColor();
        mToolbar.setBackgroundColor(getColor(mStatusColor));
        mToolbar.setSubtitleTextColor(getColor(mStatusColor));
        mToolbar.setTitleTextColor(getColor(mStatusColor));
        mTitle.setText(widget.getTitle());
        Drawable navigationIcon = getDrawable(R.drawable.album_ic_back_white);
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            mTitle.setTextColor(getColor(R.color.textBlack));
            AlbumUtils.setDrawableTint(navigationIcon, getColor(R.color.albumIconDark));
            setHomeAsUpIndicator(navigationIcon);
        } else {
            mTitle.setTextColor(getColor(R.color.textWhite));
            setHomeAsUpIndicator(navigationIcon);
        }
        Widget.ButtonStyle buttonStyle = widget.getButtonStyle();
        ColorStateList buttonSelector = buttonStyle.getButtonSelector();
        mBtnTakeImage.setBackgroundTintList(buttonSelector);
        mBtnTakeVideo.setBackgroundTintList(buttonSelector);
        if (buttonStyle.getUiStyle() == Widget.STYLE_LIGHT) {
            Drawable drawable = mBtnTakeImage.getCompoundDrawables()[0];
            AlbumUtils.setDrawableTint(drawable, getColor(R.color.albumIconDark));
            mBtnTakeImage.setCompoundDrawables(drawable, null, null, null);
            drawable = mBtnTakeVideo.getCompoundDrawables()[0];
            AlbumUtils.setDrawableTint(drawable, getColor(R.color.albumIconDark));
            mBtnTakeVideo.setCompoundDrawables(drawable, null, null, null);
            mBtnTakeImage.setTextColor(getColor(R.color.albumFontDark));
            mBtnTakeVideo.setTextColor(getColor(R.color.albumFontDark));
        }
    }

    @Override
    public void setMessage(int message) {
        mTvMessage.setText(message);
    }

    @Override
    public void setMakeImageDisplay(boolean display) {
        mBtnTakeImage.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMakeVideoDisplay(boolean display) {
        mBtnTakeVideo.setVisibility(display ? View.VISIBLE : View.GONE);
    }

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