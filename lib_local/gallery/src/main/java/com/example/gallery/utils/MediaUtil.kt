package com.example.gallery.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.example.common.utils.function.ensureDirExists
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 多媒体整合工具类 (公共基础工具)
 * 1) 文件命名（时间戳 + MD5）
 * 2) 视图通用着色、Spannable、ColorStateList
 * 3) 通用工具函数
 */
object MediaUtil {

    /**
     * 生成时间戳+MD5的唯一命名前缀（用于文件/图片命名）
     */
    @JvmStatic
    fun generateUniquePrefix(): String {
        return "${getNowDateTime()}_${getMd5(UUID.randomUUID().toString())}"
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
    private fun getMd5(content: String): String {
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
     * 根据目录和后缀生成随机文件路径
     */
    @JvmStatic
    fun randomMediaPath(bucket: File?, extension: String): String {
        bucket ?: return ""
        // 校验并创建目录（不存在则创建）
        bucket.absolutePath.ensureDirExists().takeIf { it.isNotEmpty() } ?: return ""
        // 输出文件名
        val outFileName = "${generateUniquePrefix()}${extension}"
        // 包装详细文件路径
        val file = File(bucket, outFileName)
        // 返回输出文件详细地址
        return file.absolutePath
    }

    /**
     * 构建按钮/选择器的颜色状态
     */
    @JvmStatic
    fun getColorStateList(@ColorInt normal: Int, @ColorInt highLight: Int): ColorStateList {
        val states = arrayOfNulls<IntArray>(6)
        states[0] = intArrayOf(android.R.attr.state_checked)
        states[1] = intArrayOf(android.R.attr.state_pressed)
        states[2] = intArrayOf(android.R.attr.state_selected)
        states[3] = intArrayOf()
        states[4] = intArrayOf()
        states[5] = intArrayOf()
        val colors = intArrayOf(highLight, highLight, highLight, normal, normal, normal)
        return ColorStateList(states, colors)
    }

    /**
     * 设置字符串部分文字颜色
     */
    @JvmStatic
    fun getColorText(content: CharSequence, start: Int, end: Int, @ColorInt color: Int): SpannableString {
        val stringSpan = SpannableString(content)
        stringSpan.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return stringSpan
    }

    /**
     * 给颜色设置透明度
     */
    @ColorInt
    @JvmStatic
    fun getColorAlpha(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * 给 Drawable 设置着色
     * DrawableCompat.setTint -> 淘汰
     * DrawableCompat.wrap(drawable.mutate()) -> 给旧系统的drawable套一个兼容壳,高版本直接删
     */
    @JvmStatic
    fun setDrawableTint(drawable: Drawable, @ColorInt color: Int): Drawable  {
        return drawable.mutate().apply { setTint(color) }
    }

}