package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.callback.Filter;
import com.yanzhenjie.album.app.album.AlbumActivity;

import java.util.ArrayList;

/**
 * 图片 + 视频 **单选模式**
 * 继承自：BasicChoiceAlbumWrapper（混合选择器父类）
 * 功能：
 * 对外提供链式调用，最终跳转到 AlbumActivity 单选页面
 * 只能选 1 个！
 */
public class AlbumSingleWrapper extends BasicChoiceAlbumWrapper<AlbumSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile> {
    // 视频时长过滤器
    private Filter<Long> mDurationFilter;

    public AlbumSingleWrapper(Context context) {
        super(context);
    }

    /**
     * 过滤视频时长
     */
    public AlbumSingleWrapper filterDuration(Filter<Long> filter) {
        this.mDurationFilter = filter;
        return this;
    }

    @Override
    public void start() {
        // 静态赋值，让 AlbumActivity 接收
        AlbumActivity.sSizeFilter = mSizeFilter;
        AlbumActivity.sMimeFilter = mMimeTypeFilter;
        AlbumActivity.sDurationFilter = mDurationFilter;
        AlbumActivity.sResult = mResult;
        AlbumActivity.sCancel = mCancel;
        // 跳转相册
        Intent intent = new Intent(mContext, AlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        // 功能 = 混合选择（图片+视频）
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_ALBUM);
        // 模式 = 单选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE);
        // 列数
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount);
        // 是否显示相机
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera);
        // 最大数量 = 1（单选）
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1);
        // 过滤文件是否显示
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility);
        // 相机视频质量
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality);
        // 相机最大时长
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration);
        // 相机最大大小
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes);
        mContext.startActivity(intent);
    }

}