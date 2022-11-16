package com.example.common.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.base.utils.function.value.DateFormat.EN_YMDHMS
import com.example.base.utils.function.value.getDateTime
import com.example.base.utils.logE
import com.example.common.constant.Constants
import com.example.common.utils.builder.shortToast
import java.io.*
import java.lang.ref.SoftReference
import java.math.BigDecimal
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    /**
     * 将指定路径下的所有文件打成压缩包
     * File fileDir = new File(rootDir + "/DCIM/Screenshots");
     * File zipFile = new File(rootDir + "/" + taskId + ".zip");
     *
     * @param srcFilePath 要压缩的文件或文件夹路径
     * @param zipFilePath 压缩完成的Zip路径
     */
    @JvmStatic
    @Throws(Exception::class)
    fun zipFolder(srcFilePath: String, zipFilePath: String) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFilePath))
        //创建文件
        val file = File(srcFilePath)
        //压缩
        zipFiles(file.parent + File.separator, file.name, outZip)
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    @Throws(Exception::class)
    private fun zipFiles(folderPath: String, fileName: String, zipOutputSteam: ZipOutputStream?) {
        " \n压缩路径:$folderPath\n压缩文件名:$fileName".logE("FileUtil")
        if (zipOutputSteam == null) return
        val file = File(folderPath + fileName)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileName)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            val fileList = file.list()
            //没有子文件和压缩
            if (fileList.isEmpty()) {
                val zipEntry = ZipEntry(fileName + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            //子文件和递归
            for (i in fileList.indices) {
                zipFiles("$folderPath$fileName/", fileList[i], zipOutputSteam)
            }
        }
    }

    /**
     * bitmap->存储的bitmap
     * root->图片保存路径
     * fileName->图片名称（扣除jpg和png的后缀）
     * formatJpg->确定图片类型
     * quality->压缩率
     * clear->是否清除本地路径
     */
    @JvmOverloads
    @JvmStatic
    fun compressBit(bitmap: Bitmap, root: String = "${Constants.APPLICATION_FILE_PATH}/图片", fileName: String = EN_YMDHMS.getDateTime(Date()), delete: Boolean = false, formatJpg: Boolean = true, quality: Int = 100): Boolean {
        val storeDir = File(root)
        if (!storeDir.mkdirs()) storeDir.createNewFile()//需要权限
        if (delete) storeDir.absolutePath.deleteDir()//删除路径下所有文件
        val file = File(storeDir, "${fileName}${if (formatJpg) ".jpg" else ".png"}")
        try {
            //通过io流的方式来压缩保存图片
            val fileOutputStream = FileOutputStream(file)
            val result = bitmap.compress(if (formatJpg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, quality, fileOutputStream)//png的话100不响应，但是可以维持图片透明度
            fileOutputStream.flush()
            fileOutputStream.close()
            return result
        } catch (_: Exception) {
        }
        bitmap.recycle()
        return false
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
fun Context.noticeAlbum(file:File){
    MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null)
    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.path)))
}

/**
 * 打开压缩包
 */
fun Context.openZip(filePath: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    //判断是否是AndroidN以及更高的版本
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val file = File(filePath)
        val contentUri = FileProvider.getUriForFile(this, "${Constants.APPLICATION_ID}.fileProvider", file)
        intent.setDataAndType(contentUri, "application/x-zip-compressed")
    } else {
        intent.setDataAndType(Uri.parse("file://$filePath"), "application/x-zip-compressed")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

/**
 * 打开world
 */
fun Context.openWorld(filePath: String) {
    val file = File(filePath)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        uri = FileProvider.getUriForFile(this, "${Constants.APPLICATION_ID}.fileProvider", file)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
    } else uri = Uri.parse("file://$file")
    intent.setDataAndType(uri, "application/msword")
    startActivity(intent)
}

/**
 * 发送文件
 * image -> 图片
 */
@JvmOverloads
fun Context.sendFile(filePath: String, type: String? = "*/*") {
    val file = File(filePath)
    if (!file.exists()) {
        "文件路径错误".shortToast()
        return
    }
    val intent = Intent(Intent.ACTION_SEND)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, "${Constants.APPLICATION_ID}.fileProvider", file))
    } else {
        intent.putExtra(Intent.EXTRA_STREAM, file)
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.type = type//此处可发送多种文件
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(Intent.createChooser(intent, "分享文件"))
}

/**
 * 获取安装跳转的行为
 */
fun Context.getSetupApk(apkFilePath: String): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    //判断是否是AndroidN以及更高的版本
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val file = File(apkFilePath)
        val contentUri = FileProvider.getUriForFile(this, Constants.APPLICATION_ID + ".fileProvider", file)
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
    } else {
        intent.setDataAndType(Uri.parse("file://$apkFilePath"), "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return intent
}

/**
 * 获取app的图标
 */
fun Context.getApplicationIcon(): Bitmap? {
    try {
        val drawable = packageManager.getApplicationIcon(Constants.APPLICATION_ID)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
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
fun String?.readText(): String {
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
fun File?.getFormatSize(): String {
    this ?: return ""
    return length().getFormatSize()
}

fun String?.getFormatSize(): String {
    this ?: return ""
    return File(this).getFormatSize()
}

fun Long?.getFormatSize(): String {
    this ?: return ""
    val byteResult = this / 1024
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