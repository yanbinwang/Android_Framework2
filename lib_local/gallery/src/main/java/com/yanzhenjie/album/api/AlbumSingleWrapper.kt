package com.yanzhenjie.album.api;

import android.content.Context;
import android.content.Intent;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.Filter;
import com.yanzhenjie.album.app.album.AlbumActivity;

import java.util.ArrayList;

/**
 * <p>Album wrapper.</p>
 * Created by yanzhenjie on 17-3-29.
 */
public class AlbumSingleWrapper extends BasicChoiceAlbumWrapper<AlbumSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile> {
    private Filter<Long> mDurationFilter;

    public AlbumSingleWrapper(Context context) {
        super(context);
    }

    /**
     * Filter video duration.
     *
     * @param filter filter.
     */
    public AlbumSingleWrapper filterDuration(Filter<Long> filter) {
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
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_ALBUM);
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