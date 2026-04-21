package com.yanzhenjie.durban.model;

import android.graphics.Bitmap;

/**
 * 图片裁剪参数实体类
 * 作用：统一封装裁剪过程中所有需要的参数，在裁剪核心逻辑中传递使用
 */
public class CropParameters {
    // 图片压缩质量
    private final int mCompressQuality;
    // 裁剪后最大宽高限制
    private final int mMaxResultImageSizeX;
    private final int mMaxResultImageSizeY;
    // 原始图片路径
    private final String mImagePath;
    // 裁剪后保存路径
    private final String mImageOutputPath;
    // 图片压缩格式(JPEG/PNG/WebP)
    private final Bitmap.CompressFormat mCompressFormat;
    // 图片EXIF信息(旋转、方向)
    private final ExifInfo mExifInfo;

    public CropParameters(int maxResultImageSizeX, int maxResultImageSizeY, Bitmap.CompressFormat compressFormat, int compressQuality, String imagePath, String imageOutputPath, ExifInfo exifInfo) {
        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;
        mCompressFormat = compressFormat;
        mCompressQuality = compressQuality;
        mImagePath = imagePath;
        mImageOutputPath = imageOutputPath;
        mExifInfo = exifInfo;
    }

    public int getMaxResultImageSizeX() {
        return mMaxResultImageSizeX;
    }

    public int getMaxResultImageSizeY() {
        return mMaxResultImageSizeY;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return mCompressFormat;
    }

    public int getCompressQuality() {
        return mCompressQuality;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public String getImageOutputPath() {
        return mImageOutputPath;
    }

    public ExifInfo getExifInfo() {
        return mExifInfo;
    }

}