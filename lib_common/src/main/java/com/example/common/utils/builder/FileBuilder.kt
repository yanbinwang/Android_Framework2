package com.example.common.utils.builder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.Bitmap.CompressFormat.WEBP
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Patterns
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL
import androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
import androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE
import androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION
import com.example.common.R
import com.example.common.network.CommonApi
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.function.copy
import com.example.common.utils.function.deleteDir
import com.example.common.utils.function.getBase64
import com.example.common.utils.function.getDuration
import com.example.common.utils.function.getHash
import com.example.common.utils.function.isMkdirs
import com.example.common.utils.function.loadBitmap
import com.example.common.utils.function.loadLayout
import com.example.common.utils.function.pt
import com.example.common.utils.function.read
import com.example.common.utils.function.safeRecycle
import com.example.common.utils.function.scaleBitmap
import com.example.common.utils.function.split
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.glide.ImageLoader
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 存储图片保存bitmap
 * root->图片保存路径
 * fileName->图片名称（扣除jpg和png的后缀）
 * deleteDir->是否清除目录
 * format->图片类型
 * quality->压缩率
 */
suspend fun suspendingSavePic(bitmap: Bitmap?, root: String = getStoragePath("Save Image"), fileName: String = EN_YMDHMS.convert(Date()), deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, quality: Int = 100): String? {
    return withContext(IO) {
        if (null != bitmap) {
            // 存储目录文件
            val storeDir = File(root)
            // 先判断是否需要清空目录，再判断是否存在（不存在则创建）
            if (deleteDir) root.deleteDir()
            root.isMkdirs()
            // 根据要保存的格式，返回对应后缀名->安卓只支持以下三种
            val suffix = when (format) {
                JPEG -> "jpg"
                PNG -> "png"
                WEBP -> "webp"
                else -> "jpg"
            }
            // 在目录文件夹下生成一个新的图片
            val file = File(storeDir, "${fileName}.${suffix}")
            // 开流开始写入
            file.outputStream().use { outputStream ->
                //如果是Bitmap.CompressFormat.PNG，无论quality为何值，压缩后图片文件大小都不会变化
                bitmap.compress(format, if (format != PNG) quality else 100, outputStream)
                outputStream.flush()
                bitmap.safeRecycle()
            }
            file.absolutePath
        } else {
            null
        }
    }
}

/**
 * 存储pdf
 */
suspend fun suspendingSavePDF(file: File): MutableList<String?> {
    val list = ArrayList<String?>()
    withContext(IO) {
        PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use {
            for (index in 0 until it.pageCount) {
                val filePath = suspendingSavePDF(it, index)
                list.add(filePath)
            }
        }
    }
    return list
}

suspend fun suspendingSavePDF(file: File, index: Int = 0): String? {
    return withContext(IO) {
        suspendingSavePDF(PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)), index)
    }
}

suspend fun suspendingSavePDF(renderer: PdfRenderer, index: Int = 0): String? {
    // 选择渲染哪一页的渲染数据
    return renderer.use {
        if (index > it.pageCount - 1) return null
        val page = it.openPage(index)
        val width = page.width
        val height = page.height
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val rent = Rect(0, 0, width, height)
        page.render(bitmap, rent, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        suspendingSavePic(bitmap)
    }
}

/**
 * 存储绘制的view->构建图片
 * 需要注意，如果直接写100而不是100.pt的话，是会直接100像素写死的，但是内部字体宽度大小也是像素，整体兼容性上会不是很好，而写成100.pt后，会根据手机宽高做一定的转化
 * val view = ViewTestBinding.bind(inflate(R.layout.view_test)).root
 * view.measure(WRAP_CONTENT, WRAP_CONTENT)//不传height的时候要加，高改为view.measuredHeight
 * builder.saveViewJob(view, 100, 100, {
 * showDialog()
 * }, {
 * hideDialog()
 * insertImageResolver(File(it.orEmpty()))
 * })
 * 弹窗的 show() 是当前主线程消息，会优先执行，所以弹窗能正常弹出、转圈动画流畅；
 * View 操作被 post 到下一个消息队列，等弹窗渲染完成后再执行，即使耗时久，也不会让弹窗 “卡着不显示”；
 * 协程通过 deferred.await() 挂起等待结果，不会阻塞主线程其他操作。
 */
suspend fun suspendingSaveView(view: View, targetWidth: Int = screenWidth, targetHeight: Int = WRAP_CONTENT, isScale: Boolean = false): Bitmap? {
    // 切换到主线程执行所有View操作（避免线程问题）
    return withContext(Main.immediate) {
        withTimeoutOrNull(5000) {
            try {
                // 强制触发View测量/强制布局（确保View有位置和尺寸）
                val mTargetHeight = if (targetHeight < 0) 0 else targetHeight
                if (isScale) {
                    view.loadLayout(targetWidth.pt, mTargetHeight.pt)
                    // 得到测绘后的值(如果是缩放会是pt后的值)
                    val measuredWidth = view.measuredWidth
                    val measuredHeight = view.measuredHeight
                    // 计算最终绘制尺寸（处理缩放）
                    val finalHeight = if (targetHeight < 0) {
                        // 计算缩放比例
                        val scaleRatio = targetWidth.toSafeFloat() / measuredWidth.toSafeFloat()
                        // 计算图片等比例放大后的高
                        (measuredHeight * scaleRatio).toSafeInt()
                    } else {
                        measuredHeight
                    }
                    // 根据原view大小绘制出bitmap
                    val screenBit = createBitmap(measuredWidth, finalHeight)
                    val canvas = Canvas(screenBit)
                    canvas.drawColor(Color.TRANSPARENT)
                    // View.draw()方法是必须在主线程执行
                    view.draw(canvas)
                    // 根据实际宽高缩放
                    withContext(IO) { screenBit.scaleBitmap(targetWidth, mTargetHeight) }
                } else {
                    // 直接计算最终需要的宽高（合并原 mTargetHeight 和 finalHeight 的逻辑）
                    val finalHeight = if (targetHeight < 0) {
                        // targetHeight<0（自适应），先临时布局获取原始比例
                        // 临时用 targetWidth 布局，获取 View 真实的宽高比例
                        view.loadLayout(targetWidth, 0) // 高度传0（UNSPECIFIED），让 View 自适应
                        val tempMeasuredWidth = view.measuredWidth
                        val tempMeasuredHeight = view.measuredHeight
                        // 按目标宽度计算自适应高度（保持原比例）
                        (tempMeasuredHeight.toSafeFloat() * targetWidth / tempMeasuredWidth.toSafeFloat()).toSafeInt()
                    } else {
                        // targetHeight>=0（固定高度），直接用目标高
                        targetHeight
                    }
                    // 仅执行1次布局（用最终确定的宽高，避免重复）
                    view.loadLayout(targetWidth, finalHeight)
                    // 校验布局结果（确保尺寸有效）
                    if (view.measuredWidth != targetWidth || view.measuredHeight != finalHeight) {
                        throw IllegalStateException("View布局尺寸与目标尺寸不匹配：实际(${view.measuredWidth}x${view.measuredHeight})，目标(${targetWidth}x${finalHeight})")
                    }
                    // 生成 Bitmap（直接用 View 布局后的尺寸，避免尺寸不匹配）
                    view.loadBitmap(targetWidth, finalHeight)
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/**
 * 旋转图片
 * 修整部分图片方向不正常
 * 取得一个新的图片文件
 */
/**
 * 旋转图片
 * 修整部分图片方向不正常
 * 取得一个新的图片文件
 */
suspend fun suspendingDegree(file: File, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, quality: Int = 100, originalDegree: Int = -1): File? {
    return try {
        withContext(IO) {
            val degree = if (-1 == originalDegree) {
                readImageRotation(file.absolutePath)
            } else {
                originalDegree
            }
            // 如果不需要旋转，直接返回原文件
            if (degree == 0) {
                return@withContext file
            }
            // 解码图片,解码失败返回原文件
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext file
            // 执行旋转
            val rotatedBitmap = try {
                // 应用旋转角度
                val matrix = Matrix().apply {
                    postRotate(degree.toSafeFloat())
                }
                // 按原始尺寸旋转，第三个参数true=保持图片抗锯齿（更清晰）
                Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            } catch (e: Exception) {
                // 旋转失败时返回原图（如内存不足）
                e.printStackTrace()
                originalBitmap
            }
            // 根据格式，返回对应后缀名->安卓只支持以下三种
            val suffix = when (format) {
                JPEG -> "jpg"
                PNG -> "png"
                WEBP -> "webp"
                else -> "jpg"
            }
            // 文件名：原图名 + "_rotated"（如 "photo.jpg" → "photo_rotated.jpg"）
            val rotatedFileName = file.name.replace(Regex("\\.${suffix}$"), "_rotated.${suffix}")
            val rotatedFile = File(file.parent ?: getStoragePath("Save Image"), rotatedFileName)
            // 保存旋转后的图片
            rotatedFile.outputStream().use { outputStream ->
                // 如果是PNG，无论quality为何值，压缩后图片文件大小都不会变化
                val actualQuality = if (format == PNG) 100 else quality
                rotatedBitmap.compress(format, actualQuality, outputStream)
            }
            // 回收资源
            if (originalBitmap != rotatedBitmap) {
                originalBitmap.safeRecycle()
            }
            rotatedBitmap.safeRecycle()
            // 删除原文件
            if (deleteDir && rotatedFile.exists() && file.exists()) {
                file.delete()
            }
            if (rotatedFile.exists() && rotatedFile.length() > 0) rotatedFile else file
        }
    } catch (e: Exception) {
        throw e
    }
}

/**
 * 读取图片的旋转角度:
 * 1)使用手机相机拍摄照片时，相机会在图片文件（如 JPEG）中嵌入一组 Exif 元数据，其中包含拍摄时的设备方向、焦距、时间等信息
 * 2)通过路径访问图片时，程序实际是在解析图片文件的二进制数据，从中提取出 Exif 区块中的Orientation值。这个值不是手机 “实时判断” 的，而是拍摄时就已经写入文件了
 *
 * 读不出角度的常见原因:
 * 1)图片经过编辑、压缩或格式转换后，Exif 信息可能被清除
 * 2)部分相机应用未规范写入Orientation标签
 * 3)非相机拍摄的图片（如截图、网络图片）通常没有旋转角度信息
 * 4)Android 10 + 的文件权限限制，可能导致直接通过路径无法读取 Exif（需用输入流方式）
 */
private fun readImageRotation(path: String): Int {
    return try {
        // 对于Android Q及以上版本，推荐使用ExifInterface的InputStream构造方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            readRotationFromStream(path)
        } else {
            readRotationFromPath(path)
        }
    } catch (e: Exception) {
        // 输出异常以便调试
        e.printStackTrace()
        0
    }
}

/**
 * 从文件路径读取旋转角度（适用于Android Q以下）
 */
private fun readRotationFromPath(path: String): Int {
    val exif = ExifInterface(path)
    return getRotationFromExif(exif)
}

/**
 * 从输入流读取旋转角度（适用于Android Q及以上）
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun readRotationFromStream(path: String): Int {
    // 对于Android Q及以上，使用输入流方式更可靠
    return try {
        File(path).inputStream().use { inputStream ->
            val exif = ExifInterface(inputStream)
            getRotationFromExif(exif)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        // 尝试降级使用路径方式
        readRotationFromPath(path)
    }
}

/**
 * 从ExifInterface中解析旋转角度
 */
private fun getRotationFromExif(exif: ExifInterface): Int {
    val orientation = exif.getAttributeInt(TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    return when (orientation) {
        ORIENTATION_ROTATE_90 -> 90
        ORIENTATION_ROTATE_180 -> 180
        ORIENTATION_ROTATE_270 -> 270
        // 水平翻转无需旋转
        ORIENTATION_FLIP_HORIZONTAL -> 0
        // 垂直翻转等效180度
        ORIENTATION_FLIP_VERTICAL -> 180
        // 转置等效90度
        ORIENTATION_TRANSPOSE -> 90
        // 反转置等效270度
        ORIENTATION_TRANSVERSE -> 270
        // 其他情况默认不需要旋转
        else -> 0
    }
}

/**
 * 压缩单个文件或目录到指定ZIP路径
 * @param sourcePath 源文件或目录路径
 * @param zipPath 目标ZIP文件路径
 * @return 生成的ZIP文件路径
 */
suspend fun suspendingZip(sourcePath: String, zipPath: String): String {
    return suspendingZip(listOf(sourcePath), zipPath)
}

/**
 * 压缩多个文件或目录到指定ZIP路径
 * @param sourcePaths 源文件或目录路径列表
 * @param zipPath 目标ZIP文件路径
 * @return 生成的ZIP文件路径
 */
suspend fun suspendingZip(sourcePaths: List<String>, zipPath: String): String {
    return withContext(IO) {
        // 创建ZIP文件所在的目录，而不是ZIP文件本身
        val zipFile = File(zipPath)
        zipFile.parentFile?.mkdirs()
        // 检查ZIP文件是否已存在，如果存在则删除
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw IOException("无法删除已存在的ZIP文件: $zipPath")
            }
        }
        zipFiles(sourcePaths, zipPath)
        zipPath
    }
}

/**
 * 压缩文件或目录列表到ZIP文件
 */
private fun zipFiles(sourcePaths: List<String>, zipPath: String) {
    ZipOutputStream(FileOutputStream(zipPath)).use { zipOut ->
        for (sourcePath in sourcePaths) {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) continue
            if (sourceFile.isFile) {
                addFileToZip(sourceFile, sourceFile.name, zipOut)
            } else if (sourceFile.isDirectory) {
                addDirectoryToZip(sourceFile, "", zipOut)
            }
        }
    }
}

/**
 * 将单个文件添加到ZIP流
 */
private fun addFileToZip(file: File, entryName: String, zipOut: ZipOutputStream) {
    file.inputStream().use { input ->
        zipOut.putNextEntry(ZipEntry(entryName))
        val buffer = ByteArray(4096)
        var length: Int
        while (input.read(buffer).also { length = it } > 0) {
            zipOut.write(buffer, 0, length)
        }
        zipOut.closeEntry()
    }
}

/**
 * 递归将目录添加到ZIP流
 */
private fun addDirectoryToZip(dir: File, parentPath: String, zipOut: ZipOutputStream) {
    val entryPath = if (parentPath.isEmpty()) dir.name else "$parentPath/${dir.name}"
    // 添加目录条目（必须以斜杠结尾）
    zipOut.putNextEntry(ZipEntry("$entryPath/"))
    zipOut.closeEntry()
    // 递归处理子文件和子目录
    dir.listFiles()?.forEach { file ->
        if (file.isFile) {
            addFileToZip(file, "$entryPath/${file.name}", zipOut)
        } else if (file.isDirectory) {
            addDirectoryToZip(file, entryPath, zipOut)
        }
    }
}

/**
 * 存储文件
 */
suspend fun suspendingDownload(downloadUrl: String, filePath: String, fileName: String, listener: (progress: Int) -> Unit = {}): String? {
    if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
        throw RuntimeException(string(R.string.linkError))
    }
    // 清除目录下的所有文件
    filePath.deleteDir()
    // 创建一个安装的文件，开启io协程写入
    val file = File(filePath.isMkdirs(), fileName)
    return withContext(IO) {
        try {
            // 开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
            val body = CommonApi.instance.getDownloadApi(downloadUrl)
            val buf = ByteArray(2048)
            val total = body.contentLength()
            body.byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    var len: Int
                    var sum = 0L
                    while (((inputStream.read(buf)).also { len = it }) != -1) {
                        outputStream.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toSafeInt()
                        withContext(Main) { listener.invoke(progress) }
                    }
                    outputStream.flush()
                    file.path
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

/**
 * 存储网络路径图片(下载url)
 */
suspend fun suspendingDownloadPic(mContext: Context, string: String, root: String = getStoragePath("Save Image"), deleteDir: Boolean = false): String {
    return withContext(IO) {
        // 存储目录文件
        val storeDir = File(root)
        // 先判断是否需要清空目录，再判断是否存在（不存在则创建）
        if (deleteDir) root.deleteDir()
        root.isMkdirs()
        suspendingGlideDownload(mContext, string, storeDir)
    }
}

private suspend fun suspendingGlideDownload(mContext: Context, string: String, storeDir: File) = suspendCancellableCoroutine {
    ImageLoader.instance.downloadImage(mContext, string) { file ->
        // 此处`file?.name`会包含glide下载图片的后缀（png,jpg,webp等）
        if (null == file || !file.exists()) {
            it.resumeWithException(RuntimeException("下载失败"))
        } else {
            file.copy(storeDir)
            file.delete()
            it.resume("${storeDir.absolutePath}/${file.name}")
        }
    }
}

/**
 * 文件分割
 */
suspend fun suspendingFileSplit(sourcePath: String?, cutSize: Long): MutableList<String> {
    sourcePath ?: return arrayListOf()
    return withContext(IO) {
        File(sourcePath).split(cutSize)
    }
}

/**
 * 读取文件
 */
suspend fun suspendingFileRead(sourcePath: String?): String {
    sourcePath ?: return ""
    return withContext(IO) {
        File(sourcePath).read()
    }
}

/**
 * 复制文件(将当前文件拷贝一份到目标路径)
 */
suspend fun suspendingFileCopy(sourcePath: String?, destPath: String?) {
    if (sourcePath == null || destPath == null) return
    withContext(IO) {
        File(sourcePath).copy(File(destPath))
    }
}

/**
 * 获取文件采用base64形式
 */
suspend fun suspendingFileBase64(sourcePath: String?): String {
    sourcePath ?: return ""
    return withContext(IO) {
        File(sourcePath).getBase64()
    }
}

/**
 * 获取文件hash值
 */
suspend fun suspendingFileHash(sourcePath: String?): String {
    sourcePath ?: return ""
    return withContext(IO) {
        File(sourcePath).getHash()
    }
}

/**
 * 获取media文件的时长
 * 返回时长(音频，视频)->不支持在线音视频
 * 放在线程中读取，超时会导致卡顿或闪退
 */
suspend fun suspendingFileDuration(sourcePath: String?): Int {
    sourcePath ?: return 0
    return withContext(IO) {
        File(sourcePath).getDuration()
    }
}

/**
 * 生成崩溃日志内容
 */
fun generateCrashLog(throwable: Throwable, thread: Pair<String, Long> = Thread.currentThread().let { it.name to it.id }): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    // 写入异常信息
    throwable.printStackTrace(printWriter)
    var cause = throwable.cause
    while (cause != null) {
        cause.printStackTrace(printWriter)
        cause = cause.cause
    }
    val exceptionInfo = stringWriter.toString()
    printWriter.close()
    // 构建日志内容（包含设备信息和异常信息）
    return buildString {
        append("===== 崩溃时间: $currentTimeStamp =====\n")
        append("设备型号: ${Build.MODEL}\n")
        append("系统版本: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
        append("崩溃线程: ${thread.first} (id: ${thread.second})\n")
        append("===== 异常信息 =====\n")
        append(exceptionInfo)
        append("\n===== 日志结束 =====\n\n")
    }
}

/**
 * 保存崩溃日志到本地文件
 */
fun saveCrashLogToFile(logContent: String) {
    try {
        // 获取存储路径（优先使用应用内部存储，避免权限问题）
        val logDir = File(getStoragePath("Crash Log", false))
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        // 日志文件名（以时间命名）
        val fileName = "crash_${EN_YMDHMS.convert(currentTimeStamp)}.txt"
        val logFile = File(logDir, fileName)
        // 写入日志
        FileWriter(logFile, true).use { writer ->
            writer.write(logContent)
            writer.flush()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}