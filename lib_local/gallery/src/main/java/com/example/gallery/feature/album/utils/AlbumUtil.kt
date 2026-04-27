package com.example.gallery.feature.album.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.example.gallery.feature.album.provider.CameraFileProvider
import com.example.gallery.utils.MediaUtil.randomMediaPath
import java.io.File
import java.util.Locale

/**
 * 相册工具类
 * 功能：文件路径、拍照、录视频、时间格式化、MD5、Drawable 着色、时间转换等
 * Android 从 10 开始，定义了「公共媒体目录」 所有应用都可以自由写入，不需要权限 (/storage/emulated/0/DCIM)
 * 公共媒体目录 -> DCIM / Pictures / Download / Movies
 */
object AlbumUtil {
    // 相册缓存文件夹名称
    private const val CACHE_DIRECTORY = "AlbumCache"

    /**
     * 获取本应用的专属缓存目录
     */
    @JvmStatic
    fun getAlbumCacheDir(context: Context): File {
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
     * 调用系统相机拍照
     */
    @JvmStatic
    fun takeImage(activity: Activity, requestCode: Int, outPath: File) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri = getUri(activity, outPath)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 调用系统相机录像
     */
    @JvmStatic
    fun takeVideo(activity: Activity, requestCode: Int, outPath: File, @IntRange(from = 0, to = 1) quality: Int, @IntRange(from = 1) duration: Long, @IntRange(from = 1) limitBytes: Long) {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val uri = getUri(activity, outPath)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration)
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limitBytes)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * 根据文件获取 Uri（兼容 7.0 FileProvider）
     */
    private fun getUri(context: Context, outPath: File): Uri {
        val uri: Uri
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(outPath)
        } else {
            uri = CameraFileProvider.getUriForFile(context, outPath)
        }
        return uri
    }

    /**
     * 生成随机 JPG 路径
     */
    @JvmStatic
    fun randomJPGPath(): String {
        val bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return randomJPGPath(bucket)
    }

    /**
     * 生成随机 JPG 路径
     */
    @JvmStatic
    fun randomJPGPath(context: Context): String {
        // 判断 SD 卡是否正常挂载（手机存储是否可用）
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            // SD 卡不可用（极少见）→ 存到 APP 自身缓存目录（/data/data/你的应用包名/cache/）
            return randomJPGPath(getAlbumCacheDir(context))
        }
        return randomJPGPath()
    }

    /**
     * 在指定目录生成随机 JPG 路径
     */
    @JvmStatic
    fun randomJPGPath(bucket: File?): String {
        return randomMediaPath(bucket, ".jpg")
    }

    /**
     * 生成随机 MP4 路径
     */
    @JvmStatic
    fun randomMP4Path(): String {
        val bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return randomMP4Path(bucket)
    }

    /**
     * 生成随机 MP4 路径
     */
    @JvmStatic
    fun randomMP4Path(context: Context): String {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return randomMP4Path(getAlbumCacheDir(context))
        }
        return randomMP4Path()
    }

    /**
     * 在指定目录生成随机 MP4 路径
     */
    @JvmStatic
    fun randomMP4Path(bucket: File?): String {
        return randomMediaPath(bucket, ".mp4")
    }

    /**
     * 获取文件 MimeType
     */
    @JvmStatic
    fun getMimeType(url: String?): String {
        val extension = getExtension(url)
        if (!MimeTypeMap.getSingleton().hasExtension(extension)) return ""
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return if (mimeType.isNullOrEmpty()) "" else mimeType
    }

    /**
     * 获取文件后缀
     */
    @JvmStatic
    fun getExtension(url: String?): String {
        val mUrl = if (url.isNullOrEmpty()) "" else url.lowercase(Locale.getDefault())
        val extension = MimeTypeMap.getFileExtensionFromUrl(mUrl)
        return if (extension.isNullOrEmpty()) "" else extension
    }

}