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
import com.example.base.utils.LogUtil
import com.example.base.utils.ToastUtil
import com.example.base.utils.function.doOnDestroy
import com.example.base.utils.function.value.DateFormat.EN_YMDHMS
import com.example.base.utils.function.value.getDateTime
import com.example.common.BaseApplication
import com.example.common.constant.Constants
import com.example.common.subscribe.CommonSubscribe
import kotlinx.coroutines.*
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
        get() = (Dispatchers.IO)
    private var job: Job? = null

    init {
        lifecycleOwner?.doOnDestroy { job?.cancel() }
    }

    /**
     * 存储图片协程
     */
    @JvmOverloads
    fun saveBitmap(bitmap: Bitmap, root: String, fileName: String, formatJpg: Boolean = true, clear: Boolean = false, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            val absolutePath = "${root}/${fileName}${if (formatJpg) ".jpg" else ".png"}"
            val result = FileUtil.saveBitmap(
                bitmap = bitmap,
                root = root,
                fileName = fileName,
                formatJpg = formatJpg,
                clear = clear)
            //切回主线程返回路径
            withContext(Dispatchers.Main) { onComplete(if (result) absolutePath else null) }
        }
    }

    /**
     * 保存pdf文件存成图片形式
     */
    @JvmOverloads
    fun savePdfBitmap(file: File, index: Int = 0, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch {
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
            val root = "${Constants.APPLICATION_FILE_PATH}/图片"
            val fileName = EN_YMDHMS.getDateTime(Date())
            val absolutePath = "${root}/${fileName}.jpg"
            val result = FileUtil.saveBitmap(bitmap = bitmap, root = root, fileName = fileName)
            //切回主线程返回路径
            withContext(Dispatchers.Main) { onComplete(if (result) absolutePath else null) }
        }
    }

    /**
     * @param folderPath 要打成压缩包文件的路径
     * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
     */
    @JvmOverloads
    fun zipFolder(folderPath: String, zipPath: String, onStart: () -> Unit? = {}, onComplete: (filePath: String?) -> Unit? = {}) {
        job?.cancel()
        job = launch {
            var result = true
            try {
                withContext(Dispatchers.Main) { onStart.invoke() }
                val fileDir = File(folderPath)
                if (fileDir.exists()) FileUtil.zipFolder(fileDir.absolutePath, File(zipPath).absolutePath)
            } catch (e: Exception) {
                result = false
                LogUtil.e("FileHelper", "打包图片生成压缩文件异常: $e")
            } finally {
                withContext(Dispatchers.Main) { onComplete(if (result) zipPath else null) }
            }
        }
    }

    fun download(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch {
            if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
                ToastUtil.mackToastSHORT("链接地址不合法", BaseApplication.instance?.applicationContext!!)
                return@launch
            }
            withContext(Dispatchers.Main) { onStart() }
            //清除目录下的所有文件
            FileUtil.deleteDir(filePath)
            var inputStream: InputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
                val body = CommonSubscribe.getDownloadApi(downloadUrl)
                //在上一步协程成功并拿到对象后开始执行，创建一个安装的文件，开启io协程，写入
                val file = File(FileUtil.isExistDir(filePath), fileName)
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
                    withContext(Dispatchers.Main) { onLoading(progress) }
                }
                fileOutputStream.flush()
                withContext(Dispatchers.Main) { onSuccess(file.path) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onFailed(e) }
            } finally {
                inputStream?.close()
                fileOutputStream?.close()
                withContext(Dispatchers.Main) { onComplete() }
            }
        }
    }

    /**
     * 构建图片
     */
    fun create(view: View, width: Int = Constants.SCREEN_WIDTH, height: Int = Constants.SCREEN_HEIGHT, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            loadLayout(view, width, height)
            try {
                onResult(withContext(Dispatchers.IO) { loadBitmap(view) })
            } catch (_: Exception) {
            } finally {
                onComplete()
            }
        }
    }

    /**
     * 当measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
     * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小
     */
    private fun loadLayout(view: View, width: Int, height: Int) {
        //整个View的大小 参数是左上角 和右下角的坐标
        view.layout(0, 0, width, height)
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_WIDTH, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_HEIGHT, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    //如果不设置canvas画布为白色，则生成透明
    private fun loadBitmap(view: View): Bitmap? {
        val width = view.width
        val height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.layout(0, 0, width, height)
        view.draw(canvas)
        return bitmap
    }

}