package com.example.common.imageloader.glide.callback.progress

import com.example.base.utils.logE
import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.Source

/**
 *  Created by wangyanbin
 *  监听加载进度
 */
class ProgressSource(source: Source, var responseBody: ResponseBody, var onProgress: ((progress: Int) -> Unit)?) : ForwardingSource(source) {
    private var currentProgress = 0
    private var totalBytesRead: Long = 0

    override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        val fullLength = responseBody.contentLength()
        if (bytesRead == -1L) {
            totalBytesRead = fullLength
        } else {
            totalBytesRead += bytesRead
        }
        val progress = (100f * totalBytesRead / fullLength).toInt()
        "download progress is $progress".logE("ProgressSource")
        if (progress != currentProgress) onProgress?.invoke(progress)
        if (totalBytesRead == fullLength) onProgress = null
        currentProgress = progress
        return bytesRead
    }
}