package com.yanzhenjie.durban.model

/**
 * 图片 EXIF 信息实体类
 * 作用：存储图片的旋转方向、旋转角度、翻转信息，解决图片裁剪时倒置/错位问题
 */
data class ExifInfo(
    var exifOrientation: Int = 0, // EXIF 原始方向（1-8，标准相机方向定义）
    var exifDegrees: Int = 0, // 需要旋转的角度（0°、90°、180°、270°）
    var exifTranslation: Int = 0 // 图片平移/翻转参数（内部裁剪视图使用）
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val exifInfo = other as ExifInfo
        if (exifOrientation != exifInfo.exifOrientation) return false
        if (exifDegrees != exifInfo.exifDegrees) return false
        return exifTranslation == exifInfo.exifTranslation
    }

    override fun hashCode(): Int {
        var result = exifOrientation
        result = 31 * result + exifDegrees
        result = 31 * result + exifTranslation
        return result
    }

}