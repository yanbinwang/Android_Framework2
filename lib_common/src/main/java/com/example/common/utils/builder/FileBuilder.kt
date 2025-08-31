package com.example.common.utils.builder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.CompressFormat.PNG
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
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
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
import com.example.common.utils.function.read
import com.example.common.utils.function.split
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.toSafeInt
import com.example.glide.ImageLoader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
            //存储目录文件
            val storeDir = File(root)
            //先判断是否需要清空目录，再判断是否存在（不存在则创建）
            if (deleteDir) root.deleteDir()
            root.isMkdirs()
            //根据要保存的格式，返回对应后缀名->安卓只支持以下三种
            val suffix = when (format) {
                JPEG -> "jpg"
                PNG -> "png"
                else -> "webp"
            }
            //在目录文件夹下生成一个新的图片
            val file = File(storeDir, "${fileName}.${suffix}")
            //开流开始写入
            file.outputStream().use { outputStream ->
                //如果是Bitmap.CompressFormat.PNG，无论quality为何值，压缩后图片文件大小都不会变化
                bitmap.compress(format, if (format != PNG) quality else 100, outputStream)
                outputStream.flush()
                bitmap.recycle()
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
    //选择渲染哪一页的渲染数据
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
suspend fun suspendingSaveView(view: View, width: Int = screenWidth, height: Int = WRAP_CONTENT): Bitmap? {
//    return try {
//        // Android 中 View 的 measure()/layout()/draw() 必须在主线程执行，在 IO 线程调用会导致异常
//        withContext(Main.immediate) {
//            // 对传入的高做一个修正，如果是自适应需要先做一次测绘
//            val mHeight = if (height < 0) {
//                view.measure(WRAP_CONTENT, WRAP_CONTENT)
//                view.measuredHeight
//            } else {
//                height
//            }
//            // 强制 View 完成测量与布局
//            view.loadLayout(width, mHeight)
//            // 绘制 View 到 Bitmap
//            view.loadBitmap()
//        }
//    } catch (e: Exception) {
//        throw e
//    }
    // 用 CompletableDeferred 实现“主线程异步操作+协程等待”
    val deferred = CompletableDeferred<Bitmap?>()
    // 先让弹窗正常显示（此时主线程先处理弹窗消息）
    // 用 view.post {} 把 View 操作推迟到主线程空闲时执行 (view.post强制将 View 操作切换到主线程执行，不受外层切 IO 线程影响)
    view.post {
        try {
            // 这里的 View 操作仍在主线程，但已在弹窗显示之后执行
            // 对传入的高做一个修正，如果是自适应需要先做一次测绘
            val mHeight = if (height < 0) {
                view.measure(WRAP_CONTENT, WRAP_CONTENT)
                view.measuredHeight
            } else {
                height
            }
            // 强制 View 完成测量与布局
            view.loadLayout(width, mHeight)
            // 绘制 View 到 Bitmap
            val bitmap = view.loadBitmap()
            // 操作完成，通知协程返回结果
            deferred.complete(bitmap)
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
        }
    }
    // 协程挂起等待，直到 View 操作完成（不阻塞主线程）
    return deferred.await()
}

/**
 * 旋转图片
 * 修整部分图片方向不正常
 * 取得一个新的图片文件
 */
suspend fun suspendingDegree(file: File, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, quality: Int = 100): File? {
    return try {
        withContext(IO) {
            var mFile = file
            if (readDegree(file.absolutePath) != 0f) {
                var bitmap: Bitmap
                val matrix = Matrix()
                BitmapFactory.decodeFile(file.absolutePath).let {
                    bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                    it.recycle()
                }
                //根据格式，返回对应后缀名->安卓只支持以下三种
                val suffix = when (format) {
                    JPEG -> "jpg"
                    PNG -> "png"
                    else -> "webp"
                }
                val tempFile = File(getStoragePath("Save Image"), file.name.replace(".${suffix}", "_degree.${suffix}"))
                if (tempFile.exists()) tempFile.delete()
                tempFile.outputStream().use { outputStream ->
                    //如果是Bitmap.CompressFormat.PNG，无论quality为何值，压缩后图片文件大小都不会变化
                    bitmap.compress(format, if (format != PNG) quality else 100, outputStream)
                    if (deleteDir) file.delete()
                    mFile = tempFile
                }
                bitmap.recycle()
            }
            mFile
        }
    } catch (e: Exception) {
        throw e
    }
}

/**
 * 读取图片的方向
 * 部分手机拍摄需要设置手机屏幕screenOrientation
 * 不然会读取为0
 */
private fun readDegree(path: String): Float {
    var degree = 0f
    var exifInterface: ExifInterface? = null
    try {
        exifInterface = ExifInterface(path)
    } catch (_: IOException) {
    }
    when (exifInterface?.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)) {
        ORIENTATION_ROTATE_90 -> degree = 90f
        ORIENTATION_ROTATE_180 -> degree = 180f
        ORIENTATION_ROTATE_270 -> degree = 270f
    }
    return degree
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
    //清除目录下的所有文件
    filePath.deleteDir()
    //创建一个安装的文件，开启io协程写入
    val file = File(filePath.isMkdirs(), fileName)
    return withContext(IO) {
        try {
            //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
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
        //存储目录文件
        val storeDir = File(root)
        //先判断是否需要清空目录，再判断是否存在（不存在则创建）
        if (deleteDir) root.deleteDir()
        root.isMkdirs()
        suspendingGlideDownload(mContext, string, storeDir)
    }
}

private suspend fun suspendingGlideDownload(mContext: Context, string: String, storeDir: File) = suspendCancellableCoroutine {
    ImageLoader.instance.downloadImage(mContext, string) { file ->
        //此处`file?.name`会包含glide下载图片的后缀（png,jpg,webp等）
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