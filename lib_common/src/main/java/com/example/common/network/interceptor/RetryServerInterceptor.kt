package com.example.common.network.interceptor

import com.example.common.config.ServerConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * author: wyb
 * date: 2019/7/9.
 * 重置网络请求地址拦截器（一般不会用到）
 * 项目中对接了第三方接口，可能需要在某些情况下，切换对应服务器发起的请求前缀，
 * retrofit支持自定义头，包含特殊头的接口地址，接口前缀替换为对应三方的前缀
 */
internal class RetryServerInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val headerValues: String
        val request = chain.request()
        headerValues = request.headers.toString()
        //当请求头中包含Retry-Agent，切换请求地址(第三个参数为切换的具体地址)
        if (headerValues.contains("Retry-Agent")) {
            return retryServer(chain, request, ServerConfig.serverUrl())
        }
        return chain.proceed(request)
    }

    //切换请求前缀地址
    private fun retryServer(chain: Interceptor.Chain, request: Request, server: String): Response {
        var response: Response? = null
        val newRequest = request.newBuilder().url(request.url.toString().replace(ServerConfig.serverUrl(), server)).build()
        try {
            response = chain.proceed(newRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response ?: chain.proceed(request)
    }

}
