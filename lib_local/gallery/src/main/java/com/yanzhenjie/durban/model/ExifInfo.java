package com.yanzhenjie.durban.model;

/**
 * 图片 EXIF 信息实体类
 * 作用：存储图片的旋转方向、旋转角度、翻转信息，解决图片裁剪时倒置/错位问题
 */
public class ExifInfo {
    // EXIF 原始方向（1-8，标准相机方向定义）
    private int mExifOrientation;
    // 需要旋转的角度（0°、90°、180°、270°）
    private int mExifDegrees;
    // 图片平移/翻转参数（内部裁剪视图使用）
    private int mExifTranslation;

    public ExifInfo(int exifOrientation, int exifDegrees, int exifTranslation) {
        mExifOrientation = exifOrientation;
        mExifDegrees = exifDegrees;
        mExifTranslation = exifTranslation;
    }

    public int getExifOrientation() {
        return mExifOrientation;
    }

    public int getExifDegrees() {
        return mExifDegrees;
    }

    public int getExifTranslation() {
        return mExifTranslation;
    }

    public void setExifOrientation(int exifOrientation) {
        mExifOrientation = exifOrientation;
    }

    public void setExifDegrees(int exifDegrees) {
        mExifDegrees = exifDegrees;
    }

    public void setExifTranslation(int exifTranslation) {
        mExifTranslation = exifTranslation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExifInfo exifInfo = (ExifInfo) o;
        if (mExifOrientation != exifInfo.mExifOrientation) return false;
        if (mExifDegrees != exifInfo.mExifDegrees) return false;
        return mExifTranslation == exifInfo.mExifTranslation;
    }

    @Override
    public int hashCode() {
        int result = mExifOrientation;
        result = 31 * result + mExifDegrees;
        result = 31 * result + mExifTranslation;
        return result;
    }

}