package com.yanzhenjie.durban.model;

import android.graphics.Bitmap;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public class CropParameters {
    private int mCompressQuality;
    private int mMaxResultImageSizeX, mMaxResultImageSizeY;
    private String mImagePath;
    private String mImageOutputPath;
    private Bitmap.CompressFormat mCompressFormat;
    private ExifInfo mExifInfo;

    public CropParameters(int maxResultImageSizeX, int maxResultImageSizeY, Bitmap.CompressFormat compressFormat, int compressQuality, String imagePath, String imageOutputPath, ExifInfo exifInfo) {
        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;
        mCompressFormat = compressFormat;
        mCompressQuality = compressQuality;
        this.mImagePath = imagePath;
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