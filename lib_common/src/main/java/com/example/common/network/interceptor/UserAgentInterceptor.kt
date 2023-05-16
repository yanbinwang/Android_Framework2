package com.example.common.network.interceptor

import android.os.Build
import com.example.common.utils.helper.AccountHelper
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created by WangYanBin on 2020/6/1.
 * 用户拦截器
 * 请求头中添加后台需要的参数
 */
internal class UserAgentInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .headers(defaultHeaders())
            .build()
        return chain.proceed(request)
    }

    private fun defaultHeaders(): Headers {
        val builder = Headers.Builder()
        builder.add("system-name", "Android")
        builder.add("phone-model", Build.MODEL)
        //取得本地token
        AccountHelper.getToken()?.apply { if (!isNullOrEmpty()) builder.add("Authorization-model", "basic $this") }
        return builder.build()
    }
}