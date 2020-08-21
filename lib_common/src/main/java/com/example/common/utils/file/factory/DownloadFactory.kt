package com.example.common.utils.file.factory

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.example.common.http.callback.HttpObserver
import com.example.common.subscribe.BaseSubscribe.download
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import com.example.common.utils.handler.WeakHandler
import okhttp3.ResponseBody
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
    private var complete = false//请求在完成并返回对象后又发起了线程下载，所以回调监听需要保证线程完成在回调

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory()
        }
    }

    fun download(lifecycleOwner: LifecycleOwner, downloadUrl: String, filePath: String, fileName: String, onDownloadListener: OnDownloadListener?) {
        FileUtil.deleteDir(filePath)
        download(downloadUrl).observe(lifecycleOwner, object : HttpObserver<ResponseBody>() {

            override fun onStart() {
                complete = false
                onDownloadListener?.onStart()
            }

            override fun onNext(t: ResponseBody?) {
                if (null != t) {
                    executors.execute {
                        var inputStream: InputStream? = null
                        var fileOutputStream: FileOutputStream? = null
                        try {
                            val file = File(FileUtil.isExistDir(filePath), fileName)
                            val buf = ByteArray(2048)
                            val total = t.contentLength()
                            inputStream = t.byteStream()
                            fileOutputStream = FileOutputStream(file)
                            var len: Int
                            var sum: Long = 0
                            while (((inputStream.read(buf)).also { len = it }) != -1) {
                                fileOutputStream.write(buf, 0, len)
                                sum += len.toLong()
                                val progress = (sum * 1.0f / total * 100).toInt()
                                weakHandler.post { onDownloadListener?.onLoading(progress) }
                            }
                            fileOutputStream.flush()
                            weakHandler.post { onDownloadListener?.onSuccess(file.path) }
                        } catch (e: Exception) {
                            weakHandler.post { onDownloadListener?.onFailed(e) }
                        } finally {
                            inputStream?.close()
                            fileOutputStream?.close()
                            complete = true
                            onComplete()
                        }
                    }
                    executors.isShutdown
                } else {
                    onDownloadListener?.onFailed(null)
                    complete = true
                    onComplete()
                }
            }

            override fun onComplete() {
                if (complete) {
                    complete = false
                    onDownloadListener?.onComplete()
                }
            }
        })
    }

}