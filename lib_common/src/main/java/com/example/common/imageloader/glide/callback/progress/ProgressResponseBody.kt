package com.example.common.imageloader.glide.callback.progress

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.buffer

/**
 *  Created by wangyanbin
 *  拦截器窗体
 */
class ProgressResponseBody(var url: String, var responseBody: ResponseBody) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    private val listener = ProgressInterceptor.LISTENER_MAP[url]

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        if (null == bufferedSource) {
            bufferedSource = (ProgressSource(responseBody.source(), responseBody, listener).buffer())
        }
        return bufferedSource!!
    }

}