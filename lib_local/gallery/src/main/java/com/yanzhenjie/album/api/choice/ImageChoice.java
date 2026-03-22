package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.ImageMultipleWrapper;
import com.yanzhenjie.album.api.ImageSingleWrapper;

/**
 * 图片选择器总入口
 * 实现 Choice 接口，提供【图片多选 / 图片单选】
 */
public final class ImageChoice implements Choice<ImageMultipleWrapper, ImageSingleWrapper> {
    private final Context mContext;

    public ImageChoice(Context context) {
        mContext = context;
    }

    /**
     * 图片多选
     */
    @Override
    public ImageMultipleWrapper multipleChoice() {
        return new ImageMultipleWrapper(mContext);
    }

    /**
     * 图片单选
     */
    @Override
    public ImageSingleWrapper singleChoice() {
        return new ImageSingleWrapper(mContext);
    }

}