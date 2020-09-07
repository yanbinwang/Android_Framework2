package com.example.common.utils.file.factory

import com.example.common.http.repository.ApiRepository
import com.example.common.http.repository.ResourceSubscriber
import com.example.common.subscribe.CommonSubscribe
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.callback.OnDownloadListener
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * author: wyb
 * 下载单例
 */
class DownloadFactory private constructor() : CoroutineScope {
    private var complete = false//标识本次请求是否完成，并非下载是否完成

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    fun download(
        downloadUrl: String,
        filePath: String,
        fileName: String,
        onDownloadListener: OnDownloadListener?
    ) {
        launch(Dispatchers.Main) {
            FileUtil.deleteDir(filePath)
            ApiRepository.call(
                CommonSubscribe.getDownloadApi(downloadUrl),
                object : ResourceSubscriber<ResponseBody>() {

                    override fun onStart() {
                        complete = false
                        onDownloadListener?.onStart()
                    }

                    override fun onNext(t: ResponseBody?) {
                        complete = true
                        if (t != null) {
                            launch {
                                val file = File(FileUtil.isExistDir(filePath), fileName)
                                var inputStream: InputStream? = null
                                var fileOutputStream: FileOutputStream? = null
                                try {
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
                    }

                    override fun onError(e: Exception?) {
                        onDownloadListener?.onFailed(e)
                    }

                    override fun onComplete() {
                        if (complete) {
                            onDownloadListener?.onComplete()
                        }
                        cancel()
                    }
                })


//            withContext(Dispatchers.IO) { startDownload(CommonSubscribe.getDownloadApi(downloadUrl), File(FileUtil.isExistDir(filePath), fileName), onDownloadListener) }
        }
    }

    private suspend fun startDownload(
        body: okhttp3.ResponseBody,
        file: File,
        onDownloadListener: OnDownloadListener?
    ) {
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