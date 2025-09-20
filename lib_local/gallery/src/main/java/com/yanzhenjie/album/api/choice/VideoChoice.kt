package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.VideoMultipleWrapper;
import com.yanzhenjie.album.api.VideoSingleWrapper;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public final class VideoChoice implements Choice<VideoMultipleWrapper, VideoSingleWrapper> {
    private Context mContext;

    public VideoChoice(Context context) {
        mContext = context;
    }

    @Override
    public VideoMultipleWrapper multipleChoice() {
        return new VideoMultipleWrapper(mContext);
    }

    @Override
    public VideoSingleWrapper singleChoice() {
        return new VideoSingleWrapper(mContext);
    }

}