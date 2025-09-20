package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.Filter;
import com.yanzhenjie.album.app.album.AlbumActivity;

import java.util.ArrayList;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public final class VideoSingleWrapper extends BasicChoiceVideoWrapper<VideoSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile> {
    private Filter<Long> mDurationFilter;

    public VideoSingleWrapper(Context context) {
        super(context);
    }

    /**
     * Filter video duration.
     *
     * @param filter filter.
     */
    public VideoSingleWrapper filterDuration(Filter<Long> filter) {
        this.mDurationFilter = filter;
        return this;
    }

    @Override
    public void start() {
        AlbumActivity.sSizeFilter = mSizeFilter;
        AlbumActivity.sMimeFilter = mMimeTypeFilter;
        AlbumActivity.sDurationFilter = mDurationFilter;
        AlbumActivity.sResult = mResult;
        AlbumActivity.sCancel = mCancel;
        Intent intent = new Intent(mContext, AlbumActivity.class);
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget);
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_VIDEO);
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE);
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount);
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera);
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1);
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility);
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality);
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration);
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes);
        mContext.startActivity(intent);
    }

}