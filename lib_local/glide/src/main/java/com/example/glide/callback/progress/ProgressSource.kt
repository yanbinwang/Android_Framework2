package com.example.glide.callback.progress

import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logE
import okhttp3.ResponseBody
import okio.Buffer
import okio.ForwardingSource
import kotlin.math.roundToLong

/**
 *  Created by wangyanbin
 *  监听加载进度
 */
class ProgressSource(responseBody: ResponseBody, private var onProgress: ((progress: Int) -> Unit)?) : ForwardingSource(responseBody.source()) {
    private var currentProgress = 0
    private var totalBytesRead = 0L
    private val fullLength = responseBody.contentLength() // 在初始化时缓存总长度

    override fun read(sink: Buffer, byteCount: Long): Long {
        // 调用父类的 read 方法，从原始 Source 读取数据到 sink 中
        val bytesRead = super.read(sink, byteCount)
        /**
         * 仅在总长度已知且有效时，才进行进度相关的计算
         * float和double使用了IEEE 754标准,标准规定：浮点数除以0等于正无穷或负无穷，不会造成闪退
         */
        if (fullLength > 0) {
            // 更新已读取字节数
            if (bytesRead == -1L) {
                // 读取完毕，直接置为总长度以确保100%
                totalBytesRead = fullLength
            } else {
                totalBytesRead += bytesRead
            }
            // 计算当前进度百分比（使用四舍五入）
            val progressFloat = 100f * totalBytesRead / fullLength
            val progress = progressFloat.roundToLong().toSafeInt()
            // 如果进度有变化，则触发回调
            if (progress != currentProgress) {
                onProgress?.invoke(progress)
                currentProgress = progress
                "download progress is $progress".logE("ProgressSource")
            }
            // 检查是否下载完成，完成则释放回调引用
            if (totalBytesRead == fullLength) {
                onProgress = null
            }
        }
        // 无论是否计算进度，都必须将实际读取的字节数返回给OkHttp
        return bytesRead
    }

}