package com.example.common.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import androidx.core.net.toUri
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logE
import java.io.*
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
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

    /**
     * 获取文件base64位地址
     */
    @JvmStatic
    fun fileToBase64(file: File): String {
        var base64: String? = null
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)
            val bytes = ByteArray(inputStream.available())
            val length = inputStream.read(bytes)
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return base64.orEmpty()
    }

    /**
     * 开始创建并写入tmp文件
     * @param filePath  分割文件地址
     * @param fileSize 分割文件大小
     */
    class TmpInfo(var filePath: String? = null, var fileSize: Long = 0)

    /**
     * 文件分割
     *
     * @param targetFile 分割的文件
     * @param cutSize    分割文件的大小
     */
    @JvmStatic
    fun split(targetFile: File, cutSize: Long): MutableList<String> {
        val splitList = ArrayList<String>()
        try {
            //计算需要分割的文件总数
            val targetLength = targetFile.length()
            val count = if (targetLength.mod(cutSize) == 0L) targetLength.div(cutSize).toInt() else targetLength.div(cutSize).plus(1).toInt()
            //获取目标文件,预分配文件所占的空间,在磁盘中创建一个指定大小的文件(r:只读)
            val accessFile = RandomAccessFile(targetFile, "r")
            //文件的总大小
            val length = accessFile.length()
            //文件切片后每片的最大大小
            val maxSize = length / count
            //初始化偏移量
            var offSet = 0L
            //开始切片
            for (i in 0 until count - 1) {
                val begin = offSet
                val end = (i + 1) * maxSize
                val tmpInfo = getWrite(targetFile.absolutePath, i, begin, end)
                offSet = tmpInfo.fileSize
                splitList.add(tmpInfo.filePath ?: "")
            }
            if (length - offSet > 0) splitList.add(getWrite(targetFile.absolutePath, count - 1, offSet, length).filePath ?: "")
            accessFile.close()
        } catch (_: Exception) {
        } finally {
            //确保返回的集合中不包含空路径
            for (i in splitList.indices.reversed()) {
                if (splitList.safeGet(i).isNullOrEmpty()) {
                    splitList.removeAt(i)
                }
            }
        }
        return splitList
    }

    /**
     * 开始创建并写入tmp文件
     * @param filePath  源文件地址
     * @param index 源文件的顺序标识
     * @param begin 开始指针的位置
     * @param end   结束指针的位置
     */
    @JvmStatic
    private fun getWrite(filePath: String, index: Int, begin: Long, end: Long): TmpInfo {
        val info = TmpInfo()
        try {
            //源文件
            val file = File(filePath)
            //申明文件切割后的文件磁盘
            val inAccessFile = RandomAccessFile(file, "r")
            //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            val tmpFile = File("${file.parent}/${file.name.split(".")[0]}_${index}.tmp")
            //如果不存在，则创建一个或继续写入
            val outAccessFile = RandomAccessFile(tmpFile, "rw")
            //申明具体每一文件的字节数组
            val b = ByteArray(1024)
            var n: Int
            //从指定位置读取文件字节流
            inAccessFile.seek(begin)
            //判断文件流读取的边界，从指定每一份文件的范围，写入不同的文件
            while (inAccessFile.read(b).also { n = it } != -1 && inAccessFile.filePointer <= end) {
                outAccessFile.write(b, 0, n)
            }
            //关闭输入输出流,赋值
            info.fileSize = inAccessFile.filePointer
            inAccessFile.close()
            outAccessFile.close()
            info.filePath = tmpFile.absolutePath
        } catch (_: Exception) {
        }
        return info
    }

    @JvmStatic
    fun getSignature(southPath: String) = getSignature(File(southPath))

    /**
     * 获取文件哈希值
     * 满足64位哈希，不足则前位补0
     */
    @JvmStatic
    fun getSignature(file: File): String {
        var hash = ""
        try {
            val inputStream = FileInputStream(file)
            val digest = MessageDigest.getInstance("SHA-256")
            val array = ByteArray(1024)
            var len: Int
            while (inputStream.read(array, 0, 1024).also { len = it } != -1) {
                digest.update(array, 0, len)
            }
            inputStream.close()
            val bigInt = BigInteger(1, digest.digest())
            hash = bigInt.toString(16)
            if (hash.length < 64) {
                for (i in 0 until 64 - hash.length) {
                    hash = "0$hash"
                }
            }
        } catch (_: Exception) {
        }
        return hash
    }

}

/**
 * 是否安装了XXX应用
 *  <uses-permission
 *  android:name="android.permission.QUERY_ALL_PACKAGES"
 *  tools:ignore="QueryAllPackagesPermission" />
 *  <uses-permission
 *  android:name="android.permission.WRITE_EXTERNAL_STORAGE"
 *  tools:ignore="ScopedStorage" />
 *  <uses-permission
 *  android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
 *  tools:ignore="ScopedStorage" />
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
 * 判断目录是否存在,不存在则创建
 * 返回对应路径
 */
fun String?.isMkdirs(): String {
    this ?: return ""
    val file = File(this)
    if (!file.mkdirs()) file.createNewFile()
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
 * kt中对File类做了readText扩展，但是实现相当于将每行文本塞入list集合，再从集合中读取
 * 此项操作比较吃内存，官方注释也不推荐读取2G以上的文件，所以使用java的方法
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
 * 获取文件采用base64形式
 */
fun File?.fileToBase64(): String {
    this ?: return ""
    return FileUtil.fileToBase64(this)
}

fun String?.fileToBase64(): String {
    this ?: return ""
    return File(this).fileToBase64()
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

/**
 * 通过uri获取到一个文件
 */
fun Uri?.getFileFromUri(): File? {
    this ?: return null
    return this.toString().getFileFromUri()
}

fun String?.getFileFromUri(): File? {
    this ?: return null
    val uri = toUri()
    if (uri.path == null) return null
    if (uri.scheme == "file") return File(this)
    if (uri.scheme.isNullOrEmpty()) return File(this)

    var realPath = String()
    val databaseUri: Uri
    val selection: String?
    val selectionArgs: Array<String>?
    if (uri.path?.contains("/document/image:").orFalse) {
        databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        selection = "_id=?"
        selectionArgs = arrayOf(DocumentsContract.getDocumentId(uri).split(":")[1])
    } else {
        databaseUri = uri
        selection = null
        selectionArgs = null
    }
    try {
        val column = "_data"
        val projection = arrayOf(column)
        val cursor = BaseApplication.instance.contentResolver.query(databaseUri, projection, selection, selectionArgs, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                realPath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
    } catch (e: Exception) {
        e.logE
    }
    val path = if (realPath.isNotEmpty()) realPath else {
        when {
            uri.path?.contains("/document/raw:").orFalse -> uri.path?.replace("/document/raw:", "")
            uri.path?.contains("/document/primary:").orFalse -> uri.path?.replace("/document/primary:", "/storage/emulated/0/")
            else -> return null
        }
    } ?: return null
    return File(path)
}