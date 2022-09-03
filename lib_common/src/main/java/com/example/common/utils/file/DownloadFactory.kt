package com.example.common.utils.file

import android.util.Patterns
import com.example.base.utils.ToastUtil
import com.example.common.BaseApplication
import com.example.common.http.repository.call
import com.example.common.subscribe.CommonSubscribe.getDownloadApi
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * author: wyb
 * 下载单例
 * url下载得到的是一个ResponseBody对象，对该对象进行下载和UI刷新
 * 故下载的完成回调需要在线程内外做判断
 */
class DownloadFactory private constructor(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private var job: Job? = null

    companion object {
        @JvmStatic
        val instance: DownloadFactory by lazy {
            DownloadFactory(Dispatchers.Main)
        }
    }

    fun download(
        downloadUrl: String,
        filePath: String,
        fileName: String,
        onStart: () -> Unit = {},
        onSuccess: (path: String) -> Unit = {},
        onLoading: (progress: Int) -> Unit = {},
        onFailed: (e: Exception?) -> Unit = {},
        onComplete: () -> Unit = {}) {
        if (!Patterns.WEB_URL.matcher(downloadUrl).matches()) {
            ToastUtil.mackToastSHORT("链接地址不合法", BaseApplication.instance?.applicationContext!!)
            return
        }
        job = launch {
            //清除目录下的所有文件
            FileUtil.deleteDir(filePath)
            onStart()
            //开启一个获取下载对象的协程，监听中如果对象未获取到，则中断携程，并且完成这一次下载
            val body = getDownloadApi(downloadUrl).call()
            if (null == body) {
                onFailed(Exception("ResponseBody is Null"))
                onComplete()
            } else {
                //在上一步协程成功并拿到对象后开始执行，创建一个安装的文件，开启io协程，写入
                val file = File(FileUtil.isExistDir(filePath), fileName)
                withContext(Dispatchers.IO) {
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
                            withContext(Dispatchers.Main) { onLoading(progress) }
                        }
                        fileOutputStream.flush()
                        withContext(Dispatchers.Main) { onSuccess(file.path) }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { onFailed(e) }
                    } finally {
                        inputStream?.close()
                        fileOutputStream?.close()
                        withContext(Dispatchers.Main) { onComplete() }
                    }
                }
            }
        }
    }

}