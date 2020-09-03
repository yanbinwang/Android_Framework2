package com.example.common.utils.file.factory

import com.example.common.subscribe.BaseSubscribe
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * author: wyb
 * 下载单例
 */
class DownloadFactory private constructor() : CoroutineScope {

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    fun download(downloadUrl: String, filePath: String, fileName: String, onDownloadListener: OnDownloadListener?) {
        launch(Dispatchers.Main) {
            FileUtil.deleteDir(filePath)
            withContext(Dispatchers.IO) { startDownload(BaseSubscribe.download(downloadUrl), File(FileUtil.isExistDir(filePath), fileName), onDownloadListener) }
        }
    }

    private suspend fun startDownload(body: okhttp3.ResponseBody, file: File, onDownloadListener: OnDownloadListener?) {
        withContext(Dispatchers.Main) { onDownloadListener?.onStart() }
        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
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
                withContext(Dispatchers.Main) { onDownloadListener?.onLoading(progress) }
            }
            fileOutputStream.flush()
            withContext(Dispatchers.Main) { onDownloadListener?.onSuccess(file.path) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onDownloadListener?.onFailed(e) }
        } finally {
            inputStream?.close()
            fileOutputStream?.close()
            withContext(Dispatchers.Main) { onDownloadListener?.onComplete() }
            cancel()
        }
    }

}