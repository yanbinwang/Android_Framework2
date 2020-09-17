package com.example.common.utils.file.factory

import com.example.common.http.repository.ResourceSubscriber
import com.example.common.http.repository.call
import com.example.common.subscribe.CommonSubscribe.getDownloadApi
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * author: wyb
 * 下载单例
 * url下载得到的是一个ResponseBody对象，对该对象还需开启异步线程进行下载和UI刷新
 * 故下载的完成回调需要在线程内外做判断
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
            getDownloadApi(downloadUrl).call(object : ResourceSubscriber<ResponseBody>() {

                override fun onStart() {
                    onDownloadListener?.onStart()
                }

                override fun doResult(data: ResponseBody?, throwable: Throwable?) {
                    if (null != data) {
                        launch(Dispatchers.IO) {
                            val file = File(FileUtil.isExistDir(filePath), fileName)
                            var inputStream: InputStream? = null
                            var fileOutputStream: FileOutputStream? = null
                            try {
                                val buf = ByteArray(2048)
                                val total = data.contentLength()
                                inputStream = data.byteStream()
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
                    } else {
                        onDownloadListener?.onFailed(throwable)
                        onDownloadListener?.onComplete()
                    }
                }

                override fun onComplete() {}
            })
        }
    }

}