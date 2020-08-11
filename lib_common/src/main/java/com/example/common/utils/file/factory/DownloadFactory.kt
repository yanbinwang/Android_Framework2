package com.example.common.utils.file.factory

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.common.subscribe.BaseSubscribe.download
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import com.example.common.utils.handler.WeakHandler
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.Executors

/**
 * author: wyb
 * 下载单例
 */
class DownloadFactory private constructor() {
    private val weakHandler = WeakHandler(Looper.getMainLooper())
    private val executors = Executors.newSingleThreadExecutor()

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory()
        }
    }

    fun download(lifecycleOwner: LifecycleOwner, downloadUrl: String, filePath: String, fileName: String, onDownloadListener: OnDownloadListener?) {
        FileUtil.deleteDir(filePath)
        download(downloadUrl).observe(lifecycleOwner, Observer {
            executors.execute {
                var inputStream: InputStream? = null
                var fileOutputStream: FileOutputStream? = null
                try {
                    val file = File(FileUtil.isExistDir(filePath), fileName)
                    val buf = ByteArray(2048)
                    val total = it.contentLength()
                    inputStream = it.byteStream()
                    fileOutputStream = FileOutputStream(file)
                    var len: Int
                    var sum: Long = 0
                    while (((inputStream.read(buf)).also { len = it }) != -1) {
                        fileOutputStream.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        weakHandler.post { onDownloadListener?.onDownloading(progress) }
                    }
                    fileOutputStream.flush()
                    weakHandler.post { onDownloadListener?.onDownloadSuccess(file.path) }
                } catch (e: Exception) {
                    weakHandler.post { onDownloadListener?.onDownloadFailed(e) }
                } finally {
                    inputStream?.close()
                    fileOutputStream?.close()
                    weakHandler.post { onDownloadListener?.onDownloadComplete() }
                }
            }
            executors.isShutdown
        })
    }

}