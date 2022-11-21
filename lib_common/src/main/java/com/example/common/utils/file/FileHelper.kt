package com.example.common.utils.file

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Patterns
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.example.base.utils.function.doOnDestroy
import com.example.base.utils.function.value.DateFormat.EN_YMDHMS
import com.example.base.utils.function.value.getDateTime
import com.example.base.utils.logE
import com.example.common.constant.Constants
import com.example.common.subscribe.CommonSubscribe
import com.example.common.utils.builder.shortToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * 工具类中，实现了对应文件流下载保存的方法，此处采用协程的方式引用
 */
class FileHelper(lifecycleOwner: LifecycleOwner?) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = (Main)
    private var job: Job? = null

    init {
        lifecycleOwner?.doOnDestroy { job?.cancel() }
    }

    /**
     * 存储图片协程
     */
    fun compressBitJob(bitmap: Bitmap, root: String, fileName: String, delete: Boolean = false, formatJpg: Boolean = true, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch { compressBit(bitmap, root, fileName, delete, formatJpg, onComplete) }
    }

    suspend fun compressBit(bitmap: Bitmap, root: String, fileName: String, delete: Boolean = false, formatJpg: Boolean = true, onComplete: (filePath: String?) -> Unit = {}) {
        val absolutePath = "${root}/${fileName}${if (formatJpg) ".jpg" else ".png"}"
        val result = withContext(IO) { FileUtil.compressBit(bitmap, root, fileName, delete, formatJpg) }
        onComplete(if (result) absolutePath else null)
    }

    /**
     * 保存pdf文件存成图片形式
     */
    fun compressPDFBitJob(file: File, index: Int = 0, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch { compressPDFBit(file, index, onComplete) }
    }

    suspend fun compressPDFBit(file: File, index: Int = 0, onComplete: (filePath: String?) -> Unit = {}) {
        val root = "${Constants.APPLICATION_FILE_PATH}/图片"
        val fileName = EN_YMDHMS.getDateTime(Date())
        val absolutePath = "${root}/${fileName}.jpg"
        val result = withContext(IO) {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            val page = renderer.openPage(index)//选择渲染哪一页的渲染数据
            val width = page.width
            val height = page.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val rent = Rect(0, 0, width, height)
            page.render(bitmap, rent, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            FileUtil.compressBit(bitmap, root, fileName)
        }
        onComplete(if (result) absolutePath else null)
    }

    /**
     * @param folderPath 要打成压缩包文件的路径
     * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
     */
    fun zipFolderJob(folderPath: String, zipPath: String, onStart: () -> Unit? = {}, onComplete: (filePath: String?) -> Unit? = {}) {
        job?.cancel()
        job = launch { zipFolder(folderPath, zipPath, onStart, onComplete) }
    }

    suspend fun zipFolder(folderPath: String, zipPath: String, onStart: () -> Unit? = {}, onComplete: (filePath: String?) -> Unit? = {}) {
        var result = true
        try {
            onStart()
            withContext(IO) {
                val fileDir = File(folderPath)
                if (fileDir.exists()) FileUtil.zipFolder(fileDir.absolutePath, File(zipPath).absolutePath)
            }
        } catch (e: Exception) {
            result = false
            "打包图片生成压缩文件异常: $e".logE("FileHelper")
        } finally {
            onComplete(if (result) zipPath else null)
        }
    }

    fun downloadJob(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch { download(downloadUrl, filePath, fileName, onStart, onSuccess, onLoading, onFailed, onComplete) }
    }

    suspend fun download(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
            "链接地址不合法".shortToast()
            return
        }
        onStart()
        //清除目录下的所有文件
        filePath.deleteDir()
        //创建一个安装的文件，开启io协程写入
        val file = File(filePath.isExistDir(), fileName)
        withContext(IO) {
            var inputStream: InputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
                val body = CommonSubscribe.getDownloadApi(downloadUrl)
                val buf = ByteArray(2048)
                val total = body.contentLength()
                inputStream = body.byteStream()
                fileOutputStream = FileOutputStream(file)
                var len: Int
                var sum: Long = 0
                while (((inputStream.read(buf)).also { len = it }) != -1) {
                    fileOutputStream.write(buf, 0, len)
                    sum += len.toLong()
                    val progress = (sum * 1.0f / total * 100).toInt()
                    withContext(Main) { onLoading(progress) }
                }
                fileOutputStream.flush()
                withContext(Main) { onSuccess(file.path) }
            } catch (e: Exception) {
                withContext(Main) { onFailed(e) }
            } finally {
                inputStream?.close()
                fileOutputStream?.close()
                withContext(Main) { onComplete() }
            }
        }
    }

    /**
     * 构建图片
     */
    fun createJob(view: View, width: Int = Constants.SCREEN_WIDTH, height: Int = Constants.SCREEN_HEIGHT, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch { create(view, width, height, onStart, onResult, onComplete) }
    }

    suspend fun create(view: View, width: Int = Constants.SCREEN_WIDTH, height: Int = Constants.SCREEN_HEIGHT, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}, onComplete: () -> Unit = {}) {
        onStart()
        view.loadLayout(width, height)
        try {
            onResult(withContext(IO) { view.loadBitmap() })
        } catch (_: Exception) {
        }
        onComplete()
    }

    /**
     * 当measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
     * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小
     */
    private fun View.loadLayout(width: Int, height: Int) {
        //整个View的大小 参数是左上角 和右下角的坐标
        layout(0, 0, width, height)
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_WIDTH, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_HEIGHT, View.MeasureSpec.EXACTLY)
        measure(measuredWidth, measuredHeight)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    //如果不设置canvas画布为白色，则生成透明
    private fun View.loadBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        layout(0, 0, width, height)
        draw(canvas)
        return bitmap
    }

}