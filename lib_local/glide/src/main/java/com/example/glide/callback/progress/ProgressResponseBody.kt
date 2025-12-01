package com.example.glide.callback.progress

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.buffer

/**
 *  Created by wangyanbin
 *  拦截器窗体
 */
class ProgressResponseBody(private var responseBody: ResponseBody,private val listener: ((progress: Int) -> Unit)?) : ResponseBody() {
    private val bufferedSource by lazy { ProgressSource(responseBody, listener).buffer() }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        return bufferedSource
    }

}