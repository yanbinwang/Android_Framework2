package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.AlbumMultipleWrapper;
import com.yanzhenjie.album.api.AlbumSingleWrapper;

/**
 * 相册混合选择入口（图片 + 视频 都能选）
 * 实现 Choice 接口，提供 多选 / 单选 两种能力
 */
public final class AlbumChoice implements Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {
    private final Context mContext;

    public AlbumChoice(Context context) {
        mContext = context;
    }

    /**
     * 打开 图片+视频 多选
     */
    @Override
    public AlbumMultipleWrapper multipleChoice() {
        return new AlbumMultipleWrapper(mContext);
    }

    /**
     * 打开 图片+视频 单选
     */
    @Override
    public AlbumSingleWrapper singleChoice() {
        return new AlbumSingleWrapper(mContext);
    }

}