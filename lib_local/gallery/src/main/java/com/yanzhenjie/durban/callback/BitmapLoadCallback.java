package com.yanzhenjie.durban.callback;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.model.ExifInfo;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public interface BitmapLoadCallback {

    void onSuccessfully(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo);

    void onFailure();

}