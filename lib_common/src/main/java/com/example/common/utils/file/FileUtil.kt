package com.example.common.utils.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.common.BaseApplication
import com.example.common.constant.Constants
import java.io.*
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by WangYanBin on 2020/7/1.
 * 文件管理工具类
 */
object FileUtil {

    //创建日志和缓存目录
    @JvmStatic
    fun createCacheDir(): String? {
        val file = File(
            BaseApplication.instance.externalCacheDir.toString() +
                    File.separator +
                    "log"
        )
        if (!file.exists()) {
            file.mkdir()
        }
        return file.path
    }

    //复制文件
    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(srcFile: String?, destFile: String?) {
        copyFile(File(srcFile), File(destFile))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(srcFile: File?, destFile: File?) {
        if (!destFile!!.exists()) {
            destFile.createNewFile()
        }

        FileInputStream(srcFile).channel.use { source ->
            FileOutputStream(destFile).channel.use { destination ->
                destination.transferFrom(source, 0, source.size())
            }
        }
    }

    @JvmStatic
    //删除本地路径下的所有文件
    fun deleteDir(filePath: String?) {
        val dir = File(filePath)
        deleteDirWithFile(dir)
    }

    @JvmStatic
    fun deleteDirWithFile(dir: File?) {
        if (dir == null || !dir.exists() || !dir.isDirectory) return
        for (file in dir.listFiles()) {
            if (file.isFile) file.delete() // 删除所有文件
            else if (file.isDirectory) deleteDirWithFile(file) // 递规的方式删除文件夹
        }
        dir.delete() // 删除目录本身
    }

    //判断下载目录是否存在
    @JvmStatic
    @Throws(IOException::class)
    fun isExistDir(filePath: String?): String? {
        val downloadFile = File(filePath)
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile()
        }
        return downloadFile.absolutePath
    }

    //读取文件到文本（文本，找不到文件或读取错返回null）
    @JvmStatic
    fun readText(filePath: String?): String? {
        val f = File(filePath)
        if (f.exists()) {
            try {
                val sb = StringBuilder()
                var s: String?
                val br = BufferedReader(InputStreamReader(FileInputStream(f)))
                while (br.readLine().also { s = it } != null) {
                    sb.append(s)
                }
                return sb.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    //获取文件大小
    @JvmStatic
    fun getFileSize(file: File?): Long {
        var size: Long = 0
        val fileList = file!!.listFiles()
        for (mFile in fileList) {
            size = if (mFile.isDirectory) {
                size + getFileSize(mFile)
            } else {
                size + mFile.length()
            }
        }
        return size
    }

    //转换文件大小格式
    @JvmStatic
    fun formatFileSize(fileS: Long): String? {
        val df = DecimalFormat("#.00")
        val fileSizeString: String
        fileSizeString = when {
            fileS < 1024 -> {
                df.format(fileS.toDouble()) + "B"
            }
            fileS < 1048576 -> {
                df.format(fileS.toDouble() / 1024) + "K"
            }
            fileS < 1073741824 -> {
                df.format(fileS.toDouble() / 1048576) + "M"
            }
            else -> {
                df.format(fileS.toDouble() / 1073741824) + "G"
            }
        }
        return fileSizeString
    }

    //将Bitmap缓存到本地
    @JvmStatic
    fun saveBitmap(bitmap: Bitmap?) {
        val screenImagePath: String
        //输出
        try {
            val rootDir =
                Constants.BASE_PATH.absolutePath + "/" + Constants.APPLICATION_NAME + "/截屏"
            val downloadFile = File(rootDir)
            if (!downloadFile.mkdirs()) {
                //需要权限
                downloadFile.createNewFile()
            }
            screenImagePath = "$rootDir/screen_capture" + SimpleDateFormat(
                "yyyy_MM_dd_hh_mm_ss",
                Locale.getDefault()
            ).format(Date()) + ".png"
            val fileOutputStream = FileOutputStream(screenImagePath)
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (ignored: java.lang.Exception) {
        } finally {
            bitmap!!.recycle()
        }
    }

    //判断sd卡是否存在
    @JvmStatic
    fun hasSDCard(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    //获取sdcard根目录
    @JvmStatic
    fun getSdCardPath(): String? {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    //获取当前app的应用程序名称
    @JvmStatic
    fun getApplicationName(context: Context): String? {
        var packageManager: PackageManager? = null
        var applicationInfo: ApplicationInfo?
        try {
            packageManager = context.applicationContext.packageManager
            applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            applicationInfo = null
        }
        return packageManager!!.getApplicationLabel(applicationInfo) as String
    }

    //获取当前app的应用程序名称
    @JvmStatic
    fun getApplicationId(context: Context?): String? {
        return context?.packageName
    }

    //是否安装了XXX
    @JvmStatic
    fun isAvailable(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val packageInfos = packageManager.getInstalledPackages(0)
        if (packageInfos != null) {
            for (i in packageInfos.indices) {
                val pn = packageInfos[i].packageName
                if (pn == packageName) {
                    return true
                }
            }
        }
        return false
    }

    //获取app的图标
    @JvmStatic
    fun getApplicationIcon(context: Context): Bitmap? {
        try {
            val drawable = context.packageManager.getApplicationIcon(Constants.APPLICATION_ID)
            val bitmap = SoftReference(
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
                )
            )
            val canvas = Canvas(bitmap.get())
            //canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap.get()
        } catch (ignored: java.lang.Exception) {
        }
        return null
    }

    //获取安装跳转的行为
    @JvmStatic
    private fun getSetupApk(activity: Activity, apkFilePath: String): Intent? {
        val mActivity = WeakReference(activity)
        val intent = Intent(Intent.ACTION_VIEW)
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val file = File(apkFilePath)
            val contentUri = FileProvider.getUriForFile(
                mActivity.get()!!,
                Constants.APPLICATION_ID + ".fileProvider",
                file
            )
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.parse("file://$apkFilePath"),
                "application/vnd.android.package-archive"
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

}