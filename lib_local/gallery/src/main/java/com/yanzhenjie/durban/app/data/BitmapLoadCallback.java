package com.yanzhenjie.durban.app.data;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.yanzhenjie.durban.model.ExifInfo;

/**
 * 图片加载回调接口
 * 作用：监听图片加载的 成功 / 失败 结果
 */
public interface BitmapLoadCallback {

    /**
     * 图片加载成功
     * @param bitmap 加载完成的图片对象
     * @param exifInfo 图片的EXIF信息（旋转角度、宽高、方向等）
     */
    void onSuccessfully(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo);

    /**
     * 图片加载失败（文件不存在、图片损坏、内存不足等）
     */
    void onFailure();

}