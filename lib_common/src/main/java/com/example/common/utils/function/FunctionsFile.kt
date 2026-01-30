package com.example.common.utils.function

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
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
import com.example.framework.utils.logWTF
import java.io.File
import java.io.FileOutputStream
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
 * MediaStore.Images.Media.insertImage 在Android 10+已废弃，且返回值不可靠
 * ACTION_MEDIA_SCANNER_SCAN_FILE 广播在Android 10+对外部存储部分路径失效
 */
fun Context.insertImageResolver(file: File?) {
    file ?: return
    if (!file.exists() || !file.canRead()) {
        "文件不存在或不可读：${file.absolutePath}".logWTF
        return
    }
    // 适配 Android 10+（Scoped Storage）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        insertImageToMediaStoreQPlus(file)
    } else {
        // 低版本保留原有逻辑
        MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null)
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, "file://${file.path}".toUri()))
    }
}

/**
 * Android 10+ 插入图片到媒体库（触发扫描）
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.insertImageToMediaStoreQPlus(file: File) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + packageName)
        put(MediaStore.Images.Media.IS_PENDING, 1) // 标记为待处理，避免扫描中断
    }
    // 插入到媒体库
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri ?: return
    // 写入文件内容到媒体库 Uri
    try {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            file.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        // 取消待处理标记，触发媒体库扫描
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(uri, contentValues, null, null)
    } catch (e: Exception) {
        e.printStackTrace()
        // 插入失败，删除临时记录
        contentResolver.delete(uri, null, null)
    }
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
 * 判断字符串路径对应的文件/目录是否存在
 * @return true：路径非空且对应的文件/目录存在；false：路径为空或不存在
 */
fun String?.isPathExists(): Boolean {
    this ?: return false
    val trimmedPath = trim()
    if (trimmedPath.isEmpty()) return false
    return File(trimmedPath).exists()
}

/**
 * 获取字符串路径对应的文件/目录长度
 * 1) 若为文件：返回文件大小（字节）
 * 2) 若为目录：返回 0L（目录本身无大小，需用 getTotalSize() 统计子文件总大小）
 * 3) 路径为空/文件不存在/异常：返回 0L
 */
fun String?.getFileLength(): Long {
    this ?: return 0L
    return try {
        if (!isPathExists()) return 0L
        val file = File(this)
        if (file.exists() && file.canRead()) file.length() else 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * 获取不包含后缀名的文件名
 */
fun String?.suffixName(): String {
    this ?: return ""
    if (!isPathExists()) return ""
    return File(this).suffixName()
}

fun File?.suffixName(): String {
    this ?: return ""
    if (this == File("")) return ""
    if (!exists() || !canRead()) return ""
    return name.getFileNameWithoutSuffix()
}

/**
 * 从文件名中剥离最后一个后缀
 * // 假设文件路径是：/sdcard/wallets/my-wallet.json
 * val file = File("/sdcard/wallets/my-wallet.json")
 * println(file.name)        // 输出：my-wallet.json（带.json后缀）
 * println(file.path)        // 输出：/sdcard/wallets/my-wallet.json（完整路径）
 * println(file.parent)      // 输出：/sdcard/wallets（父目录）
 */
private fun String?.getFileNameWithoutSuffix(): String {
    this ?: return ""
    // 找到最后一个 "." 的位置
    val lastDotIndex = lastIndexOf('.')
    // lastDotIndex > 0 → 避免 "." 是第一个字符（如 .hidden.json）
    // lastDotIndex < length - 1 → 避免后缀是空（如 "my-wallet."）
    return if (lastDotIndex > 0 && lastDotIndex < this.length - 1) {
        this.substring(0, lastDotIndex)
    } else {
        // 无有效后缀，直接返回原字符串
        this
    }
}

/**
 * 文件本身的整体大小
 */
fun String?.totalSize(): Long {
    this ?: return 0L
    if (!isPathExists()) return 0L
    return File(this).totalSize()
}

fun File?.totalSize(): Long {
    this ?: return 0L
    if (this == File("")) return 0L
    // 文件/目录不存在直接返回 0，避免无效遍历
    if (!exists() || !canRead()) return 0L
    // 如果是文件，直接返回大小（无需遍历）
    if (isFile) return length()
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
    // 返回自身大小
    return size
}

/**
 * 获取对应大小的文字
 * 新api --> Formatter.formatFileSize()
 */
private const val STORAGE_UNIT_BASE = 1024.0

fun String?.storageSizeFormat(): String {
    this ?: return ""
    return File(this).storageSizeFormat()
}

fun File?.storageSizeFormat(): String {
    this ?: return ""
    return length().storageSizeFormat()
}

fun Number?.storageSizeFormat(): String {
    this ?: return ""
    // 字节数
    val bytes = toSafeLong()
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
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
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
            if (dir.isDirectory) {
                dir.absolutePath
            } else {
                // 路径存在但不是目录 → 返回空
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
        // 捕获未知异常
        e.printStackTrace()
        false
    } finally {
        // 确保临时文件被删除，不残留
        tempFile.safeDelete()
    }
}

/**
 * 删除文件
 */
fun String?.deleteFile(): Boolean {
    this ?: return false
    if (!isPathExists()) return false
    return File(this).safeDelete()
}

/**
 * 删除目录下的所有文件,包含目录本身
 */
fun String?.deleteDirectory(): Boolean {
    this ?: return false
    if (!isPathExists()) return false
    return File(this).let {
        if (it.isDirectory) {
            it.deleteRecursively()
        } else {
            it.safeDelete()
        }
    }
}

/**
 * 安全删除文件（处理文件占用等异常）
 */
fun File?.safeDelete(): Boolean {
    // 避免空路径文件
    this ?: return false
    if (this == File("")) return false
    return try {
        /**
         * deleteRecursively() 核心能力（Kotlin 标准库）：
         * 1) 自底向上遍历文件树（先删子文件，再删父目录）；
         * 2) 兼容文件/目录：文件直接删，目录递归删；
         * 3) 兜底判断：删除失败时检查文件是否不存在，不存在则视为成功；
         * 4) 整体结果：所有文件删成功返回 true，否则返回 false。
         */
        // 提前判断，文件不存在视为删除成功，减少遍历开销
        if (!exists()) return true
        // 删失败则走兜底
        val deleteSuccess = deleteRecursively()
        if (deleteSuccess) {
            true
        } else {
            /**
             * 极端场景兜底，尝试强制删除（文件被系统/其他App占用）
             * 当调用 file.deleteOnExit() 时，JVM 会将该文件的路径添加到一个内部注册表中（本质是一个线程安全的集合）
             * 当 JVM 正常终止（比如 App 正常退出、进程被系统正常回收）时，会遍历这个注册表，尝试删除所有注册的文件
             * 删除顺序与注册顺序相反（先注册的后删除）
             * 仅对「文件」有效，对「目录」无效（目录需手动删除或用 deleteRecursively()）
             */
            deleteOnExit()
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
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
 * 判断目录下是否存在文件（递归遍历所有子目录，找到第一个文件就返回true）
 * Android 6.0+ 需动态申请 READ_EXTERNAL_STORAGE/WRITE_EXTERNAL_STORAGE（针对外部存储）；
 * Android 10+ 需在 AndroidManifest.xml 中添加 android:requestLegacyExternalStorage="true"（兼容旧存储访问）；
 * Android 11+ 推荐使用 MediaStore 或 Scoped Storage，避免直接访问外部存储根目录。
 */
fun File.hasFiles(recursive: Boolean = true): Boolean {
    // 基础校验：不存在/非目录 → 无文件
    if (!exists() || !isDirectory) return false
    try {
        val dirQueue = ArrayDeque<File>()
        dirQueue.add(this)
        while (dirQueue.isNotEmpty()) {
            val currentDir = dirQueue.removeFirst()
            val files = currentDir.listFiles() ?: continue
            for (file in files) {
                // 跳过符号链接（避免循环/无效遍历）
                if (isSymbolicLinkCompat(file)) continue
                // 找到文件 → 直接返回true（无需继续遍历）
                if (file.isFile) return true
                // 需要递归 → 将子目录加入队列
                if (recursive && file.isDirectory) {
                    dirQueue.add(file)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // 权限不足/IO异常等 → 视为无文件
        return false
    }
    // 遍历完所有目录都没找到文件 → 返回false
    return false
}

/**
 * 纯File实现的符号链接判断（API 1+ 兼容）
 */
private fun isSymbolicLinkCompat(file: File): Boolean {
    if (!file.exists()) return false
    return try {
        file.absolutePath != file.canonicalPath
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 获取指定目录下的第一个文件路径（仅文件，排除文件夹）
 * @param this 目标目录
 * @return 第一个文件的绝对路径，无文件则返回 null
 */
fun File?.getFirstFileInDirectory(): String? {
    // 检查目录是否合法
    if (this == null || !this.exists() || !this.isDirectory()) {
        "目录不存在或不是文件夹".logWTF
        return null
    }
    // 获取目录下所有文件/文件夹（过滤隐藏文件）
    val files = this.listFiles { file ->
        // 仅保留「非隐藏」且「是文件」的项（排除文件夹）
        !file.isHidden() && file.isFile()
    }
    // 判断是否有文件，返回第一个文件的路径
    if (files != null && files.size > 0) {
        // 按修改时间排序
        // Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        // 返回第一个文件的绝对路径
        return files[0]?.absolutePath
    } else {
        "目录下无文件".logWTF
        return null
    }
}

/**
 * 获取文件的纯名称（去掉后缀，无后缀则返回完整文件名）
 * @return 纯文件名（小写可选，建议和后缀保持一致）
 */
fun File?.getRealFileName(): String {
    // 文件为空返回空
    this ?: return ""
    // 文件名本身为空返回空
    val fileName = name ?: return ""
    // 截取最后一个小数点前的内容（无小数点则返回完整文件名）
    return fileName.substringBeforeLast(".", fileName).trim()
}

/**
 * 解析文件的真实后缀（不含小数点，如 "jpg/png/tmp"）
 * @return 后缀字符串，无后缀返回空字符串
 */
fun File?.getRealFileSuffix(): String {
    this ?: return ""
    val fileName = name ?: return ""
    return fileName.substringAfterLast(".", "").lowercase().trim()
}

/**
 * 从 Uri 解析源文件的真实后缀（绕过临时文件，直接读 Uri 元数据）
 */
fun Uri?.getRealSourceSuffix(context: Context?): String {
    this ?: return ""
    val mContext = context ?: BaseApplication.instance.applicationContext
    // 从媒体库DISPLAY_NAME中提取原始后缀
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    mContext.contentResolver.query(this, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            // 增加非空、长度校验，避免索引越界
            if (!displayName.isNullOrEmpty() && displayName.length > 1 && displayName.contains(".")) {
                val lastDotIndex = displayName.lastIndexOf(".")
                // 确保后缀不是文件名的第一个字符（避免".filename"这种非法格式）
                if (lastDotIndex > 0 && lastDotIndex < displayName.length - 1) {
                    val suffix = displayName.substring(lastDotIndex)
                    // 图片文件：保留正则校验，返回合规图片后缀
                    if (suffix.matches(Regex("\\.(jpg|jpeg|png|bmp|webp)", RegexOption.IGNORE_CASE))) {
                        return suffix
                    }
                    // 非图片文件跳过正则后，直接返回从DISPLAY_NAME提取的原始后缀
                    return suffix
                }
            }
        }
    }
    // 通过MIME类型映射后缀（非图片默认兜底为通用二进制类型）
    val mimeType = mContext.contentResolver.getType(this) ?: "application/octet-stream"
    // 判断是否为图片MIME类型
    val isImageMime = mimeType.startsWith("image/")
    val extension = if (isImageMime) {
        // 图片文件：保留原有图片MIME映射逻辑（完全不变）
        when (mimeType) {
            "image/webp" -> "webp"
            "image/bmp" -> "bmp"
            else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        }
    } else {
        // 非图片文件：通用MIME类型映射，返回真实后缀，不兜底为图片格式
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "tmp"
    }
    val standardSuffix = ".${extension.lowercase()}"
    // 图片文件：保留二进制头部校验（非图片文件无需此步骤，直接返回standardSuffix）
    if (!isImageMime) return standardSuffix
    // 二进制头部校验（仅图片文件执行，逻辑完全不变）
    return try {
        mContext.contentResolver.openInputStream(this)?.use { inputStream ->
            val headerBytes = ByteArray(4)
            val readLength = inputStream.read(headerBytes)
            if (readLength >= 4) {
                when {
                    headerBytes[0] == 0x89.toByte() && headerBytes[1] == 0x50.toByte() && headerBytes[2] == 0x4E.toByte() && headerBytes[3] == 0x47.toByte() -> ".png"
                    headerBytes[0] == 0xFF.toByte() && headerBytes[1] == 0xD8.toByte() && headerBytes[2] == 0xFF.toByte() -> ".jpg"
                    headerBytes[0] == 0x52.toByte() && headerBytes[1] == 0x49.toByte() && headerBytes[2] == 0x46.toByte() && headerBytes[3] == 0x46.toByte() -> ".webp"
                    else -> standardSuffix
                }
            } else {
                standardSuffix
            }
        } ?: standardSuffix
    } catch (e: Exception) {
        e.printStackTrace()
        standardSuffix
    }
}

/**
 * 获取目录下【一级】所有可见项（文件+文件夹）的路径与类型
 * @return Pair列表：First=绝对路径，Second=是否是文件夹（true=文件夹，false=文件）
 * @note 仅遍历当前目录一级，不递归子目录；过滤隐藏文件
 */
fun File?.getFirstLevelPathItems(): List<Pair<String, Boolean>> {
    if (this == null || !this.exists() || !this.isDirectory) {
        "目录不存在或不是文件夹".logWTF
        return emptyList()
    }
    val allItems = this.listFiles { file -> !file.isHidden } ?: return emptyList()
    return allItems.map { it.absolutePath to it.isDirectory }
}

/**
 * 递归获取目录下【所有层级】的所有文件绝对路径（仅文件，不含文件夹）
 * @return 所有子目录文件的绝对路径列表，无数据返回空列表
 * @note 遍历当前目录+所有子目录；包含非隐藏文件（listFiles未过滤隐藏，保持原逻辑）
 */
fun File.getAllFilePathsRecursively(): List<String> {
    if (exists().not() || isDirectory.not()) return emptyList()
    val files = listFiles() ?: return emptyList()
    return files.flatMap { if (it.isFile) listOf(it.absolutePath) else it.getAllFilePathsRecursively() }
}

/**
 * 通过uri获取到一个文件
 */
fun Uri?.getFileFromUri(context: Context?): File? {
    this ?: return null
    return this.toString().getFileFromUri(context)
}

fun String?.getFileFromUri(context: Context?): File? {
    this ?: return null
    val uri = toUri()
    if (uri.path.isNullOrEmpty()) return null
    val mContext = context ?: BaseApplication.instance.applicationContext
    return when {
        // file:// 协议：本地文件（私有/低版本公共目录）
        uri.scheme == "file" -> {
            File(this).takeIf { it.exists() && it.canRead() }
        }
        // http/https 协议：网络文件（直接返回null，提示需下载）
        uri.scheme == "http" || uri.scheme == "https" -> {
            "网络文件需先下载到本地再上传".logWTF
            null
        }
        // content:// 协议：基础媒体/共享文件 + 各类子类型
        uri.scheme == "content" -> {
            when (uri.authority) {
                // 外接存储文件：content://com.android.externalstorage.documents
                "com.android.externalstorage.documents" -> getFileFromExternalStorageDoc(uri)
                // 系统下载目录文件：content://com.android.providers.downloads.documents
                "com.android.providers.downloads.documents" -> getFileFromDownloadDoc(mContext, uri)
                // 谷歌相册文件：content://com.google.android.apps.photos.content
                "com.google.android.apps.photos.content" -> getFileFromCloudAlbum(mContext, uri, "google_album_")
                // 微信文件：content://com.tencent.mm.opensdk.fileprovider
                "com.tencent.mm.opensdk.fileprovider" -> getFileFromCloudAlbum(mContext, uri, "wechat_file_")
                // 邮件附件等其他content子类型：通过流转临时文件
                else -> getFileFromCommonContent(mContext, uri)
            }
        }
        // document:// 协议：文件管理器选择的文件（SAF框架）
        DocumentsContract.isDocumentUri(context, uri) -> {
            getFileFromDocumentUri(mContext, uri)
        }
        // 其他协议（如android.resource://）：无法转File，返回null
        else -> null
    }
}

/**
 * 外接存储文件（U盘/SD卡）
 */
private fun getFileFromExternalStorageDoc(uri: Uri): File? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":")
    if (split.size < 2) return null
    val filePath = if ("primary".equals(split[0], ignoreCase = true)) {
        "/storage/emulated/0/${split[1]}"
    } else {
        "/storage/${split[0]}/${split[1]}"
    }
    return File(filePath).takeIf { it.exists() }
}

/**
 * 系统下载目录文件
 */
private fun getFileFromDownloadDoc(context: Context, uri: Uri): File? {
    val docId = DocumentsContract.getDocumentId(uri)
    return if (docId.startsWith("raw:")) {
        // 直接路径
        File(docId.replace("raw:", "")).takeIf { it.exists() }
    } else {
        // 媒体类型文件，复用MediaStore逻辑
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getFileFromContentUriQPlus(context, uri)
        } else {
            getFileFromContentUriLegacy(context, uri)
        }
    }
}

/**
 * 云相册/第三方App文件（谷歌相册、微信等，通过流转临时文件）
 * 1) Android 10+ 开启 Scoped Storage 后，App 无法直接访问「其他 App 私有目录」「媒体库非应用私有目录」的文件（比如微信保存的图片、谷歌相册的图片）
 * 2) 只能通过 ContentResolver.openInputStream(uri) 读取文件内容，无法直接获取源文件的真实路径。
 * 3) 把源文件内容拷贝到 App 缓存目录（生成 .tmp 文件），拿到一个可直接操作的 File 对象（上传、解析等）。
 */
private fun getFileFromCloudAlbum(context: Context, uri: Uri, prefix: String): File? {
    return try {
        /**
         * prefix="google_album_", suffix=".tmp" → 文件名是 "google_album_123456789.tmp"
         * 1) 服务端通常只关心文件的「MIME 类型」「二进制内容」，不会因为文件名是 .tmp 拒绝接收
         * 2) 若服务端对文件名 / 后缀有要求（比如需要 .jpg/.png），可以在上传时手动指定文件名
         * val tempUri = it.data?.data
         * val tempFile = tempUri.getFileFromUri(this)
         * if (tempFile != null) {
         *     // 获取源文件的真实后缀（比如从Uri/文件名解析）
         *     val realSuffix = tempUri.getRealSourceSuffix(this)
         * }
         */
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // 获取真实后缀
            val realSuffix = uri.getRealSourceSuffix(context)
            // 先生成临时文件（避免直接创建带真实后缀的文件冲突）
            val tempTmpFile = File.createTempFile(prefix, ".tmp", context.cacheDir)
            tempTmpFile.deleteOnExit()
            // 拷贝文件流到临时.tmp文件
            FileOutputStream(tempTmpFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            // 重命名文件：将.tmp替换为真实后缀（避免文件名冲突）
            val realFileName = prefix + System.currentTimeMillis() + realSuffix
            val realFile = File(context.cacheDir, realFileName)
            val renameSuccess = tempTmpFile.renameTo(realFile)
            // 返回重命名后的真实后缀文件（重命名失败则返回原.tmp文件）
            if (renameSuccess) {
                // 设置JVM退出自动删除
                realFile.deleteOnExit()
                realFile
            } else {
                tempTmpFile
            }
        }
    } catch (e: Exception) {
        e.logWTF
        null
    }
}

/**
 * 普通content://文件（媒体/共享文件）
 */
private fun getFileFromCommonContent(context: Context, uri: Uri): File? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getFileFromContentUriQPlus(context, uri) ?: getFileFromCloudAlbum(context, uri, "media_")
    } else {
        getFileFromContentUriLegacy(context, uri)
    }
}

/**
 * 适配：document:// 协议文件
 */
private fun getFileFromDocumentUri(context: Context, uri: Uri): File? {
    val docId = DocumentsContract.getDocumentId(uri)
    return when (uri.authority) {
        "com.android.providers.media.documents" -> {
            val split = docId.split(":")
            if (split.size < 2) return null
            val mediaUri = when (split[0]) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> return null
            }
            val queryUri = mediaUri.buildUpon().appendQueryParameter("_id", split[1]).build()
            getFileFromCommonContent(context, queryUri)
        }
        else -> null
    }
}

/**
 * Android 10+ 从ContentUri获取文件（无_data依赖）
 */
private fun getFileFromContentUriQPlus(context: Context, uri: Uri): File? {
    val projection = arrayOf(MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
            val publicDir = context.externalMediaDirs.firstOrNull()?.parent?.replace("/Android/media/${context.packageName}", "") ?: "/storage/emulated/0"
            val filePath = "$publicDir/$relativePath/$displayName"
            return File(filePath).takeIf { it.exists() }
        }
    }
    return null
}

/**
 * 低版本（Android < 10）从ContentUri获取文件
 */
private fun getFileFromContentUriLegacy(context: Context, uri: Uri): File? {
    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val filePath = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
            return File(filePath).takeIf { it.exists() }
        }
    }
    return null
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
        // 视频时长（毫秒）/1000=x秒
        val duration = player.duration.orZero
        duration.divide(1000, ROUND_HALF_UP).toSafeInt().apply { "文件时长：${this}秒".logE() }
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