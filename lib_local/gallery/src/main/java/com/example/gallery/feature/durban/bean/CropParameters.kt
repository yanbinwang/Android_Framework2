package com.example.gallery.feature.durban.bean

import android.graphics.Bitmap

/**
 * 图片裁剪参数实体类
 * 作用：统一封装裁剪过程中所有需要的参数，在裁剪核心逻辑中传递使用
 */
data class CropParameters(
    var maxResultImageSizeX: Int = 0, // 裁剪后最大宽限制
    var maxResultImageSizeY: Int = 0, // 裁剪后最大高限制
    var compressFormat: Bitmap.CompressFormat? = null, // 图片压缩格式(JPEG/PNG/WebP)
    var compressQuality: Int = 0, // 图片压缩质量
    var imagePath: String? = null, // 原始图片路径
    var imageOutputPath: String? = null, // 裁剪后保存路径
    var exifInfo: ExifInfo? = null // 图片EXIF信息(旋转、方向)
)