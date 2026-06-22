package com.example.glide.callback.progress

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
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
        @JvmStatic
        fun addListener(url: String, onProgress: ((progress: Int) -> Unit)) {
            if (url.isNotBlank()) {
                listenerMap[url] = onProgress
            }
        }

        /**
         * 取消注册下载监听
         */
        @JvmStatic
        fun removeListener(url: String) {
            if (url.isNotBlank()) {
                listenerMap.remove(url)
            }
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        // 获取 listener 实例 , 如果没有注册监听器，直接返回原始响应
        val listener = listenerMap[url] ?: return chain.proceed(request)
        // 如果有监听器，则继续执行请求，并包装响应体
        val response = chain.proceed(request)
        val body = response.body
        // 将 listener 直接传递给 ProgressResponseBody，而不是让它再去 Map 里取
        return response.newBuilder()
            .body(ProgressResponseBody(body, listener))
            .build()
    }

}