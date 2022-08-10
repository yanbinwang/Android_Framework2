package com.example.common.imageloader.glide.callback.progress

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by wangyanbin
 *  进度条拦截器
 */
class ProgressInterceptor : Interceptor {

    companion object {
        val listenerMap by lazy { ConcurrentHashMap<String, ProgressListener>() }

        /**
         * 注册下载监听
         */
        fun addListener(url: String, listener: ProgressListener) {
            listenerMap[url] = listener
        }

        /**
         * 取消注册下载监听
         */
        fun removeListener(url: String) {
            listenerMap.remove(url)
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()
        return response.newBuilder().body(ProgressResponseBody(url, response.body!!)).build()
    }

}