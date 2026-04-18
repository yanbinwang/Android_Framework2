package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.album.AlbumActivity;
import com.yanzhenjie.album.model.AlbumFile;

import java.util.ArrayList;

/**
 * 图片单选专用包装类
 * 继承自：BasicChoiceWrapper
 * 功能：打开相册 → 只选图片 → 只能选 1 张 → 直接返回
 */
public final class ImageSingleWrapper extends BasicChoiceWrapper<ImageSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile> {

    public ImageSingleWrapper(Context context) {
        super(context);
    }

    /**
     * 启动图片单选页面
     */
    @Override
    public void start() {
        // 把过滤、回调交给 AlbumActivity
        AlbumActivity.sSizeFilter = mSizeFilter;
        AlbumActivity.sMimeFilter = mMimeTypeFilter;
        AlbumActivity.sResult = mResult;
        AlbumActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, AlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        // 功能 = 纯图片选择
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_IMAGE);
        // 模式 = 单选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE);
        // 列数、相机、最大数量=1、过滤显示
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount);
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera);
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1);
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility);
        mContext.startActivity(intent);
    }

}