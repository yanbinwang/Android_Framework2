package com.dataqin.common.utils.file.factory

import android.os.Looper
import com.dataqin.common.utils.file.FileUtil
import com.dataqin.common.utils.file.callback.OnDownloadListener
import com.dataqin.common.utils.handler.WeakHandler
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

/**
 * Created by wangyanbin
 * 下载地址中带有中文，oss的下载地址中部分Retrofit2无法支持转码
 * 使用当前下载类
 */
class CharactersFactory {
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val executors by lazy { Executors.newSingleThreadExecutor() }

    companion object {
        @JvmStatic
        val instance: CharactersFactory by lazy {
            CharactersFactory()
        }
    }

    fun download(downloadUrl: String, filePath: String, fileName: String, onDownloadListener: OnDownloadListener?) {
        onDownloadListener?.onStart()
        FileUtil.deleteDir(filePath)
        val request: Request = Request.Builder().url(downloadUrl).build()
        val call = OkHttpClient.Builder().build().newCall(request)
        call.enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                executors.execute {
                    if (null != response.body) {
                        var inputStream: InputStream? = null
                        var fileOutputStream: FileOutputStream? = null
                        try {
                            val file = File(FileUtil.isExistDir(filePath), fileName)
                            val buf = ByteArray(2048)
                            val total = response.body!!.contentLength()
                            inputStream = response.body!!.byteStream()
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
                            weakHandler.post { onDownloadListener?.onComplete() }
                        }
                    } else {
                        onFailure(call, IOException())
                    }
                }
                executors.isShutdown
            }

            override fun onFailure(call: Call, e: IOException) {
                weakHandler.post {
                    onDownloadListener?.onFailed(e)
                    onDownloadListener?.onComplete()
                }
            }
        })
    }

}