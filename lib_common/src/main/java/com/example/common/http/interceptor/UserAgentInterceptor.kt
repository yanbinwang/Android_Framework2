package com.example.common.http.interceptor

import android.os.Build
import android.util.ArrayMap
import com.example.common.BuildConfig
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * Created by WangYanBin on 2020/6/1.
 * 用户拦截器
 */
class UserAgentInterceptor : Interceptor {

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
        params["yoogurt-request-id"] = UUID.randomUUID().toString()
        params["X-yoogurt-system-type"] = "1"
        params["X-yoogurt-system-name"] = "Android"
        params["X-yoogurt-system-version"] = Build.VERSION.RELEASE
        params["X-yoogurt-api-version"] = "v1"
        params["X-yoogurt-app-version"] = BuildConfig.VERSION_CODE.toString()
        params["X-yoogurt-phone-model"] = Build.MODEL
        val builder = Headers.Builder()
        for (key in params.keys) {
            builder.add(key, params[key]!!)
        }
        return builder.build()
    }
}