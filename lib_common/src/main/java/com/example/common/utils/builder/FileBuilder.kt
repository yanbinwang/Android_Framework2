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
import com.example.common.utils.function.string
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.toSafeInt
import com.example.glide.ImageLoader
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
suspend fun suspendingSavePic(bitmap: Bitmap?, root: String = getStoragePath("保存图片"), fileName: String = EN_YMDHMS.convert(Date()), deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, quality: Int = 100): String? {
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
 */
suspend fun suspendingSaveView(view: View, width: Int = screenWidth, height: Int = WRAP_CONTENT): Bitmap? {
    return try {
        withContext(IO) {
            //对传入的高做一个修正，如果是自适应需要先做一次测绘
            val mHeight = if (height < 0) {
                view.measure(WRAP_CONTENT, WRAP_CONTENT)
                view.measuredHeight
            } else {
                height
            }
            view.loadLayout(width, mHeight)
            view.loadBitmap()
        }
    } catch (e: Exception) {
        throw e
    }
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
                val tempFile = File(getStoragePath("保存图片"), file.name.replace(".${suffix}", "_degree.${suffix}"))
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
 * 存储zip压缩包
 * @param folderPath 要打成压缩包文件的路径
 * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
 */
suspend fun suspendingZip(folderPath: String, zipPath: String): String {
    return suspendingZip(mutableListOf(folderPath), zipPath)
}

suspend fun suspendingZip(folderList: MutableList<String>, zipPath: String): String {
    zipPath.isMkdirs()
    withContext(IO) {
        zipFolder(folderList, zipPath)
    }
    return zipPath
}

private fun zipFolder(folderList: MutableList<String>, zipPath: String) {
    //创建ZIP
    ZipOutputStream(FileOutputStream(zipPath)).use { outZipStream ->
        //批量打入压缩包
        for (folderPath in folderList) {
            val file = File(folderPath)
            val zipEntry = ZipEntry(file.name)
            file.inputStream().use { inputStream ->
                outZipStream.putNextEntry(zipEntry)
                var len: Int
                val buffer = ByteArray(4096)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outZipStream.write(buffer, 0, len)
                }
                outZipStream.closeEntry()
            }
        }
        //完成和关闭
        outZipStream.finish()
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
            //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载(加try/catch为双保险，万一地址不正确应用就会闪退)
            val body = CommonApi.downloadInstance.getDownloadApi(downloadUrl)
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
                        withContext(Main) {
                            listener.invoke(progress)
                        }
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
suspend fun suspendingDownloadPic(mContext: Context, string: String, root: String = getStoragePath("保存图片"), deleteDir: Boolean = false): String {
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
 * 文件分片
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