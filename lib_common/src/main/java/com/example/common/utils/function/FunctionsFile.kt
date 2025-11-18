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
import java.math.RoundingMode
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
 * 校验文件是否无独占写锁定、可删除（间接判断）
 * @param this 文件路径
 * @return true：无写锁定，可尝试删除；false：有写锁定/占用
 */
fun String?.isFileWritableAndDeletable(): Boolean {
    this ?: return false
    // 文件是否存在
    if (!isPathExists()) return false
    // 文件是否可写（间接判断无独占写锁定）
    val file = File(this)
    if (!file.canWrite()) return false
    // 尝试创建临时文件（进一步确认目录无锁定）
    val parentDir = file.parentFile ?: return false
    val tempFile = File(parentDir, "temp_check_lock_${System.currentTimeMillis()}.tmp")
    return try {
        // 无论创建成功与否，最终都要删除临时文件（防残留）
        val createSuccess = tempFile.createNewFile()
        createSuccess
    } catch (e: SecurityException) {
        // 捕获“权限不足”异常（部分机型/目录可能限制创建临时文件）
        e.printStackTrace()
        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        // 确保临时文件被删除，不残留
        tempFile.safeDelete()
    }
}

/**
 * 判断字符串路径对应的文件/目录是否存在
 * @return true：路径非空且对应的文件/目录存在；false：路径为空或不存在
 */
fun String?.isPathExists(): Boolean {
    this ?: return false
    return File(this.trim()).exists()
}

/**
 * 确保目录存在（不存在则创建），返回目录绝对路径
 * mkdirs():创建目录（文件夹）
 * createNewFile():创建文件
 */
fun String?.ensureDirExists(): String {
    // 空路径直接返回空
    this ?: return ""
    // 去除首尾空格（避免路径含无效空格导致创建失败）
    val dirPath = trim()
    if (dirPath.isEmpty()) return ""
    // 取得文件类
    val dir = File(dirPath)
    return try {
        // 目录已存在 → 直接返回绝对路径
        if (dir.exists()) {
            // 路径存在但不是目录 → 返回空
            if (dir.isDirectory) {
                dir.absolutePath
            } else {
                ""
            }
        } else {
            // 目录不存在 → 创建多级目录（mkdirs() 支持多级）
            if (dir.mkdirs()) {
                dir.absolutePath
            } else {
                // 创建成功返回路径，失败返回空
                ""
            }
        }
    } catch (e: Exception) {
        // 捕获异常（权限不足、路径无效等）
        e.printStackTrace()
        ""
    }
}

/**
 * 删除文件
 */
fun String?.deleteFile() {
    this ?: return
    File(this).safeDelete()
}

/**
 * 删除目录下的所有文件,包含目录本身
 */
fun String?.deleteDir() {
    this ?: return
    File(this).deleteRecursively()
}

/**
 * 安全删除文件（处理文件占用等异常）
 */
fun File?.safeDelete(): Boolean {
    // 避免空路径文件
    this ?: return false
    if (this == File("")) return false
    return try {
        when {
            // 文件不存在，视为删除成功
            !exists() -> true
            /**
             * 兼容目录（防止误传目录）
             * Kotlin 标准库中 File 类的递归删除方法，核心作用是：删除文件或目录（包括目录下所有子文件、子目录），一步到位清理整个文件树
             */
            isDirectory -> deleteRecursively()
            // 余下的进入删除逻辑
            else -> {
                // 直接删除
                if (delete()) return true
                // 文件被占用，先重命名再删除
                val tempFile = File(parent, "${name}.tmp.${System.currentTimeMillis()}")
                if (renameTo(tempFile)) {
                    tempFile.delete()
                } else {
                    /**
                     * 最后尝试强制删除（部分场景有效）
                     * 当调用 file.deleteOnExit() 时，JVM 会将该文件的路径添加到一个内部注册表中（本质是一个线程安全的集合）
                     * 当 JVM 正常终止（比如 App 正常退出、进程被系统正常回收）时，会遍历这个注册表，尝试删除所有注册的文件
                     * 删除顺序与注册顺序相反（先注册的后删除）
                     * 仅对「文件」有效，对「目录」无效（目录需手动删除或用 deleteRecursively()）
                     */
                    deleteOnExit()
                    false
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 文件本身的整体大小
 */
fun String?.totalSize(): Long {
    this ?: return 0
    return File(this).totalSize()
}

fun File?.totalSize(): Long {
    this ?: return 0
    // 文件/目录不存在直接返回 0，避免无效遍历
    if (!exists()) return 0
    var size = 0L
    // 遍历子文件（orEmpty() 处理 listFiles() 返回 null 的情况）
    for (mFile in listFiles().orEmpty()) {
        size += if (mFile.isDirectory) {
            // 递归调用时要传 mFile
            mFile.totalSize()
        } else {
            mFile.length()
        }
    }
    // 如果是单个文件，直接返回自身大小
    return if (isFile) length() else size
}

/**
 * 获取对应大小的文字
 * 新api --> Formatter.formatFileSize()
 */
private const val STORAGE_UNIT_BASE = 1024.0 // 用 Double 避免类型转换

fun String?.storageSizeFormat(): String {
    this ?: return ""
    return File(this).storageSizeFormat()
}

fun File?.storageSizeFormat(): String {
    this ?: return ""
    // 字节数
    val bytes = length().toSafeLong()
    // 用 Double 简化计算，避免重复整除丢失精度
    val kb = bytes / STORAGE_UNIT_BASE
    return when {
        kb < 1 -> "<1K"
        kb < STORAGE_UNIT_BASE -> "${formatStorageValue(kb)}K"
        kb < STORAGE_UNIT_BASE * STORAGE_UNIT_BASE -> "${formatStorageValue(kb / STORAGE_UNIT_BASE)}M"
        kb < STORAGE_UNIT_BASE * STORAGE_UNIT_BASE * STORAGE_UNIT_BASE -> "${formatStorageValue(kb / (STORAGE_UNIT_BASE * STORAGE_UNIT_BASE))}GB"
        else -> "${formatStorageValue(kb / (STORAGE_UNIT_BASE * STORAGE_UNIT_BASE * STORAGE_UNIT_BASE))}TB"
    }
}

/**
 * 统一格式化存储大小数值（保留2位小数，四舍五入）
 */
private fun formatStorageValue(value: Double): String {
    return BigDecimal.valueOf(value)
        // 显式指定 RoundingMode，避免歧义
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
}

/**
 * 重命名文件(只能改文件名，路径固定（原文件父目录）)
 * @param this 原始文件
 * @param newFileName 新的文件名（仅文件名，不包含路径）
 * @return 是否重命名成功
 */
fun File?.renameFile(newFileName: String): Boolean {
    this ?: return false
    // 仅对文件生效，避免目录误操作
    if (!exists() || !isFile) return false
    val parentDir = parentFile ?: return false
    // 确保父目录存在（极端情况父目录被删除，避免重命名失败）
    if (!parentDir.exists() && !parentDir.mkdirs()) {
        return false
    }
    // 创建目标文件（新路径 + 新文件名）
    val targetFile = File(parentDir, newFileName)
    // 避免覆盖已存在的文件
    if (targetFile.exists()) {
        return false
    }
    // 执行重命名操作
    return renameTo(targetFile)
}

/**
 * 重命名文件（可改路径 + 文件名，更灵活）
 * @param this 原始文件
 * @param targetFile 目标文件（包含新路径和新文件名）
 * @return 是否重命名成功
 * 若「原文件和目标文件在同一个分区」（如都在 /data/user/0/ 下）：仅修改文件的路径和名称，文件数据本身不移动（速度极快）；
 * 若「原文件和目标文件在不同分区」（如原文件在内部存储，目标在 SD 卡）：会先复制文件数据到新路径，再删除原文件（速度取决于文件大小）；
 * 无论哪种情况，最终只有一个文件存在（原文件会消失）
 */
fun File?.renameFileTo(targetFile: File): Boolean {
    this ?: return false
    if (!exists()) {
        return false
    }
    // 确保目标文件的父目录存在
    val targetParent = targetFile.parentFile
    if (targetParent != null && !targetParent.exists()) {
        // 创建父目录（包括所有必要的父目录）
        if (!targetParent.mkdirs()) {
            return false
        }
    }
    // 避免覆盖已存在的文件
    if (targetFile.exists()) {
        return false
    }
    return renameTo(targetFile)
}

/**
 * 扩展函数：获取字符串路径对应的文件/目录长度
 * - 若为文件：返回文件大小（字节）
 * - 若为目录：返回 0L（目录本身无大小，需用 getTotalSize() 统计子文件总大小）
 * - 路径为空/文件不存在/异常：返回 0L
 */
fun String?.getFileLength(): Long {
    this ?: return 0L
    return try {
        val file = File(this.trim())
        if (file.exists()) file.length() else 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * 获取当前手机缓存目录下的缓存文件大小,
 * @return 返回格式化后的缓存大小字符串，如 "2.5M"
 */
fun Context?.getFormattedCacheSize(): String {
    var formattedSize = "0M"
    this ?: return formattedSize
    // 安全获取缓存目录，计算总大小并格式化
    cacheDir?.takeIf { it.exists() }?.apply {
        val totalCacheBytes = totalSize()
        formattedSize = if (totalCacheBytes > 0) {
            storageSizeFormat()
        } else {
            formattedSize
        }
    }
    return formattedSize
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

/**
 * 文件分割
 * cutSize->分割文件的大小
 */
internal fun File?.split(chunkSize: Long): MutableList<String> {
    this ?: return arrayListOf()
    // 分割文件总大小
    val targetLength = length()
    // 总分片数
    val numChunks = if (targetLength.mod(chunkSize) == 0L) {
        targetLength.div(chunkSize)
    } else {
        targetLength.div(chunkSize).plus(1)
    }.toSafeInt()
    // 获取目标文件,预分配文件所占的空间,在磁盘中创建一个指定大小的文件(r:只读)
    val splitList = ArrayList<String>()
    RandomAccessFile(this, "r").use { accessFile ->
        // 文件的总大小
        val length = accessFile.length()
        // 文件切片后每片的最大大小
        val maxSize = length / numChunks
        // 初始化偏移量
        var offSet = 0L
        // 开始切片
        for (i in 0 until numChunks - 1) {
            val begin = offSet
            val end = (i + 1) * maxSize
            val (mFilePath, mOffSet) = write(absolutePath, i, begin, end)
            offSet = mOffSet.orZero
            splitList.add(mFilePath.orEmpty())
        }
        // 剩余部分（若存在）
        if (length - offSet > 0) {
            splitList.add(write(absolutePath, numChunks - 1, offSet, length).first.orEmpty())
        }
        // 确保返回的集合中不包含空路径
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
    // 源文件
    val file = File(filePath)
//    //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
//    val tmpFile = File("${file.parent}/${file.name.split(".")[0]}_${index}.tmp")
    val fileName = file.name.split(".")[0]
    // 本地文件存储路径，例如/storage/emulated/0/oss/文件名_record
    val recordDirectory = "${file.parent}/${fileName}_record"
    // 定义一个可读，可写的文件并且后缀名为.tmp的二进制文件->多一个文件夹，好管理对应文件的tmp
    val tmpFile = File("${recordDirectory}/${fileName}_${index}.tmp")
    // 如果不存在，则创建一个或继续写入
    return RandomAccessFile(tmpFile, "rw").use { outAccessFile ->
        RandomAccessFile(file, "r").use { inAccessFile ->
            // 申明具体每一文件的字节数组
            val b = ByteArray(1024)
            var n: Int
            // 从指定位置读取文件字节流
            inAccessFile.seek(begin)
            // 判断文件流读取的边界，从指定每一份文件的范围，写入不同的文件
            while (inAccessFile.read(b).also { n = it } != -1 && inAccessFile.filePointer <= end) {
                outAccessFile.write(b, 0, n)
            }
            // 关闭输入输出流,赋值
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