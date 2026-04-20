package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.app.album.AlbumActivity;
import com.yanzhenjie.album.callback.Filter;
import com.yanzhenjie.album.model.AlbumFile;

import java.util.ArrayList;

/**
 * 视频多选专用包装类
 * 继承自：BasicChoiceVideoWrapper（视频专属父类）
 * 功能：只选视频 + 多选 + 视频时长过滤 + 相机录制参数
 */
public final class VideoMultipleWrapper extends BasicChoiceVideoWrapper<VideoMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>> {
    // 最大选择数量
    private int mLimitCount = Integer.MAX_VALUE;
    // 视频时长过滤器
    private Filter<Long> mDurationFilter;

    public VideoMultipleWrapper(Context context) {
        super(context);
    }

    /**
     * 设置已选中的视频列表
     */
    public final VideoMultipleWrapper checkedList(ArrayList<AlbumFile> checked) {
        this.mChecked = checked;
        return this;
    }

    /**
     * 设置已选中的视频列表
     */
    public VideoMultipleWrapper selectCount(@IntRange(from = 1, to = Integer.MAX_VALUE) int count) {
        this.mLimitCount = count;
        return this;
    }

    /**
     * 设置视频时长过滤（只显示符合时长的视频）
     */
    public VideoMultipleWrapper filterDuration(Filter<Long> filter) {
        this.mDurationFilter = filter;
        return this;
    }

    /**
     * 启动视频多选页面
     */
    @Override
    public void start() {
        // 传递过滤器和回调
        AlbumActivity.sSizeFilter = mSizeFilter;
        AlbumActivity.sMimeFilter = mMimeTypeFilter;
        AlbumActivity.sDurationFilter = mDurationFilter;
        AlbumActivity.sResult = mResult;
        AlbumActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, AlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked);
        // 功能 = 只选视频
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_VIDEO);
        // 模式 = 多选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE);
        // 其他配置
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount);
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera);
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount);
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility);
        // 相机录制视频的参数（父类传来的）
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality);
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration);
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes);
        mContext.startActivity(intent);
    }

}