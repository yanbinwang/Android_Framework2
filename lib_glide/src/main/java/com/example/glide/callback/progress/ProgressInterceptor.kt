package com.example.glide.callback.progress

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by wangyanbin
 *  进度条拦截器
 */
class ProgressInterceptor : Interceptor {

    companion object {
        internal val listenerMap by lazy { ConcurrentHashMap<String, ((progress: Int) -> Unit)>() }

        /**
         * 注册下载监听
         */
        fun addListener(url: String, onProgress: ((progress: Int) -> Unit)) {
            listenerMap[url] = onProgress
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
        return response.newBuilder().body(ProgressResponseBody(url, response.body ?: "".toResponseBody())).build()
    }

}