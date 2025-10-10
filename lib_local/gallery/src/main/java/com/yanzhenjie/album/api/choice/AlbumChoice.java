package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.AlbumMultipleWrapper;
import com.yanzhenjie.album.api.AlbumSingleWrapper;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public final class AlbumChoice implements Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
    private Context mContext;

    public AlbumChoice(Context context) {
        mContext = context;
    }

    @Override
    public AlbumMultipleWrapper multipleChoice() {
        return new AlbumMultipleWrapper(mContext);
    }

    @Override
    public AlbumSingleWrapper singleChoice() {
        return new AlbumSingleWrapper(mContext);
    }

}