package com.example.gallery.feature.durban.utils

import android.content.Context
import android.graphics.Bitmap.CompressFormat
import com.example.gallery.utils.MediaUtil.randomMediaPath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * 文件操作工具类
 * 作用：目录创建、文件复制、流关闭、生成随机图片文件名
 */
object DurbanUtil {
    // 相册缓存文件夹名称
    private const val CACHE_DIRECTORY = "DurbanCache"

    /**
     * 获取本应用的专属缓存目录
     */
    @JvmStatic
    fun getDurbanCacheDir(context: Context): File {
        // 定义缓存路径
        val cacheDir = File(context.cacheDir, CACHE_DIRECTORY)
        // 如果不存在，递归创建文件夹
        if (!cacheDir.exists()) {
            // 一定要带 s，多级目录也能创建
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * 生成随机图片文件名（时间戳 + 随机数）
     */
    @JvmStatic
    fun randomCropPath(bucket: File?, format: CompressFormat): String {
        return randomMediaPath(bucket, ".${format.name.lowercase()}")
    }

    /**
     * 文件复制（无需裁剪时直接复制原图）
     */
    @JvmStatic
    fun copyFile(pathFrom: String, pathTo: String) {
        try {
            FileInputStream(pathFrom).use { input ->
                FileOutputStream(pathTo).use { output ->
                    val buffer = ByteArray(2048)
                    var len: Int
                    while ((input.read(buffer).also { len = it }) != -1) {
                        output.write(buffer, 0, len)
                    }
                }
            }
        } catch (e: IOException) {
            throw AssertionError(e)
        }
    }

}