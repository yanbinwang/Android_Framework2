package com.yanzhenjie.durban.callback;

import androidx.annotation.NonNull;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public interface BitmapCropCallback {

    void onBitmapCropped(@NonNull String imagePath, int imageWidth, int imageHeight);

    void onCropFailure(@NonNull Throwable t);

}