package com.yanzhenjie.album.util

import android.R
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
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.graphics.drawable.DrawableCompat
import com.yanzhenjie.album.provider.CameraFileProvider
import com.yanzhenjie.album.widget.divider.Api20ItemDivider
import com.yanzhenjie.album.widget.divider.Api21ItemDivider
import com.yanzhenjie.album.widget.divider.Divider
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * <p>Helper for album.</p>
 * Created by Yan Zhenjie on 2016/10/30.
 */
object AlbumUtils {
    private const val CACHE_DIRECTORY = "AlbumCache"

    /**
     * Get a writable root directory.
     *
     * @param context context.
     * @return [File].
     */
    @JvmStatic
    fun getAlbumRootPath(context: Context): File {
        return if (sdCardIsAvailable()) {
            File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY)
        } else {
            File(context.filesDir, CACHE_DIRECTORY)
        }
    }

    /**
     * SD card is available.
     *
     * @return true when available, other wise is false.
     */
    @JvmStatic
    fun sdCardIsAvailable(): Boolean {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().canWrite()
        } else {
            false
        }
    }

    /**
     * Take picture.
     *
     * @param activity    activity.
     * @param requestCode code, see [`onActivityResult`(int, int, Intent)][Activity].
     * @param outPath     file path.
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
     * Take video.
     *
     * @param activity    activity.
     * @param requestCode code, see [`onActivityResult`(int, int, Intent)][Activity].
     * @param outPath     file path.
     * @param quality     currently value 0 means low quality, suitable for MMS messages, and  value 1 means high quality.
     * @param duration    specify the maximum allowed recording duration in seconds.
     * @param limitBytes  specify the maximum allowed size.
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
     * Generates an externally accessed URI based on path.
     *
     * @param context context.
     * @param outPath file path.
     * @return the uri address of the file.
     */
    @JvmStatic
    fun getUri(context: Context, outPath: File): Uri {
        val uri: Uri
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(outPath)
        } else {
            uri = getUriForFile(context, CameraFileProvider.getProviderName(context), outPath)
        }
        return uri
    }

    /**
     * Generate a random jpg file path.
     *
     * @return file path.
     */
    @Deprecated("use {@link #randomJPGPath(Context)} instead.")
    @JvmStatic
    fun randomJPGPath(): String {
        val bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        return randomJPGPath(bucket)
    }

    /**
     * Generate a random jpg file path.
     *
     * @param context context.
     * @return file path.
     */
    @JvmStatic
    fun randomJPGPath(context: Context): String {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return randomJPGPath(context.cacheDir)
        }
        return randomJPGPath()
    }

    /**
     * Generates a random jpg file path in the specified directory.
     *
     * @param bucket specify the directory.
     * @return file path.
     */
    @JvmStatic
    fun randomJPGPath(bucket: File): String {
        return randomMediaPath(bucket, ".jpg")
    }

    /**
     * Generate a random mp4 file path.
     *
     * @return file path.
     */
    @Deprecated("use {@link #randomMP4Path(Context)} instead.")
    @JvmStatic
    fun randomMP4Path(): String {
        val bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return randomMP4Path(bucket)
    }

    /**
     * Generate a random mp4 file path.
     *
     * @param context context.
     * @return file path.
     */
    @JvmStatic
    fun randomMP4Path(context: Context): String {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return randomMP4Path(context.cacheDir)
        }
        return randomMP4Path()
    }

    /**
     * Generates a random mp4 file path in the specified directory.
     *
     * @return file path.
     */
    @JvmStatic
    fun randomMP4Path(bucket: File): String {
        return randomMediaPath(bucket, ".mp4")
    }

    /**
     * Generates a random file path using the specified suffix name in the specified directory.
     *
     * @param bucket    specify the directory.
     * @param extension extension.
     * @return file path.
     */
    private fun randomMediaPath(bucket: File, extension: String?): String {
        if (bucket.exists() && bucket.isFile()) bucket.delete()
        if (!bucket.exists()) bucket.mkdirs()
        val outFilePath = getNowDateTime("yyyyMMdd_HHmmssSSS") + "_" + getMD5ForString(UUID.randomUUID().toString()) + extension
        val file = File(bucket, outFilePath)
        return file.absolutePath
    }

    /**
     * Format the current time in the specified format.
     *
     * @return the time string.
     */
    @JvmStatic
    fun getNowDateTime(format: String): String {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    /**
     * Get the mime type of the file in the url.
     *
     * @param url file url.
     * @return mime type.
     */
    @JvmStatic
    fun getMimeType(url: String?): String {
        val extension = getExtension(url)
        if (!MimeTypeMap.getSingleton().hasExtension(extension)) return ""
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return if (mimeType.isNullOrEmpty()) "" else mimeType
    }

    /**
     * Get the file extension in url.
     *
     * @param url file url.
     * @return extension.
     */
    @JvmStatic
    fun getExtension(url: String?): String {
        var mUrl = url
        mUrl = if (mUrl.isNullOrEmpty()) "" else mUrl.lowercase(Locale.getDefault())
        val extension = MimeTypeMap.getFileExtensionFromUrl(mUrl)
        return (if (extension.isNullOrEmpty()) "" else extension)
    }

    /**
     * Specifies a tint for `drawable`.
     *
     * @param drawable drawable target, mutate.
     * @param color    color.
     */
    @JvmStatic
    fun setDrawableTint(drawable: Drawable, @ColorInt color: Int) {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable.mutate()), color)
    }

    /**
     * Specifies a tint for `drawable`.
     *
     * @param drawable drawable target, mutate.
     * @param color    color.
     * @return convert drawable.
     */
    @JvmStatic
    fun getTintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable.mutate())
        DrawableCompat.setTint(drawable, color)
        return drawable
    }

    /**
     * [ColorStateList].
     *
     * @param normal    normal color.
     * @param highLight highLight color.
     * @return [ColorStateList].
     */
    @JvmStatic
    fun getColorStateList(@ColorInt normal: Int, @ColorInt highLight: Int): ColorStateList {
        val states = arrayOfNulls<IntArray>(6)
        states[0] = intArrayOf(R.attr.state_checked)
        states[1] = intArrayOf(R.attr.state_pressed)
        states[2] = intArrayOf(R.attr.state_selected)
        states[3] = intArrayOf()
        states[4] = intArrayOf()
        states[5] = intArrayOf()
        val colors = intArrayOf(highLight, highLight, highLight, normal, normal, normal)
        return ColorStateList(states, colors)
    }

    /**
     * Change part of the color of CharSequence.
     *
     * @param content content text.
     * @param start   start index.
     * @param end     end index.
     * @param color   color.
     * @return `SpannableString`.
     */
    @JvmStatic
    fun getColorText(content: CharSequence, start: Int, end: Int, @ColorInt color: Int): SpannableString {
        val stringSpan = SpannableString(content)
        stringSpan.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return stringSpan
    }

    /**
     * Return a color-int from alpha, red, green, blue components.
     *
     * @param color color.
     * @param alpha alpha, alpha component [0..255] of the color.
     */
    @ColorInt
    @JvmStatic
    fun getAlphaColor(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * Generate divider.
     *
     * @param color color.
     * @return [Divider].
     */
    @JvmStatic
    fun getDivider(@ColorInt color: Int): Divider {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Api21ItemDivider(color)
        }
        return Api20ItemDivider(color)
    }

    /**
     * Time conversion.
     *
     * @param duration ms.
     * @return such as: `00:00:00`, `00:00`.
     */
    @JvmStatic
    fun convertDuration(@IntRange(from = 1) duration: Long): String {
        var duration = duration
        duration /= 1000
        val hour = (duration / 3600).toInt()
        val minute = ((duration - hour * 3600) / 60).toInt()
        val second = (duration - hour * 3600 - minute * 60).toInt()
        var hourValue = ""
        if (hour > 0) {
            hourValue = if (hour >= 10) {
                hour.toString()
            } else {
                "0$hour"
            }
            hourValue += ":"
        }
        var minuteValue = if (minute > 0) {
            if (minute >= 10) {
                minute.toString()
            } else {
                "0$minute"
            }
        } else {
            "00"
        }
        minuteValue += ":"
        val secondValue = if (second > 0) {
            if (second >= 10) {
                second.toString()
            } else {
                "0$second"
            }
        } else {
            "00"
        }
        return hourValue + minuteValue + secondValue
    }

    /**
     * Get the MD5 value of string.
     *
     * @param content the target string.
     * @return the MD5 value.
     */
    @JvmStatic
    fun getMD5ForString(content: String): String {
        val md5Buffer = StringBuilder()
        try {
            val digest = MessageDigest.getInstance("MD5")
            val tempBytes = digest.digest(content.toByteArray())
            var digital: Int
            for (i in tempBytes.indices) {
                digital = tempBytes[i].toInt()
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

}