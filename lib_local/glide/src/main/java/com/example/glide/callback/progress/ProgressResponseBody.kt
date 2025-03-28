package com.example.glide.callback.progress

import com.example.framework.utils.function.value.orZero
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.buffer

/**
 *  Created by wangyanbin
 *  拦截器窗体
 */
class ProgressResponseBody(url: String, private var responseBody: ResponseBody) : ResponseBody() {
    private val listener by lazy { ProgressInterceptor.listenerMap[url] }
    private val bufferedSource by lazy { ProgressSource(responseBody, listener).buffer() }

    override fun contentLength(): Long {
        return responseBody.contentLength().orZero
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        return bufferedSource
    }

}