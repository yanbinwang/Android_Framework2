package com.example.common.utils.file

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.base.utils.DateUtil
import com.example.base.utils.LogUtil
import com.example.base.utils.ToastUtil
import com.example.common.constant.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.lang.ref.SoftReference
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/7/1.
 * 文件管理工具类
 */
object FileUtil : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = (Main)
    private const val TAG = "FileUtil"

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
        var file: File
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
    fun deleteFile(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) return
        val file = File(filePath)
        if (file.isFile && file.exists()) file.delete()
    }

    /**
     * 删除本地路径下的所有文件
     */
    @JvmStatic
    fun deleteDir(filePath: String) = deleteDirWithFile(File(filePath))

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
        LogUtil.e(TAG, " \n压缩路径:$folderPath\n压缩文件名:$fileName")
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
     * 将bitmap存成文件至指定目录下-读写权限
     * BitmapFactory.decodeResource(resources, R.mipmap.img_qr_code)
     */
    @JvmOverloads
    @JvmStatic
    fun saveBitmap(context: Context, bitmap: Bitmap, root: String = "${Constants.APPLICATION_FILE_PATH}/图片", formatJpg: Boolean = true, quality: Int = 100): Boolean {
        try {
            val storeDir = File(root)
            if (!storeDir.mkdirs()) storeDir.createNewFile()//需要权限
            val file = File(storeDir, DateUtil.getDateTime(DateUtil.EN_YMDHMS, Date()) + if (formatJpg) ".jpg" else ".png")
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
     * 压缩文件协程
     */
    @JvmStatic
    fun zipFolderJob(folderPath: String, zipFilePath: String, onStart: () -> Unit? = {}, onStop: () -> Unit? = {}): Job {
        return launch {
            onStart
            val fileDir = File(folderPath)
            val zipFile = File(zipFilePath)
            try {
                withContext(IO) { if (fileDir.exists()) zipFolder(fileDir.absolutePath, zipFile.absolutePath) }
            } catch (e: Exception) {
                LogUtil.e(TAG, "打包图片生成压缩文件异常: $e")
            } finally {
                onStop
            }
        }
    }

    /**
     * 存储图片协程
     */
    @JvmStatic
    fun saveBitmapJob(context: Context, bitmap: Bitmap, onStart: () -> Unit? = {}, onStop: () -> Unit? = {}): Job {
        return launch {
            onStart
            var type: Boolean
            withContext(IO) { type = saveBitmap(context, bitmap) }
            ToastUtil.mackToastSHORT(if (type) "保存成功" else "保存失败", context)
            onStop
        }
    }

//    /**
//     * 视频图片打包协程
//     * 传入视频原路径，并通过秒数集合，批量生成图片，并打包成压缩包保存到指定路径下
//     */
//    @JvmStatic
//    fun handleVideoJob(
//        videoPath: String,
//        secondList: MutableList<Int>,
//        zipFilePath: String, ,
//        onStart: () -> Unit? = {},
//        onStop: () -> Unit? = {}
//    ): Job {
//        return launch {
//            onStart
//            withContext(IO) {
//                //在‘视频抽帧’文件夹下建立一个以抽帧文件名命名的文件夹，方便后续对当前文件夹打压缩包
//                val savePath = Constants.APPLICATION_FILE_PATH + "/文件/视频抽帧/${File(videoPath).name}"
//                val thumbPaths = ArrayList<String>()
//                for (i in secondList) {
////                    val thumbPath = VideoHelper.getFrames(videoPath, savePath, i)
////                    thumbPaths.add(thumbPath)
//                }
//                try {
//                    zipFolder(savePath, zipFilePath)
//                } catch (ignored: Exception) {
//                } finally {
//                    //清空当前文件夹和其下的所有图片
//                    deleteDir(savePath)
//                    withContext(Main) { onStop }
//                }
//            }
//        }
//    }

    /**
     * base64文件流的形式加载文件，需要先下载，之后在放置
     */
    fun handleBase64Job(base64: String, suffix: String, root: String = "${Constants.APPLICATION_FILE_PATH}/缓存", clear: Boolean = true, onStart: () -> Unit? = {}, onStop: (path: String) -> Unit? = {}): Job {
        return launch {
            onStart
            if (clear) deleteDir(root)
            val storeDir = File(root)
            if (!storeDir.mkdirs()) storeDir.createNewFile()
            val file = File(storeDir, "${System.currentTimeMillis()}_cache${suffix}")
            val pdfAsBytes = Base64.decode(base64, 0)
            val fileOutputStream: FileOutputStream?
            try {
                withContext(IO) {
                    fileOutputStream = FileOutputStream(file, false)
                    fileOutputStream.write(pdfAsBytes)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }
            } catch (e: Exception) {
            } finally {
                onStop(file.absolutePath)
            }
        }
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
    fun formatFileSize(fileSize: Long): String {
        val format = DecimalFormat("#.00")
        return when {
            fileSize < 1024 -> format.format(fileSize.toDouble()) + "B"
            fileSize < 1048576 -> format.format(fileSize.toDouble() / 1024) + "K"
            fileSize < 1073741824 -> format.format(fileSize.toDouble() / 1048576) + "M"
            else -> format.format(fileSize.toDouble() / 1073741824) + "G"
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
            val fileReader = FileReader("/proc/cpuinfo")
            val bufferedReader = BufferedReader(fileReader)
            val text = bufferedReader.readLine()
            val array = text.split(":\\s+".toRegex(), 2).toTypedArray()
            return array[1]
        } catch (ignored: Exception) {
        }
        return "暂无"
    }

}