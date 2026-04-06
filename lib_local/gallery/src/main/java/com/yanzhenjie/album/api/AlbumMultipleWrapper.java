package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.callback.Filter;
import com.yanzhenjie.album.app.album.AlbumActivity;

import java.util.ArrayList;

/**
 * 图片 + 视频 **多选模式** 封装
 * 继承自：BasicChoiceAlbumWrapper（混合选择器父类）
 * 功能：
 * 对外提供链式调用，最终跳转到 AlbumActivity 多选页面
 */
public class AlbumMultipleWrapper extends BasicChoiceAlbumWrapper<AlbumMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>> {
    // 最大选择数量，默认无限制
    private int mLimitCount = Integer.MAX_VALUE;
    // 视频时长过滤器
    private Filter<Long> mDurationFilter;

    public AlbumMultipleWrapper(Context context) {
        super(context);
    }

    /**
     * 设置已经选中的图片列表（用于编辑）
     */
    public final AlbumMultipleWrapper checkedList(ArrayList<AlbumFile> checked) {
        this.mChecked = checked;
        return this;
    }

    /**
     * 设置最大可选数量
     */
    public AlbumMultipleWrapper selectCount(@IntRange(from = 1, to = Integer.MAX_VALUE) int count) {
        this.mLimitCount = count;
        return this;
    }

    /**
     * 过滤视频时长
     */
    public AlbumMultipleWrapper filterDuration(Filter<Long> filter) {
        this.mDurationFilter = filter;
        return this;
    }

    /**
     * 【核心方法】
     * 启动相册多选页面
     * 把所有配置通过 Intent 传给 AlbumActivity
     */
    @Override
    public void start() {
        // 把过滤器、回调 赋值给静态变量，让 AlbumActivity 可以拿到
        AlbumActivity.sSizeFilter = mSizeFilter;
        AlbumActivity.sMimeFilter = mMimeTypeFilter;
        AlbumActivity.sDurationFilter = mDurationFilter;
        AlbumActivity.sResult = mResult;
        AlbumActivity.sCancel = mCancel;
        // 跳转到相册主页面
        Intent intent = new Intent(mContext, AlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked);
        // 功能 = 混合选择（图片+视频）
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_ALBUM);
        // 模式 = 多选
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE);
        // 列数
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount);
        // 是否显示相机
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera);
        // 最大选择数量
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount);
        // 过滤后是否显示
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