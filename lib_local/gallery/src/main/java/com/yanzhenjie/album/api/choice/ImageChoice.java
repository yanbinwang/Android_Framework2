package com.yanzhenjie.album.api.choice;

import android.content.Context;

import com.yanzhenjie.album.api.ImageMultipleWrapper;
import com.yanzhenjie.album.api.ImageSingleWrapper;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public final class ImageChoice implements Choice<ImageMultipleWrapper, ImageSingleWrapper> {
    private Context mContext;

    public ImageChoice(Context context) {
        mContext = context;
    }

    @Override
    public ImageMultipleWrapper multipleChoice() {
        return new ImageMultipleWrapper(mContext);
    }

    @Override
    public ImageSingleWrapper singleChoice() {
        return new ImageSingleWrapper(mContext);
    }

}