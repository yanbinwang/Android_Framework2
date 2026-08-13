package com.example.common.utils.function

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.common.BaseApplication
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toFixed
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logWTF
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode

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
fun Context.insertImageResolver(pathname: String?): Boolean {
    if (pathname.isNullOrEmpty()) return false
    return insertImageResolver(pathname.toSafeFile())
}

fun Context.insertImageResolver(file: File?): Boolean {
    file ?: return false
    // 文件不存在或不可读
    if (!file.exists() || !file.canRead()) return false
    // 适配 Android 10+（Scoped Storage）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        insertImageToMediaStoreQPlus(file)
    } else {
        // 低版本保留原有逻辑
        MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null)
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, "file://${file.path}".toUri()))
    }
    return true
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
        contentResolver.openOutputStream(uri)?.use { output ->
            file.inputStream().use { input ->
                input.copyTo(output)
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
 * 获取当前手机缓存目录下的缓存文件大小,
 * @return 返回格式化后的缓存大小字符串，如 "2.5M"
 */
fun Context?.getFormattedCacheSize(): String {
    var formattedSize = "0M"
    this ?: return formattedSize
    // 安全获取缓存目录，计算总大小并格式化
    cacheDir?.takeIf { it.exists() }?.apply {
        val totalCacheBytes = getFileTotalSize()
        formattedSize = if (totalCacheBytes > 0) {
            storageSizeFormat()
        } else {
            formattedSize
        }
    }
    return formattedSize
}

/**
 * 获取对应大小的文字
 * 新api --> Formatter.formatFileSize()
 */
private const val STORAGE_UNIT_BASE = 1024.0

fun String?.storageSizeFormat(): String {
    this ?: return ""
    return toSafeFile().storageSizeFormat()
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
        kb < STORAGE_UNIT_BASE -> "${kb.toFixed(2, RoundingMode.HALF_UP)}K"
        kb < STORAGE_UNIT_BASE * STORAGE_UNIT_BASE -> "${(kb / STORAGE_UNIT_BASE).toFixed(2, RoundingMode.HALF_UP)}M"
        kb < STORAGE_UNIT_BASE * STORAGE_UNIT_BASE * STORAGE_UNIT_BASE -> "${(kb / (STORAGE_UNIT_BASE * STORAGE_UNIT_BASE)).toFixed(2, RoundingMode.HALF_UP)}GB"
        else -> "${(kb / (STORAGE_UNIT_BASE * STORAGE_UNIT_BASE * STORAGE_UNIT_BASE)).toFixed(2, RoundingMode.HALF_UP)}TB"
    }
}

/**
 * 将路径字符串安全地转换为 File 对象
 * - null / 空串 / 纯空白 → 返回 null
 * - 其他情况 → trim 后返回 File（不涉及 I/O）
 */
fun String?.toSafeFile(): File? {
    this ?: return null
    val trimmed = trim()
    return if (trimmed.isEmpty()) null else File(trimmed)
}

/**
 * 获取字符串路径对应的文件/目录长度
 * 1) 若为文件：返回文件大小（字节）
 * 2) 若为目录：返回 0L（目录本身无大小，需用 [getFileTotalSize] 统计子文件总大小）
 * 3) 路径为空/文件不存在/异常：返回 0L
 */
fun String?.getFileLength(): Long {
    this ?: return 0L
    return toSafeFile().getFileLength()
}

fun File?.getFileLength(): Long {
    this ?: return 0L
    return try {
        if (isFile) length() else 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * 文件本身的整体大小
 */
fun String?.getFileTotalSize(): Long {
    this ?: return 0L
    return toSafeFile().getFileTotalSize()
}

fun File?.getFileTotalSize(): Long {
    this ?: return 0L
    return try {
        // 如果是文件，直接返回大小（无需遍历）
        if (isFile) {
            length()
        } else {
            // 遍历子文件（orEmpty() 处理 listFiles() 返回 null 的情况）
            var size = 0L
            for (file in listFiles().orEmpty()) {
                size += if (file.isDirectory) {
                    // 递归调用时要传 file
                    file.getFileTotalSize()
                } else {
                    file.length()
                }
            }
            // 返回所有子文件/子目录的内容大小总和（不含目录自身元数据）
            size
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * 校验文件是否无独占写锁定、可删除（间接判断）
 * @param this 文件路径
 * @return true：无写锁定，可尝试删除；false：有写锁定/占用
 */
fun String?.isFileWritableAndDeletable(): Boolean {
    this ?: return false
    // 文件是否可写（间接判断无独占写锁定）
    val file = toSafeFile() ?: return false
    if (!file.exists() || !file.isFile) return false
    /**
     * 尝试以追加模式打开目标文件本身
     * 1) 如果文件被其他进程独占锁定，此处会抛 IOException
     * 2) 在父目录创建临时文件只能证明"父目录可写"，不能证明目标文件本身没有被独占锁定。
     *   例如一个视频正在被播放器占用，canWrite() 可能仍返回 true，且父目录也能创建临时文件，但实际删除该视频会失败
     */
    return try {
        // 仅测试能否打开，不写入任何数据
        FileOutputStream(file, true).use {}
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 判断字符串路径对应的文件/目录是否存在（仅做存在性校验）
 * 注意：本方法仅检查 [File.exists]，不包含可读/可写权限校验
 * 1) 存在性与访问权限是两个独立的关注点。若在此处耦合 canRead()/canWrite()，会导致无法区分"路径未创建"与"权限不足"两种截然不同的错误语义，增加上层排查问题的难度
 * 2) 如需确认路径是否可安全读写，在上层业务中结合具体 I/O 意图单独进行权限预检或直接 try-catch 实际 I/O 操作
 * @return true：路径非空且对应的文件/目录在文件系统中已创建；false：路径为空或尚未创建
 */
fun String?.isPathExists(): Boolean {
    val file = toSafeFile() ?: return false
    return file.exists()
}

/**
 * 确保目录存在（不存在则创建），返回目录绝对路径
 * mkdirs():创建目录（文件夹）
 * createNewFile():创建文件
 */
fun String?.ensureDirExists(): String {
    // 空路径直接返回空
    this ?: return ""
    // 取得文件类
    val dirFile = toSafeFile() ?: return ""
    // 目录已存在 → 直接返回绝对路径；路径存在但不是目录 → 返回空
    return try {
        if (dirFile.exists()) {
            if (dirFile.isDirectory) dirFile.absolutePath else ""
        } else {
            // 目录不存在 → 创建多级目录（mkdirs() 支持多级）；创建成功返回路径，失败返回空
            if (dirFile.mkdirs()) dirFile.absolutePath else ""
        }
    } catch (e: Exception) {
        // mkdirs() 不是完全安全的
        e.printStackTrace()
        ""
    }
}

/**
 * 获取不包含后缀名的文件名
 * // 假设文件路径是：/sdcard/wallets/my-wallet.json
 * val file = File("/sdcard/wallets/my-wallet.json")
 * println(file.name)                 // 输出：my-wallet.json（带后缀的文件名）
 * println(file.path)                 // 输出：/sdcard/wallets/my-wallet.json（原始传入路径）
 * println(file.absolutePath)         // 输出：/sdcard/wallets/my-wallet.json（绝对路径）
 * println(file.parent)               // 输出：/sdcard/wallets（父目录）
 * println(file.nameWithoutExtension) // 输出：my-wallet（不含后缀的文件名）
 * println(file.extension)            // 输出：json（后缀名，不带点号）
 *
 * path 与 absolutePath 区别
 * // 传入绝对路径 → 两者相同
 * val f1 = File("/sdcard/wallets/my-wallet.json")
 * println(f1.path)                   // 输出：/sdcard/wallets/my-wallet.json
 * println(f1.absolutePath)           // 输出：/sdcard/wallets/my-wallet.json
 * // 传入相对路径 → 两者不同
 * val f2 = File("wallets/my-wallet.json")
 * println(f2.path)                   // 输出：wallets/my-wallet.json
 * println(f2.absolutePath)           // 输出：/data/user/0/com.example.app/files/wallets/my-wallet.json
 */
fun String?.nameWithoutExtension(): String {
    this ?: return ""
    return toSafeFile()?.nameWithoutExtension.orEmpty()
}

/**
 * 获取文件后缀名
 */
fun String?.extension(): String {
    this ?: return ""
    return toSafeFile()?.extension.orEmpty()
}

/**
 * 获取文件父目录
 */
fun String?.parent(): String {
    this ?: return ""
    return toSafeFile()?.parent.orEmpty()
}

/**
 * 删除文件
 */
fun String?.deleteFile(): Boolean {
    this ?: return false
    return toSafeFile()?.safeDelete().orFalse
}

/**
 * 删除目录下的所有文件,包含目录本身
 */
fun String?.deleteDirectory(): Boolean {
    this ?: return false
    return toSafeFile()?.let {
        if (it.isDirectory) {
            it.deleteRecursively()
        } else {
            it.safeDelete()
        }
    }.orFalse
}

/**
 * 安全删除文件（处理文件占用等异常）
 */
fun File?.safeDelete(): Boolean {
    // 避免空路径文件
    this ?: return false
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
             * 1) 当调用 file.deleteOnExit() 时，JVM 会将该文件的路径添加到一个内部注册表中（本质是一个线程安全的集合）
             * 2) 当 JVM 正常终止（比如 App 正常退出、进程被系统正常回收）时，会遍历这个注册表，尝试删除所有注册的文件，删除顺序与注册顺序相反（先注册的后删除）
             * 3) 仅对「文件」有效，对「目录」无效（目录需手动删除或用 deleteRecursively()）
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
 * 从图片缓存文件的二进制头推断真实扩展名。
 * 专用于 Glide/Coil 等图片库产生的无后缀缓存文件。
 * 若文件已有可信后缀，应优先使用 [File.extension]。
 * @return 小写扩展名（如 "jpeg"、"webp"），无法识别时返回 null
 */
fun File.cacheImageExtension(): String? {
    if (!exists() || !isFile || length() == 0L) return null
    // 仅解码边界信息（宽高、MIME），不加载像素到内存
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(absolutePath, options)
    // outMimeType 格式为 "image/jpeg"、"image/webp" 等；解码失败时返回 "image/*"，需过滤掉
    return options.outMimeType?.substringAfterLast('/')?.takeIf { it != "*" && it.isNotBlank() }
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
    // 确保父目录存在（极端情况父目录被删除，避免重命名失败）
    val parentDir = parentFile ?: return false
    if (!parentDir.exists() && !parentDir.mkdirs()) return false
    // 创建目标文件（新路径 + 新文件名）
    val targetFile = File(parentDir, newFileName)
    // 避免覆盖已存在的文件
    if (targetFile.exists()) return false
    // 执行重命名操作
    return renameTo(targetFile)
}

/**
 * 移动文件到新路径（可改路径 + 文件名）
 * @param this 原始文件
 * @param targetFile 目标文件（包含新路径和新文件名）
 * @return 是否移动成功
 * 同分区：仅修改路径和名称，速度极快；
 * 跨分区：先复制再删除原文件，速度取决于文件大小；
 * 无论哪种情况，成功后原文件消失，仅目标文件存在。
 */
fun File?.moveFileTo(targetFile: File): Boolean {
    this ?: return false
    if (!exists() || !isFile) return false
    val targetParentDir = targetFile.parentFile ?: return false
    if (!targetParentDir.exists() && !targetParentDir.mkdirs()) return false
    // 目标已存在则不覆盖
    if (targetFile.exists()) return false
    // 优先尝试 rename（同分区零拷贝）
    if (renameTo(targetFile)) return true
    // 跨分区兜底：复制 + 删除
    return try {
        copyTo(targetFile, overwrite = false)
        safeDelete()
    } catch (e: Exception) {
        e.printStackTrace()
        targetFile.safeDelete()
        false
    }
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
    if (this == null || !this.exists() || !this.isDirectory) return null
    /**
     * 获取目录下所有文件/文件夹（过滤隐藏文件）
     * 例子:
     * 按修改时间排序
     * Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
     */
    val files = this.listFiles { file ->
        // 仅保留「非隐藏」且「是文件」的项（排除文件夹）
        !file.isHidden && file.isFile
    }
    // 判断是否有文件，返回第一个文件的路径
    return if (files != null && files.size > 0) {
        files[0]?.absolutePath
    } else {
        null
    }
}

/**
 * 获取目录下【一级】所有可见项（文件+文件夹）的路径与类型
 * @return Pair列表：First=绝对路径，Second=是否是文件夹（true=文件夹，false=文件）
 * @note 仅遍历当前目录一级，不递归子目录；过滤隐藏文件
 */
fun File?.getFirstLevelPathItems(): List<Pair<String, Boolean>> {
    // 目录不存在或不是文件夹
    if (this == null || !this.exists() || !this.isDirectory) return emptyList()
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
        // 图片文件：保留原有图片MIME映射逻辑
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
    // 二进制头部校验（仅图片文件执行）
    return try {
        mContext.contentResolver.openInputStream(this)?.use { input ->
            val headerBytes = ByteArray(4)
            val readLength = input.read(headerBytes)
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
 * 通过 Uri 获取到一个文件
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
        context.contentResolver.openInputStream(uri)?.use { input ->
            // 获取真实后缀
            val realSuffix = uri.getRealSourceSuffix(context)
            // 先生成临时文件（避免直接创建带真实后缀的文件冲突）
            val tempTmpFile = File.createTempFile(prefix, ".tmp", context.cacheDir)
            tempTmpFile.deleteOnExit()
            // 拷贝文件流到临时.tmp文件
            FileOutputStream(tempTmpFile).use { output ->
                input.copyTo(output)
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