package com.example.common.utils.file.factory

import com.example.common.subscribe.BaseSubscribe
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * author: wyb
 * 下载单例
 */
class DownloadFactory private constructor() {
    private var job: Job? = null//kotlin协程

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory()
        }
    }

    fun download(downloadUrl: String, filePath: String, fileName: String, onDownloadListener: OnDownloadListener?) {
        FileUtil.deleteDir(filePath)
        job = GlobalScope.launch(Dispatchers.Main) {
            onDownloadListener?.onStart()
            val downloadBody = BaseSubscribe.download(downloadUrl)
            withContext(Dispatchers.IO) {
                var inputStream: InputStream? = null
                var fileOutputStream: FileOutputStream? = null
                try {
                    val file = File(FileUtil.isExistDir(filePath), fileName)
                    val buf = ByteArray(2048)
                    val total = downloadBody.contentLength()
                    inputStream = downloadBody.byteStream()
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
                    job?.cancel()
                }
            }
        }
    }

}