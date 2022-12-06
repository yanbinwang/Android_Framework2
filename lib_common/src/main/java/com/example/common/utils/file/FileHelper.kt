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
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.logE
import com.example.base.utils.logWTF
import com.example.common.constant.Constants
import com.example.common.subscribe.Subscribe
import com.example.common.utils.builder.shortToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
    fun saveJob(bitmap: Bitmap, root: String, fileName: String, delete: Boolean = false, formatJpg: Boolean = true, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch { save(bitmap, root, fileName, delete, formatJpg, onComplete) }
    }

    /**
     * 保存pdf文件存成图片形式
     */
    fun saveJob(file: File, index: Int = 0, onComplete: (filePath: String?) -> Unit = {}) {
        job?.cancel()
        job = launch { save(file, index, onComplete) }
    }

    /**
     * 构建图片
     */
    fun saveJob(view: View, width: Int = Constants.SCREEN_WIDTH, height: Int = Constants.SCREEN_HEIGHT, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch { save(view, width, height, onStart, onResult, onComplete) }
    }

    /**
     * @param folderPath 要打成压缩包文件的路径
     * @param zipPath 压缩完成的Zip路径（包含压缩文件名）-"${Constants.SDCARD_PATH}/10086.zip"
     */
    fun zipJob(folderPath: String, zipPath: String, onStart: () -> Unit? = {}, onComplete: (filePath: String?) -> Unit? = {}) {
        job?.cancel()
        job = launch { zip(folderPath, zipPath, onStart, onComplete) }
    }

    fun downloadJob(downloadUrl: String, filePath: String, fileName: String, onStart: () -> Unit = {}, onSuccess: (path: String) -> Unit = {}, onLoading: (progress: Int) -> Unit = {}, onFailed: (e: Exception?) -> Unit = {}, onComplete: () -> Unit = {}) {
        job?.cancel()
        job = launch { download(downloadUrl, filePath, fileName, onStart, onSuccess, onLoading, onFailed, onComplete) }
    }

    suspend fun save(bitmap: Bitmap, root: String, fileName: String, delete: Boolean = false, formatJpg: Boolean = true, onComplete: (filePath: String?) -> Unit = {}) {
        onComplete(withContext(IO) { saveBitmap(bitmap, root, fileName, delete, formatJpg) })
    }

    suspend fun save(file: File, index: Int = 0, onComplete: (filePath: String?) -> Unit = {}) {
        val root = "${Constants.APPLICATION_FILE_PATH}/图片"
        val fileName = EN_YMDHMS.getDateTime(Date())
        onComplete(withContext(IO) {
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
            saveBitmap(bitmap, root, fileName)
        })
    }

    suspend fun save(view: View, width: Int = Constants.SCREEN_WIDTH, height: Int = Constants.SCREEN_HEIGHT, onStart: () -> Unit = {}, onResult: (bitmap: Bitmap?) -> Unit = {}, onComplete: () -> Unit = {}) {
        onStart()
        view.loadLayout(width, height)
        try {
            onResult(withContext(IO) { view.loadBitmap() })
        } catch (_: Exception) {
        }
        onComplete()
    }

    suspend fun zip(folderPath: String, zipPath: String, onStart: () -> Unit? = {}, onComplete: (filePath: String?) -> Unit? = {}) {
        var result = true
        try {
            onStart()
            withContext(IO) { File(folderPath).apply { if (exists()) zipFolder(absolutePath, File(zipPath).absolutePath) } }
        } catch (e: Exception) {
            result = false
            "打包图片生成压缩文件异常: $e".logE("FileHelper")
        }
        onComplete(if (result) zipPath else null)
    }

    /**
     * 将指定路径下的所有文件打成压缩包
     * File fileDir = new File(rootDir + "/DCIM/Screenshots");
     * File zipFile = new File(rootDir + "/" + taskId + ".zip");
     *
     * @param srcFilePath 要压缩的文件或文件夹路径
     * @param zipFilePath 压缩完成的Zip路径
     */
    @Throws(Exception::class)
    private fun zipFolder(srcFilePath: String, zipFilePath: String) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFilePath))
        //创建文件
        val file = File(srcFilePath)
        //压缩
        zipFiles(file.parent + File.separator, file.name, outZip)
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    @Throws(Exception::class)
    private fun zipFiles(folderPath: String, fileName: String, zipOutputSteam: ZipOutputStream?) {
        " \n压缩路径:$folderPath\n压缩文件名:$fileName".logWTF
        if (zipOutputSteam == null) return
        val file = File(folderPath + fileName)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileName)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            val fileList = file.list()
            //没有子文件和压缩
            if (fileList.isNullOrEmpty()) {
                val zipEntry = ZipEntry(fileName + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            if (!fileList.isNullOrEmpty()) {
                //子文件和递归
                for (i in fileList.indices) {
                    zipFiles("$folderPath$fileName/", fileList[i], zipOutputSteam)
                }
            }
        }
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
                val body = Subscribe.getDownloadApi(downloadUrl)
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

/**
 *  Created by wangyanbin
 *  文件管理工具（切片，合并）
 */
object DocumentHelper {

    /**
     * 开始创建并写入tmp文件
     * @param filePath  分割文件地址
     * @param fileSize 分割文件大小
     */
    class TmpInfo(var filePath: String? = null, var fileSize: Long = 0)

    /**
     * 文件分割
     *
     * @param targetFile 分割的文件
     * @param cutSize    分割文件的大小
     */
    @JvmStatic
    fun split(targetFile: File, cutSize: Long): MutableList<String> {
        val splitList = ArrayList<String>()
        try {
            //计算需要分割的文件总数
            val targetLength = targetFile.length()
            val count = if (targetLength.mod(cutSize) == 0L) targetLength.div(cutSize).toInt() else targetLength.div(cutSize).plus(1).toInt()
            //获取目标文件,预分配文件所占的空间,在磁盘中创建一个指定大小的文件(r:只读)
            val accessFile = RandomAccessFile(targetFile, "r")
            //文件的总大小
            val length = accessFile.length()
            //文件切片后每片的最大大小
            val maxSize = length / count
            //初始化偏移量
            var offSet = 0L
            //开始切片
            for (i in 0 until count - 1) {
                val begin = offSet
                val end = (i + 1) * maxSize
                val tmpInfo = getWrite(targetFile.absolutePath, i, begin, end)
                offSet = tmpInfo.fileSize
                splitList.add(tmpInfo.filePath ?: "")
            }
            if (length - offSet > 0) splitList.add(getWrite(targetFile.absolutePath, count - 1, offSet, length).filePath ?: "")
            accessFile.close()
        } catch (_: Exception) {
        } finally {
            //确保返回的集合中不包含空路径
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
     */
    @JvmStatic
    private fun getWrite(filePath: String, index: Int, begin: Long, end: Long): TmpInfo {
        val info = TmpInfo()
        try {
            //源文件
            val file = File(filePath)
            //申明文件切割后的文件磁盘
            val inAccessFile = RandomAccessFile(file, "r")
            //定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            val tmpFile = File("${file.parent}/${file.name.split(".")[0]}_${index}.tmp")
            //如果不存在，则创建一个或继续写入
            val outAccessFile = RandomAccessFile(tmpFile, "rw")
            //申明具体每一文件的字节数组
            val b = ByteArray(1024)
            var n: Int
            //从指定位置读取文件字节流
            inAccessFile.seek(begin)
            //判断文件流读取的边界，从指定每一份文件的范围，写入不同的文件
            while (inAccessFile.read(b).also { n = it } != -1 && inAccessFile.filePointer <= end) {
                outAccessFile.write(b, 0, n)
            }
            //关闭输入输出流,赋值
            info.fileSize = inAccessFile.filePointer
            inAccessFile.close()
            outAccessFile.close()
            info.filePath = tmpFile.absolutePath
        } catch (_: Exception) {
        }
        return info
    }

    @JvmStatic
    fun getSignature(southPath: String) = getSignature(File(southPath))

    /**
     * 获取文件哈希值
     * 满足64位哈希，不足则前位补0
     */
    @JvmStatic
    fun getSignature(file: File): String {
        var hash = ""
        try {
            val inputStream = FileInputStream(file)
            val digest = MessageDigest.getInstance("SHA-256")
            val array = ByteArray(1024)
            var len: Int
            while (inputStream.read(array, 0, 1024).also { len = it } != -1) {
                digest.update(array, 0, len)
            }
            inputStream.close()
            val bigInt = BigInteger(1, digest.digest())
            hash = bigInt.toString(16)
            if (hash.length < 64) {
                for (i in 0 until 64 - hash.length) {
                    hash = "0$hash"
                }
            }
        } catch (_: Exception) {
        }
        return hash
    }

}