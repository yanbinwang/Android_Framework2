package com.example.common.utils.file

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.base.utils.DateUtil
import com.example.common.constant.Constants
import java.io.*
import java.lang.ref.SoftReference
import java.text.DecimalFormat
import java.util.*

/**
 * Created by WangYanBin on 2020/7/1.
 * 文件管理工具类
 */
object FileUtil {

    /**
     * 是否安装了XXX应用
     */
    @JvmStatic
    fun isAvailable(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val packageInfos = packageManager.getInstalledPackages(0)
        for (i in packageInfos.indices) {
            val pn = packageInfos[i].packageName
            if (pn == packageName) return true
        }
        return false
    }

    /**
     * 是否Root-报错或获取失败都为未Root
     */
    @JvmStatic
    fun isRoot(): Boolean {
        var file :File
        val paths = arrayOf("/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/")
        try {
            for (element in paths) {
                file = File(element + "su")
                if (file.exists()) return true
            }
        } catch (ignored: Exception) {
        }
        return false
    }

    /**
     * 判断手机是否开启开发者模式
     */
    @JvmStatic
    fun isAdbEnabled(context: Context) = (Settings.Secure.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) > 0)

    /**
     * 判断下载目录是否存在
     */
    @JvmStatic
    @Throws(IOException::class)
    fun isExistDir(filePath: String): String {
        val downloadFile = File(filePath)
        if (!downloadFile.mkdirs()) downloadFile.createNewFile()
        return downloadFile.absolutePath
    }

    /**
     * 复制文件
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(srcFile: String, destFile: String) = copyFile(File(srcFile), File(destFile))

    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(srcFile: File, destFile: File) {
        if (!destFile.exists()) destFile.createNewFile()
        FileInputStream(srcFile).channel.use { source ->
            FileOutputStream(destFile).channel.use { destination ->
                destination.transferFrom(source, 0, source.size())
            }
        }
    }

    /**
     * 删除文件
     */
    @JvmStatic
    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.isFile && file.exists()) file.delete()
    }

    /**
     * 删除本地路径下的所有文件
     */
    @JvmStatic
    fun deleteDir(filePath: String) {
        val dir = File(filePath)
        deleteDirWithFile(dir)
    }

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
     * 将bitmap存成文件至指定目录下-读写权限
     * BitmapFactory.decodeResource(resources, R.mipmap.img_qr_code)
     */
    @JvmStatic
    fun saveBitmap(context: Context, bitmap: Bitmap, quality: Int = 100): Boolean {
        return saveBitmap(context, bitmap, Constants.APPLICATION_FILE_PATH + "/图片", true, quality)
    }

    @JvmStatic
    fun saveBitmap(context: Context, bitmap: Bitmap, root: String = Constants.APPLICATION_FILE_PATH + "/图片", formatJpg: Boolean = false, quality: Int = 100): Boolean {
        try {
            val storeDir = File(root)
            if (!storeDir.mkdirs()) storeDir.createNewFile()//需要权限
            val file = File(storeDir, DateUtil.getDateTimeStr(DateUtil.EN_YMDHMS, Date()) + if (formatJpg) ".jpg" else ".png")
            //通过io流的方式来压缩保存图片
            val fileOutputStream = FileOutputStream(file)
            val result = bitmap.compress(if (formatJpg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, quality, fileOutputStream)//png的话100不响应，但是可以维持图片透明度
            fileOutputStream.flush()
            fileOutputStream.close()
            //保存图片后发送广播通知更新数据库
            MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, null)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.path)))
//            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), arrayOf(file.name), null)
//            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
            return result
        } catch (ignored: Exception) {
        } finally {
            bitmap.recycle()
        }
        return false
    }

    /**
     * 读取文件到文本（文本，找不到文件或读取错返回null）
     */
    @JvmStatic
    fun readText(filePath: String): String? {
        val file = File(filePath)
        if (file.exists()) {
            try {
                val stringBuilder = StringBuilder()
                var str: String?
                val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file)))
                while (bufferedReader.readLine().also { str = it } != null) stringBuilder.append(str)
                return stringBuilder.toString()
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    /**
     * 转换文件大小格式
     */
    @JvmStatic
    fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        return when {
            fileS < 1024 -> df.format(fileS.toDouble()) + "B"
            fileS < 1048576 -> df.format(fileS.toDouble() / 1024) + "K"
            fileS < 1073741824 -> df.format(fileS.toDouble() / 1048576) + "M"
            else -> df.format(fileS.toDouble() / 1073741824) + "G"
        }
    }

    /**
     * 获取文件大小
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
     * 获取app的图标
     */
    @JvmStatic
    fun getApplicationIcon(context: Context): Bitmap? {
        try {
            val drawable = context.packageManager.getApplicationIcon(Constants.APPLICATION_ID)
            val bitmap = SoftReference(Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565))
            val canvas = Canvas(bitmap.get()!!)
            //canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap.get()
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 获取安装跳转的行为
     */
    @JvmStatic
    fun getSetupApk(context: Context, apkFilePath: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val file = File(apkFilePath)
            val contentUri = FileProvider.getUriForFile(context, Constants.APPLICATION_ID + ".fileProvider", file)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.parse("file://$apkFilePath"), "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

    /**
     * 获取手机cpu信息-报错或获取失败显示暂无
     */
    @JvmStatic
    fun getCpuInfo(): String {
        try {
            val fr = FileReader("/proc/cpuinfo")
            val br = BufferedReader(fr)
            val text = br.readLine()
            val array = text.split(":\\s+".toRegex(), 2).toTypedArray()
            return array[1]
        } catch (ignored: Exception) {
        }
        return "暂无"
    }

}