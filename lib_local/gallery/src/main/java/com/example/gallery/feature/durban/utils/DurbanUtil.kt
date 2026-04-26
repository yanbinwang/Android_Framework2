package com.example.gallery.feature.durban.utils

import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 文件操作工具类
 * 作用：目录创建、文件复制、流关闭、生成随机图片文件名
 */
object DurbanUtil {

    /**
     * 校验并创建目录（不存在则创建）
     */
    @JvmStatic
    fun ensureFilePath(path: String) {
        val file = File(path)
        try {
            // 如果是文件，先删除
            if (file.isFile) {
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
    fun randomFileName(format: CompressFormat): String {
        return "${getNowDateTime()}_${getMD5ForString(UUID.randomUUID().toString())}.${format.name.lowercase()}"
    }

    /**
     * 获取当前时间格式化字符串
     */
    private fun getNowDateTime(): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    /**
     * 获取字符串 MD5
     */
    private fun getMD5ForString(content: String): String {
        val md5Buffer = StringBuilder()
        try {
            val digest = MessageDigest.getInstance("MD5")
            val tempBytes = digest.digest(content.toByteArray())
            var digital: Int
            for (tempByte in tempBytes) {
                digital = tempByte.toInt()
                if (digital < 0) {
                    digital += 256
                }
                if (digital < 16) {
                    md5Buffer.append("0")
                }
                md5Buffer.append(Integer.toHexString(digital))
            }
        } catch (_: Exception) {
            return content.hashCode().toString()
        }
        return md5Buffer.toString()
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
     * 给 Drawable 设置着色
     */
    @JvmStatic
    fun setDrawableTint(drawable: Drawable, @ColorInt color: Int): Drawable  {
        return drawable.mutate().apply { setTint(color) }
    }

}