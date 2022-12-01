package com.example.common.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.base.utils.function.value.toSafeLong
import com.example.common.BaseApplication
import com.example.common.constant.Constants
import com.example.common.utils.builder.shortToast
import java.io.*
import java.math.BigDecimal
import java.util.*

/**
 * Created by WangYanBin on 2020/7/1.
 * 文件管理工具类
 */
object FileUtil {

    /**
     * 是否Root-报错或获取失败都为未Root
     */
    @JvmStatic
    fun isRoot(): Boolean {
        var file: File
        val paths = arrayOf("/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/")
        try {
            for (element in paths) {
                file = File(element + "su")
                if (file.exists()) return true
            }
        } catch (_: Exception) {
        }
        return false
    }

    /**
     * 递归完全删除对应文件夹下的所有文件
     */
    @JvmStatic
    fun deleteDirWithFile(dir: File?) {
        if (dir == null || !dir.exists() || !dir.isDirectory) return
        for (file in dir.listFiles()) {
            if (file.isFile) file.delete() //删除所有文件
            else if (file.isDirectory) deleteDirWithFile(file) //递规的方式删除文件夹
        }
        dir.delete() //删除目录本身
    }

    /**
     * 获取整个目录的文件大小
     */
    @JvmStatic
    fun getFileSize(file: File): Long {
        var size: Long = 0
        for (mFile in file.listFiles()) {
            size = if (mFile.isDirectory) {
                size + getFileSize(mFile)
            } else {
                size + mFile.length()
            }
        }
        return size
    }

    /**
     * 获取手机cpu信息-报错或获取失败显示暂无
     */
    @JvmStatic
    fun getCpuInfo(): String {
        try {
            val result = BufferedReader(FileReader("/proc/cpuinfo")).readLine().split(":\\s+".toRegex(), 2).toTypedArray()[1]
            return if ("0" == result) "暂无" else result
        } catch (_: Exception) {
        }
        return "暂无"
    }

}

/**
 * 是否安装了XXX应用
 */
@SuppressLint("QueryPermissionsNeeded")
fun Context.isAvailable(packageName: String): Boolean {
    val packages = packageManager.getInstalledPackages(0)
    for (i in packages.indices) {
        if (packages[i].packageName == packageName) return true
    }
    return false
}

/**
 * 判断手机是否开启开发者模式
 */
fun Context.isAdbEnabled() = (Settings.Secure.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) > 0)

/**
 * 发送广播通知更新数据库
 */
fun Context.insertImageResolver(file: File) {
    MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null)
    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://${file.path}")))
}

/**
 * 获取app的图标
 */
fun Context.getApplicationIcon(): Bitmap? {
    try {
        packageManager.getApplicationIcon(Constants.APPLICATION_ID).apply {
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            draw(canvas)
            return bitmap
        }
    } catch (_: Exception) {
    }
    return null
}

/**
 * 判断目录是否存在,不存则创建
 * 返回对应路径
 */
fun String?.isExistDir(createNewFile: Boolean = true): String {
    this ?: return ""
    val file = File(this)
    if (!file.mkdirs() && createNewFile) file.createNewFile()
    return file.absolutePath
}

/**
 * 删除文件
 */
fun String?.deleteFile() {
    this ?: return
    val file = File(this)
    if (file.isFile && file.exists()) file.delete()
}

/**
 * 删除路径下的所有文件
 */
fun String?.deleteDir() {
    this ?: return
    FileUtil.deleteDirWithFile(File(this))
}

/**
 * 读取文件到文本（文本，找不到文件或读取错返回null）
 */
fun String?.readTxt(): String {
    this ?: return ""
    val file = File(this)
    if (file.exists()) {
        try {
            val stringBuilder = StringBuilder()
            var str: String?
            val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file)))
            while (bufferedReader.readLine().also { str = it } != null) stringBuilder.append(str)
            return stringBuilder.toString()
        } catch (_: Exception) {
        }
    }
    return ""
}

/**
 * 将当前文件拷贝一份到目标路径
 */
@Throws(IOException::class)
fun File.copyFile(destFile: File) {
    if (!destFile.exists()) destFile.createNewFile()
    FileInputStream(this).channel.use { source ->
        FileOutputStream(destFile).channel.use { destination ->
            destination.transferFrom(source, 0, source.size())
        }
    }
}

@Throws(IOException::class)
fun String.copyFile(destSouth: String) {
    File(this).copyFile(File(destSouth))
}

/**
 * 获取对应大小的文字
 */
fun File?.getSizeFormat(): String {
    this ?: return ""
    return length().getSizeFormat()
}

fun String?.getSizeFormat(): String {
    this ?: return ""
    return File(this).getSizeFormat()
}

fun Number?.getSizeFormat(): String {
    this ?: return ""
    val byteResult = this.toSafeLong() / 1024
    if (byteResult < 1) return "<1K"
    val kiloByteResult = byteResult / 1024
    if (kiloByteResult < 1) return "${BigDecimal(byteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}K"
    val mByteResult = kiloByteResult / 1024
    if (mByteResult < 1) return "${BigDecimal(kiloByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}M"
    val gigaByteResult = mByteResult / 1024
    if (gigaByteResult < 1) return "${BigDecimal(mByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}GB"
    val teraByteResult = BigDecimal(gigaByteResult)
    return "${teraByteResult.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}TB"
}