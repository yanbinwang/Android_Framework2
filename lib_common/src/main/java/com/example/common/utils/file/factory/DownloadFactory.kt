package com.example.common.utils.file.factory

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.common.subscribe.BaseSubscribe.download
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import com.example.framework.widget.WeakHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * author: wyb
 * 下载单例
 */
@SuppressLint("CheckResult")
class DownloadFactory private constructor() {
    private val weakHandler: WeakHandler =
        WeakHandler(Looper.getMainLooper())

    companion object {
        val instance: DownloadFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadFactory()
        }
    }

    fun download(owner: LifecycleOwner, downloadUrl: String, saveDir: String, fileName: String, onDownloadListener: OnDownloadListener) {
        FileUtil.deleteDir(saveDir)
        download(downloadUrl)
            .observe(owner, Observer {
                object : Thread() {
                    override fun run() {
                        var inputStream: InputStream? = null
                        val buf = ByteArray(2048)
                        var len: Int
                        var fileOutputStream: FileOutputStream? = null
                        try {
                            inputStream = it.byteStream()
                            val total = it.contentLength()
                            val file = File(FileUtil.isExistDir(saveDir), fileName)
                            fileOutputStream = FileOutputStream(file)
                            var sum: Long = 0
                            while (((inputStream.read(buf)).also { len = it }) != -1) {
                                fileOutputStream.write(buf, 0, len)
                                sum += len.toLong()
                                val progress = (sum * 1.0f / total * 100).toInt()
                                weakHandler.post { onDownloadListener.onDownloading(progress) }
                            }
                            fileOutputStream.flush()
                            weakHandler.post { onDownloadListener.onDownloadSuccess(file.path) }
                        } catch (e: Exception) {
                            weakHandler.post { onDownloadListener.onDownloadFailed(e) }
                        } finally {
                            try {
                                inputStream?.close()
                                fileOutputStream?.close()
                            } catch (ignored: IOException) {
                            }
                        }
                    }
                }.start()
            })
    }
}
