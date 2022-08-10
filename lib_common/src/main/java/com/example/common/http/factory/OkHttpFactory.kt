package com.example.common.http.factory

import com.example.common.http.interceptor.LoggingInterceptor
import com.example.common.http.interceptor.UserAgentInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2019/7/30.
 * okhttp单例
 */
class OkHttpFactory private constructor() {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)//建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间
        .callTimeout(120, TimeUnit.SECONDS)//从调用call.execute()和enqueue()这两个方法开始计时,时间到后网络还未请求完成将调用cancel()方法
//        .pingInterval(5, TimeUnit.SECONDS)//只有http2和webSocket中有使用,如果设置了这个值会定时的向服务器发送一个消息来保持长连接
        .readTimeout(60, TimeUnit.SECONDS)//设置读超时
        .writeTimeout(60, TimeUnit.SECONDS)//设置写超时
        .retryOnConnectionFailure(true)
        .addInterceptor(UserAgentInterceptor())//请求加头
        .addInterceptor(LoggingInterceptor())//日志监听
//            .addInterceptor(RetryServerInterceptor())//重新构建请求
        .build()

    companion object {
        @JvmStatic
        val instance by lazy { OkHttpFactory() }
    }

}