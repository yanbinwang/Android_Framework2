package com.example.common.utils.function

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logE
import java.io.File
import java.io.RandomAccessFile
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.math.BigInteger
import java.security.MessageDigest

/**
 * 各个单位换算
 */
val Number.mb get() = this.toSafeLong() * 1024L * 1024L
val Number.gb get() = this.toSafeLong() * 1024L * 1024L * 1024L
val Number.tb get() = this.toSafeLong() * 1024L * 1024L * 1024L * 1024L

/**
 * 发送广播通知更新数据库
 */
fun Context.insertImageResolver(file: File?) {
    file ?: return
    MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null)
    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, "file://${file.path}".toUri()))
}

/**
 * 获取app的图标
 */
fun Context.getApplicationIcon(): Bitmap? {
    try {
        packageManager.getApplicationIcon(Constants.APPLICATION_ID).apply {
            val bitmap = createBitmap(intrinsicWidth, intrinsicHeight, if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            draw(canvas)
            return bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
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
 * 判断是否存在
 */
fun String?.isExists(): Boolean {
    this ?: return false
    return File(this).exists()
}

/**
 * 删除文件
 */
fun String?.deleteFile() {
    this ?: return
    File(this).deleteFile()
}

fun File?.deleteFile() {
    this ?: return
    if (isFile && exists()) delete()
}

/**
 * 删除路径下的所有文件
 */
fun String?.deleteDir() {
    this ?: return
    File(this).deleteDir()
}

fun File?.deleteDir() {
    this ?: return
    deleteDirWithFile(this)
}

private fun deleteDirWithFile(dir: File?) {
    if (dir == null || !dir.exists() || !dir.isDirectory) return
    for (file in dir.listFiles().orEmpty()) {
        if (file.isFile) file.delete() //删除所有文件
        else if (file.isDirectory) deleteDirWithFile(file) //递规的方式删除文件夹
    }
    dir.delete() //删除目录本身
}

/**
 * 文件本身的整体大小
 */
fun String?.getTotalSize(): Long {
    this ?: return 0
    return File(this).getTotalSize()
}

fun File?.getTotalSize(): Long {
    this ?: return 0
    return totalSizeWithFile(this)
}

fun totalSizeWithFile(file: File): Long {
    var size = 0L
    for (mFile in file.listFiles().orEmpty()) {
        size = if (mFile.isDirectory) {
            size + totalSizeWithFile(mFile)
        } else {
            size + mFile.length()
        }
    }
    return size
}

/**
 * 获取对应大小的文字
 * 新api --> Formatter.formatFileSize()
 */
fun File?.getSizeFormat(): String {
    this ?: return ""
    return length().getSizeFormat()
}

fun Number?.getSizeFormat(): String {
    this ?: return ""
    val byteResult = this.toSafeLong() / 1024
    if (byteResult < 1) return "<1K"
    val kiloByteResult = byteResult / 1024
    if (kiloByteResult < 1) return "${BigDecimal(byteResult.toString()).setScale(2, ROUND_HALF_UP).toPlainString()}K"
    val mByteResult = kiloByteResult / 1024
    if (mByteResult < 1) return "${BigDecimal(kiloByteResult.toString()).setScale(2, ROUND_HALF_UP).toPlainString()}M"
    val gigaByteResult = mByteResult / 1024
    if (gigaByteResult < 1) return "${BigDecimal(mByteResult.toString()).setScale(2, ROUND_HALF_UP).toPlainString()}GB"
    val teraByteResult = BigDecimal(gigaByteResult)
    return "${teraByteResult.setScale(2, ROUND_HALF_UP).toPlainString()}TB"
}

/**
 * 文件长度
 */
fun String?.getLength(): Long {
    return this?.let { File(it).length() } ?: 0L
}

/**
 * 文件分割
 * cutSize->分割文件的大小
 */
internal fun File?.split(chunkSize: Long): MutableList<String> {
    this ?: return arrayListOf()
    //分割文件总大小
    val targetLength = length()
    //总分片数
    val numChunks = if (targetLength.mod(chunkSize) == 0L) {
        targetLength.div(chunkSize)
    } else {
        targetLength.div(chunkSize).plus(1)
    }.toSafeInt()
    //获取目标文件,预分配文件所占的空间,在磁盘中创建一个指定大小的文件(r:只读)
    val splitList = ArrayList<String>()
    RandomAccessFile(this, "r").use { accessFile ->
        //文件的总大小
        val length = accessFile.length()
        //文件切片后每片的最大大小
        val maxSize = length / numChunks
        //初始化偏移量
        var offSet = 0L
        //开始切片
        for (i in 0 until numChunks - 1) {
            val begin = offSet
            val end = (i + 1) * maxSize
            val tmpInfo = write(absolutePath, i, begin, end)
            offSet = tmpInfo.second.orZero
            splitList.add(tmpInfo.first.orEmpty())
        }
        //剩余部分（若存在）
        if (length - offSet > 0) {
            splitList.add(write(absolutePath, numChunks - 1, offSet, length).first.orEmpty())
        }
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
 * @return first->分割文件地址 second->分割文件大小
 */
private fun write(filePath: String, index: Int, begin: Long, end: Long): Pair<String?, Long?> {
    //源文件
    val file = File(filePath)
//    //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
//    val tmpFile = File("${file.parent}/${file.name.split(".")[0]}_${index}.tmp")
    val fileName = file.name.split(".")[0]
    //本地文件存储路径，例如/storage/emulated/0/oss/文件名_record
    val recordDirectory = "${file.parent}/${fileName}_record"
    //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件->多一个文件夹，好管理对应文件的tmp
    val tmpFile = File("${recordDirectory}/${fileName}_${index}.tmp")
    //如果不存在，则创建一个或继续写入
    return RandomAccessFile(tmpFile, "rw").use { outAccessFile ->
        RandomAccessFile(file, "r").use { inAccessFile ->
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
            tmpFile.absolutePath to inAccessFile.filePointer
        }
    }
}

/**
 * 读取文件到文本（文本，找不到文件或读取错返回null）
 * kt中对File类做了readText扩展，但是实现相当于将每行文本塞入list集合，再从集合中读取
 * 此项操作比较吃内存，官方注释也不推荐读取2G以上的文件，所以使用java的方法
 */
internal fun File?.read(): String {
    this ?: return ""
    return if (exists()) {
        bufferedReader().use { reader ->
            val stringBuilder = StringBuilder()
            var str: String?
            while (reader.readLine().also { str = it } != null) stringBuilder.append(str)
            stringBuilder.toString()
        }
    } else {
        ""
    }
}

/**
 * 将当前文件拷贝一份到目标路径
 */
internal fun File?.copy(destFile: File?) {
    if (null == this || !exists()) return
    if (!destFile?.exists().orFalse) destFile?.createNewFile()
    inputStream().channel.use { source ->
        destFile?.outputStream()?.channel.use { destination ->
            destination?.transferFrom(source, 0, source.size())
        }
    }
}

/**
 * 获取文件采用base64形式
 */
internal fun File?.getBase64(): String {
    this ?: return ""
    return inputStream().use { input ->
        val bytes = ByteArray(input.available())
        val length = input.read(bytes)
        Base64.encodeToString(bytes, 0, length, Base64.DEFAULT)
    }
}

/**
 * 获取文件hash值
 * 满足64位哈希，不足则前位补0
 */
//internal fun File?.getHash(): String {
//    this ?: return ""
//    return inputStream().use { input ->
//        val digest = MessageDigest.getInstance("SHA-256")
//        val array = ByteArray(1024)
//        var len: Int
//        while (input.read(array, 0, 1024).also { len = it } != -1) {
//            digest.update(array, 0, len)
//        }
//        //检测是否需要补0
//        val bigInt = BigInteger(1, digest.digest())
//        var hash = bigInt.toString(16)
//        if (hash.length < 64) {
//            for (i in 0 until 64 - hash.length) {
//                hash = "0$hash"
//            }
//        }
//        hash
//    }
//}
internal fun File?.getHash(): String {
    return this?.inputStream()?.use { input ->
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        digest.digest().toHexString()
    } ?: ""
}

private fun ByteArray.toHexString(): String {
    return BigInteger(1, this).toString(16).padStart(64, '0')
}

/**
 * 获取media文件的时长（
 * 返回时长(音频，视频)->不支持在线音视频
 * 放在io子线程中读取，超时会导致卡顿或闪退
 */
internal fun File?.getDuration(): Int {
    this ?: return 0
    val player = MediaPlayer()
    return try {
        player.setDataSource(absolutePath)
        player.prepare()
        //视频时长（毫秒）/1000=x秒
        val duration = player.duration.orZero
        duration.divide(1000, ROUND_HALF_UP).toSafeInt().apply { "文件时长：${this}秒".logE() }
//        Math.round(duration / 1000.0).toSafeInt().apply { "文件时长：${this}秒".logE() }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    } finally {
        try {
            player.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
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
    val path = realPath.ifEmpty {
        when {
            uri.path?.contains("/document/raw:").orFalse -> uri.path?.replace("/document/raw:", "")
            uri.path?.contains("/document/primary:").orFalse -> uri.path?.replace("/document/primary:", "/storage/emulated/0/")
            else -> return null
        }
    } ?: return null
    return File(path)
}