package com.example.common.utils.file

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Patterns
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.common.subscribe.CommonSubscribe
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.logWTF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * 工具类中，实现了对应文件流下载保存的方法，此处采用协程的方式引用
 */
class FileHelper(lifecycleOwner: LifecycleOwner) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = (Main)
    private var job: Job? = null

    init {
        lifecycleOwner.doOnDestroy { job?.cancel() }
    }

    /**
     * 存储图片协程
     */
    fun savePicJob(bitmap: Bitmap, root: String, fileName: String, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            savePic(bitmap, root, fileName, deleteDir, format, onResult)
        }
    }

    private suspend fun savePic(bitmap: Bitmap, root: String, fileName: String, deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, listener: (filePath: String?) -> Unit = {}) {
        listener(withContext(IO) { saveBit(bitmap, root, fileName, deleteDir, format) })
    }

    /**
     * 保存pdf文件存成图片形式
     * 指定页数
     */
    fun savePDFJob(file: File, index: Int = 0, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            savePDF(file, index, onResult)
        }
    }

    /**
     * 全部保存下来，返回集合
     */
    fun savePDFJob(file: File, onStart: () -> Unit = {}, onResult: (list: MutableList<String?>?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            val list: MutableList<String?>? = null
            val pageCount = withContext(IO) { PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).pageCount }
            for (index in 0 until pageCount) {
                savePDF(file, index) { list?.add(it) }
            }
            onResult.invoke(list)
        }
    }

    private suspend fun savePDF(file: File, index: Int = 0, listener: (filePath: String?) -> Unit = {}) {
        listener(withContext(IO) {
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
            saveBit(bitmap)
        })
    }

    /**
     * 构建图片
     */
    fun saveViewJob(view: View, width: Int = screenWidth, height: Int = screenHeight, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            saveView(view, width, height, onResult)
        }
    }

    private suspend fun saveView(view: View, width: Int = screenWidth, height: Int = screenHeight, listener: (bitmap: Bitmap?) -> Unit = {}) {
        view.loadLayout(width, height)
        try {
            listener(withContext(IO) { view.loadBitmap() })
        } catch (_: Exception) {
            listener(null)
        }
    }

    /**
     * @param folderPath 要打成压缩包文件的路径
     * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
     */
//    fun zipJob(folderPath: String, zipPath: String, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
//        job?.cancel()
//        job = launch {
//            onStart()
//            zip(folderPath, zipPath, onResult)
//        }
//    }
//
//    private suspend fun zip(folderPath: String, zipPath: String, listener: (filePath: String?) -> Unit = {}) {
//        try {
//            withContext(IO) { File(folderPath).let { if (it.exists()) zipFolder(it.absolutePath, File(zipPath).absolutePath) } }
//        } catch (e: Exception) {
//            "打包图片生成压缩文件异常: $e".logWTF
//        }
//        listener(zipPath.isMkdirs())
//    }
//
//    /**
//     * 将指定路径下的所有文件打成压缩包
//     * File fileDir = new File(rootDir + "/DCIM/Screenshots");
//     * File zipFile = new File(rootDir + "/" + taskId + ".zip");
//     *
//     * @param folderPath 要压缩的文件或文件夹路径
//     * @param zipPath 压缩完成的Zip路径
//     */
//    @Throws(Exception::class)
//    private fun zipFolder(folderPath: String, zipPath: String) {
//        //创建ZIP
//        val outZip = ZipOutputStream(FileOutputStream(zipPath))
//        //创建文件
//        val file = File(folderPath)
//        //压缩
//        zipFiles(file.parent + File.separator, file.name, outZip)
//        //完成和关闭
//        outZip.finish()
//        outZip.close()
//    }
//
//    @Throws(Exception::class)
//    private fun zipFiles(folderPath: String, fileName: String, zipOutputSteam: ZipOutputStream?) {
//        " \n压缩路径:$folderPath\n压缩文件名:$fileName".logWTF
//        if (zipOutputSteam == null) return
//        val file = File(folderPath + fileName)
//        if (file.isFile) {
//            val zipEntry = ZipEntry(fileName)
//            val inputStream = FileInputStream(file)
//            zipOutputSteam.putNextEntry(zipEntry)
//            var len: Int
//            val buffer = ByteArray(4096)
//            while (inputStream.read(buffer).also { len = it } != -1) {
//                zipOutputSteam.write(buffer, 0, len)
//            }
//            zipOutputSteam.closeEntry()
//        } else {
//            //文件夹
//            file.list().let {
//                //没有子文件和压缩
//                if (it.isNullOrEmpty()) {
//                    val zipEntry = ZipEntry(fileName + File.separator)
//                    zipOutputSteam.putNextEntry(zipEntry)
//                    zipOutputSteam.closeEntry()
//                } else {
//                    //子文件和递归
//                    for (i in it.indices) {
//                        zipFiles("$folderPath$fileName/", it[i], zipOutputSteam)
//                    }
//                }
//            }
//        }
//    }

    fun zipJob(folderPath: String, zipPath: String, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        zipJob(mutableListOf(folderPath), zipPath, onStart, onResult)
    }

    fun zipJob(folderList: MutableList<String>, zipPath: String, onStart: () -> Unit = {}, onResult: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch {
            onStart()
            zip(folderList, zipPath, onResult)
        }
    }

    private suspend fun zip(folderList: MutableList<String>, zipPath: String, listener: (filePath: String?) -> Unit = {}) {
        try {
            withContext(IO) { zipFolder(folderList, File(zipPath).absolutePath) }
        } catch (e: Exception) {
            "打包图片生成压缩文件异常: $e".logWTF
        }
        listener(zipPath.isMkdirs())
    }

    @Throws(Exception::class)
    private fun zipFolder(srcFileList: MutableList<String>, zipPath: String) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipPath))
        //批量打入压缩包
        for (folderPath in srcFileList) {
            val file = File(folderPath)
            val zipEntry = ZipEntry(file.name)
            val inputStream = FileInputStream(file)
            outZip.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                outZip.write(buffer, 0, len)
            }
            outZip.closeEntry()
        }
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    fun downloadJob(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
            R.string.link_invalid_error.shortToast()
            return
        }
        job?.cancel()
        job = launch {
            onStart()
            download(downloadUrl, filePath, fileName, onSuccess, onLoading, onFailed, onComplete)
        }
    }

    private suspend fun download(downloadUrl: String, filePath: String, fileName: String, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        //清除目录下的所有文件
        filePath.deleteDir()
        //创建一个安装的文件，开启io协程写入
        val file = File(filePath.isMkdirs(), fileName)
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
                var sum = 0L
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

}