package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.VideoMultipleWrapper;
import com.yanzhenjie.album.api.VideoSingleWrapper;

/**
 * 视频选择器总入口
 * 实现 Choice 接口，提供【视频多选 / 视频单选】
 */
public final class VideoChoice implements Choice<VideoMultipleWrapper, VideoSingleWrapper> {
    private final Context mContext;

    public VideoChoice(Context context) {
        mContext = context;
    }

    /**
     * 视频多选
     */
    @Override
    public VideoMultipleWrapper multipleChoice() {
        return new VideoMultipleWrapper(mContext);
    }

    /**
     * 视频单选
     */
    @Override
    public VideoSingleWrapper singleChoice() {
        return new VideoSingleWrapper(mContext);
    }

}