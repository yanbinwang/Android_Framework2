package com.example.common.utils.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Patterns
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.common.subscribe.CommonSubscribe
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.loadBitmap
import com.example.common.utils.function.loadLayout
import com.example.common.utils.function.saveBit
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import com.example.glide.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * 工具类中，实现了对应文件流下载保存的方法，此处采用协程的方式引用
 */
class FileBuilder(observer: LifecycleOwner) : CoroutineScope {
    private var picJob: Job? = null
    private var pdfJob: Job? = null
    private var viewJob: Job? = null
    private var zipJob: Job? = null
    private var downloadJob: Job? = null
    private var downloadPicJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Main + job

    companion object {
        /**
         * 存储图片
         */
        suspend fun suspendingSavePic(bitmap: Bitmap, root: String, fileName: String, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG): String? {
            return withContext(IO) { saveBit(bitmap, root, fileName, deleteDir, format) }
        }

        /**
         * 存储pdf
         */
        suspend fun suspendingSavePDF(file: File, index: Int = 0): String? {
//            return withContext(IO) {
//                var filePath: String? = null
//                var renderer: PdfRenderer? = null
//                var page: PdfRenderer.Page? = null
//                try {
//                    renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
//                    page = renderer.openPage(index)//选择渲染哪一页的渲染数据
//                    val width = page.width
//                    val height = page.height
//                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                    val canvas = Canvas(bitmap)
//                    canvas.drawColor(Color.WHITE)
//                    canvas.drawBitmap(bitmap, 0f, 0f, null)
//                    val rent = Rect(0, 0, width, height)
//                    page.render(bitmap, rent, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                    filePath = saveBit(bitmap)
//                    bitmap.recycle()
//                } catch (_: Exception) {
//                } finally {
//                    try {
//                        page?.close()
//                    } catch (_: Exception) {
//                    }
//                    try {
//                        renderer?.close()
//                    } catch (_: Exception) {
//                    }
//                }
//                filePath
//            }
            return withContext(IO) {
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                    //选择渲染哪一页的渲染数据
                    renderer.openPage(index).use { page ->
                        val width = page.width
                        val height = page.height
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        val rent = Rect(0, 0, width, height)
                        page.render(bitmap, rent, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        saveBit(bitmap).apply { bitmap.recycle() }
                    }
                }
            }
        }

        /**
         * 存储绘制的view
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
            } catch (_: Exception) {
                null
            }
        }

        /**
         * 存储zip压缩包
         */
        suspend fun suspendingZip(folderList: MutableList<String>, zipPath: String): String? {
            return try {
                withContext(IO) {
                    zipPath.isMkdirs()
                    zipFolder(folderList, zipPath)
                }
                zipPath
            } catch (e: Exception) {
                "打包图片生成压缩文件异常: $e".logWTF
                null
            }
        }

        private fun zipFolder(folderList: MutableList<String>, zipPath: String) {
            //创建ZIP
            var outZipStream: ZipOutputStream? = null
            try {
                outZipStream = ZipOutputStream(FileOutputStream(zipPath))
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
            } catch (_: Exception) {
            } finally {
                try {
                    outZipStream?.close()
                } catch (_: Exception) {
                }
            }
        }

        /**
         * 存储文件
         */
        suspend fun suspendingDownload(downloadUrl: String, filePath: String, fileName: String, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
//            //清除目录下的所有文件
//            filePath.deleteDir()
//            //创建一个安装的文件，开启io协程写入
//            val file = File(filePath.isMkdirs(), fileName)
//            withContext(IO) {
//                var inputStream: InputStream? = null
//                var outputStream: FileOutputStream? = null
//                try {
//                    //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
//                    val body = CommonSubscribe.getDownloadApi(downloadUrl)
//                    val buf = ByteArray(2048)
//                    val total = body.contentLength()
//                    inputStream = body.byteStream()
//                    outputStream = FileOutputStream(file)
//                    var len: Int
//                    var sum = 0L
//                    while (((inputStream.read(buf)).also { len = it }) != -1) {
//                        outputStream.write(buf, 0, len)
//                        sum += len.toLong()
//                        val progress = (sum * 1.0f / total * 100).toSafeInt()
//                        withContext(Main) { onLoading(progress) }
//                    }
//                    outputStream.flush()
//                    withContext(Main) { onSuccess(file.path) }
//                } catch (e: Exception) {
//                    withContext(Main) { onFailed(e) }
//                } finally {
//                    try {
//                        inputStream?.close()
//                    } catch (_: Exception) {
//                    }
//                    try {
//                        outputStream?.close()
//                    } catch (_: Exception) {
//                    }
//                    withContext(Main) { onComplete() }
//                }
//            }
            //清除目录下的所有文件
            filePath.deleteDir()
            //创建一个安装的文件，开启io协程写入
            val file = File(filePath.isMkdirs(), fileName)
            withContext(IO) {
                try {
                    //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
                    val body = CommonSubscribe.getDownloadApi(downloadUrl)
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
                                withContext(Main) { onLoading.invoke(progress) }
                            }
                            outputStream.flush()
                            withContext(Main) { onSuccess.invoke(file.path) }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Main) { onFailed.invoke(e) }
                } finally {
                    withContext(Main) { onComplete.invoke() }
                }
            }
        }

        suspend fun suspendingDownload(downloadUrl: String, filePath: String, fileName: String, listener: (progress: Int) -> Unit = {}): String? {
            //清除目录下的所有文件
            filePath.deleteDir()
            //创建一个安装的文件，开启io协程写入
            val file = File(filePath.isMkdirs(), fileName)
            return withContext(IO) {
                try {
                    //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
                    val body = CommonSubscribe.getDownloadApi(downloadUrl)
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
         * 存储网络路径图片
         */
        suspend fun suspendingDownloadPic(mContext: Context, string: String, storeDir: File): String {
//            return withContext(IO) {
//                var file: File? = null
//                var filePath: String? = null
//                ImageLoader.instance.download(mContext, string) {
//                    file = it
//                    filePath = "${storeDir.absolutePath}/${it?.name}"//此处`it?.name`会包含glide下载图片的后缀（png,jpg,webp等）
//                }
//                file?.copy(storeDir)
//                file?.delete()
//                filePath
//            }
            return withContext(IO) { suspendingGlideDownload(mContext, string, storeDir) }
        }

        private suspend fun suspendingGlideDownload(mContext: Context, string: String, storeDir: File) = suspendCancellableCoroutine {
            ImageLoader.instance.download(mContext, string) { file ->
                //此处`file?.name`会包含glide下载图片的后缀（png,jpg,webp等）
                it.resume("${storeDir.absolutePath}/${file?.name}".apply {
                    file?.copy(storeDir)
                    file?.delete()
                })
            }
        }

        /**
         * 读取文件
         */
        suspend fun suspendingRead(sourcePath: String?): String {
            sourcePath ?: return ""
            return withContext(IO) { File(sourcePath).read() }
        }

        /**
         * 复制文件(将当前文件拷贝一份到目标路径)
         */
        suspend fun suspendingCopy(sourcePath: String?, destPath: String?) {
            if (sourcePath == null || destPath == null) return
            withContext(IO) { File(sourcePath).copy(File(destPath)) }
        }

        /**
         * 获取文件采用base64形式
         */
        suspend fun suspendingBase64(sourcePath: String?): String {
            sourcePath ?: return ""
            return withContext(IO) { File(sourcePath).getBase64() }
        }

        /**
         * 获取文件hash值
         */
        suspend fun suspendingHash(sourcePath: String?): String {
            sourcePath ?: return ""
            return withContext(IO) { File(sourcePath).getHash() }
        }

    }

    init {
        observer.doOnDestroy {
            picJob?.cancel()
            downloadPicJob?.cancel()
            pdfJob?.cancel()
            viewJob?.cancel()
            zipJob?.cancel()
            downloadJob?.cancel()
            job.cancel()
        }
    }

    /**
     * 存储图片协程
     */
    fun savePicJob(bitmap: Bitmap, root: String, fileName: String, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        onStart()
        picJob?.cancel()
        picJob = launch {
            val filePath = suspendingSavePic(bitmap, root, fileName, deleteDir, format)
            onResult.invoke(filePath)
        }
    }

    /**
     * 保存pdf文件存成图片形式
     * 指定页数
     */
    fun savePDFJob(file: File, index: Int = 0, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        onStart()
        pdfJob?.cancel()
        pdfJob = launch {
            val filePath = suspendingSavePDF(file, index)
            onResult.invoke(filePath)
        }
    }

    /**
     * 全部保存下来，返回集合
     */
    fun savePDFJob(file: File, onStart: () -> Unit = {}, onResult: (list: MutableList<String?>?) -> Unit = {}) {
        onStart()
        pdfJob?.cancel()
        pdfJob = launch {
            val list = ArrayList<String?>()
            val pageCount = withContext(IO) { PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).pageCount }
            for (index in 0 until pageCount) {
                val filePath = suspendingSavePDF(file, index)
                list.add(filePath)
            }
            onResult.invoke(list)
        }
    }

    /**
     * 构建图片
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
    fun saveViewJob(view: View, width: Int = screenWidth, height: Int = WRAP_CONTENT, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        onStart()
        viewJob?.cancel()
        viewJob = launch {
            val filePath = withContext(IO) { saveBit(suspendingSaveView(view, width, height)) }
            onResult.invoke(filePath)
        }
    }

    /**
     * @param folderPath 要打成压缩包文件的路径
     * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
     */
    fun zipJob(folderPath: String, zipPath: String, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        zipJob(mutableListOf(folderPath), zipPath, onStart, onResult)
    }

    fun zipJob(folderList: MutableList<String>, zipPath: String, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        onStart()
        zipJob?.cancel()
        zipJob = launch {
            val filePath = suspendingZip(folderList, zipPath)
            onResult.invoke(filePath)
        }
    }

    fun downloadJob(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String?) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
//        if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
//            R.string.linkError.shortToast()
//            return
//        }
//        onStart()
//        downloadJob?.cancel()
//        downloadJob = launch {
//            suspendingDownload(downloadUrl, filePath, fileName, onSuccess, onLoading, onFailed, onComplete)
//        }
        if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
            R.string.linkError.shortToast()
            return
        }
        onStart()
        downloadJob?.cancel()
        downloadJob = launch {
            try {
                val downloadFilePath = suspendingDownload(downloadUrl, filePath, fileName, onLoading)
                onSuccess(downloadFilePath)
            } catch (e: Exception) {
                onFailed.invoke(e)
            } finally {
                onComplete()
            }
        }
    }

    /**
     * 存储图片协程(下载url)
     */
    fun downloadPicJob(mContext: Context, string: String, root: String = getStoragePath("保存图片"), deleteDir: Boolean = false, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        onStart()
        downloadPicJob?.cancel()
        downloadPicJob = launch {
            //存储目录文件
            val storeDir = File(root)
            //先判断是否需要清空目录，再判断是否存在（不存在则创建）
            if (deleteDir) root.deleteDir()
            root.isMkdirs()
            //下载的文件从缓存目录拷贝到指定目录
            val filePath = suspendingDownloadPic(mContext, string, storeDir)
            onResult.invoke(filePath)
        }
    }

}