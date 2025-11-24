package com.example.glide.callback.progress

import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logE
import com.example.framework.utils.logWTF
import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource

/**
 *  Created by wangyanbin
 *  监听加载进度
 */
class ProgressSource(private var responseBody: ResponseBody, private var onProgress: ((progress: Int) -> Unit)?) : ForwardingSource(responseBody.source()) {
    private var currentProgress = 0
    private var totalBytesRead = 0L

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        val fullLength = responseBody.contentLength()
        if (bytesRead == -1L) {
            totalBytesRead = fullLength.orZero
        } else {
            totalBytesRead += bytesRead
        }
        // float和double使用了IEEE 754标准,标准规定：浮点数除以0等于正无穷或负无穷，不会造成闪退
        val progress = (100f * totalBytesRead / fullLength.orZero).toSafeInt()
        "download progress is $progress".logE("ProgressSource")
        if (progress != currentProgress) {
            onProgress?.invoke(progress)
        }
        if (totalBytesRead == fullLength) {
            onProgress = null
        }
        currentProgress = progress
        return bytesRead
    }
}