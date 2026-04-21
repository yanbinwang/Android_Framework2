package com.yanzhenjie.durban.app.data;

import androidx.annotation.NonNull;

/**
 * 图片裁剪回调接口
 * 作用：监听图片裁剪的 成功 / 失败 结果
 */
public interface BitmapCropCallback {

    /**
     * 图片裁剪成功
     * @param imagePath  裁剪后的图片保存路径
     * @param imageWidth 裁剪后的图片宽度
     * @param imageHeight 裁剪后的图片高度
     */
    void onBitmapCropped(@NonNull String imagePath, int imageWidth, int imageHeight);

    /**
     * 裁剪失败（比如权限不足、内存不足、图片损坏）
     */
    void onCropFailure(@NonNull Throwable t);

}