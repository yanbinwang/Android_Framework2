package com.example.common.http.factory

import com.example.common.http.interceptor.LoggingInterceptor
import com.example.common.http.interceptor.RetryServerInterceptor
import com.example.common.http.interceptor.UserAgentInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2019/7/30.
 * okhttp单例
 */
class OkHttpFactory private constructor() {
    val okHttpClient =
        OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS)//设置连接超时
            .readTimeout(6, TimeUnit.SECONDS)//设置读超时
            .writeTimeout(6, TimeUnit.SECONDS)//设置写超时
            .retryOnConnectionFailure(true)
            .addInterceptor(UserAgentInterceptor())//请求加头
            .addInterceptor(LoggingInterceptor())//日志监听
            .addInterceptor(RetryServerInterceptor())//重新构建请求
            .connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))//根据自己的机型设置同时连接的个数和时间（项目中配置8个，和每个保持时间为15s）
            .build()

    companion object {
        @JvmStatic
        val instance: OkHttpFactory by lazy {
            OkHttpFactory()
        }
    }

}
