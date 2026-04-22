package com.example.gallery.feature.durban.utils

import android.graphics.Bitmap.CompressFormat
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

/**
 * 文件操作工具类
 * 作用：目录创建、文件复制、流关闭、生成随机图片文件名
 */
object FileUtil {
    private val random = Random()

    /**
     * 校验并创建目录（不存在则创建）
     */
    @JvmStatic
    fun validateDirectory(path: String) {
        val file = File(path)
        try {
            // 如果是文件，先删除
            if (file.isFile()) {
                file.delete()
            }
            // 目录不存在则创建
            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (_: Exception) {
            throw AssertionError("Directory creation failed.")
        }
    }

    /**
     * 生成随机图片文件名（时间戳 + 随机数）
     */
    @JvmStatic
    fun randomImageName(format: CompressFormat): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmSSS", Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate) + random.nextInt(9000) + "." + format
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

    /**
     * 安全关闭流，避免IOException
     */
    @JvmStatic
    fun close(c: Closeable?) {
        try {
            c?.close()
        } catch (_: IOException) {
        }
    }

}