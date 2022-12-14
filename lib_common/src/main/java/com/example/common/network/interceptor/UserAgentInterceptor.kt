package com.example.common.network.interceptor

import android.os.Build
import android.util.ArrayMap
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created by WangYanBin on 2020/6/1.
 * 用户拦截器
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
//        val token: String? = AccountHelper.getToken()//取得本地token
//        if (!TextUtils.isEmpty(token)) {
//            params["Authorization"] = "basic $token"
//        }
        val params = ArrayMap<String, String>()
        params["system-name"] = "Android"
        params["phone-model"] = Build.MODEL
        val builder = Headers.Builder()
        for (key in params.keys) {
            builder.add(key, params[key].orEmpty())
        }
        return builder.build()
    }
}