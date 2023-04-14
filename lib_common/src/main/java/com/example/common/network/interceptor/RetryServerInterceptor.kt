package com.example.common.network.interceptor

import com.example.common.config.ServerConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * author: wyb
 * date: 2019/7/9.
 * 重置网络请求地址拦截器
 */
internal class RetryServerInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val headerValues: String
        val request = chain.request()
        headerValues = request.headers.toString()
        //当请求头中包含User-Agent，切换请求地址(第三个参数为切换的具体地址)
        if (headerValues.contains("User-Agent")) {
            return retryServer(chain, request, ServerConfig.serverUrl())
        }
        return chain.proceed(request)
    }

    //切换请求前缀地址
    private fun retryServer(chain: Interceptor.Chain, request: Request, server: String): Response {
        var response: Response? = null
        val newRequest =
            request.newBuilder().url(request.url.toString().replace(ServerConfig.serverUrl(), server)).build()
        try {
            response = chain.proceed(newRequest)
        } catch (_: Exception) {
        }
        return response ?: chain.proceed(request)
    }

}
