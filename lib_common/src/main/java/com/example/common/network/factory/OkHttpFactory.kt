package com.example.common.network.factory

import com.example.common.network.interceptor.LoggingInterceptor
import com.example.common.network.interceptor.MemoryTrackingInterceptor
import com.example.common.network.interceptor.UserAgentInterceptor
import com.example.framework.utils.function.value.isDebug
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2019/7/30.
 * okhttp单例
 */
class OkHttpFactory private constructor() {
    /**
     * 服务器网络请求
     */
    val serverOkHttpClient by lazy {
        val builder = createOkHttpBuilder()
            .connectTimeout(6, TimeUnit.SECONDS)//建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间
            .callTimeout(120, TimeUnit.SECONDS)//从调用call.execute()和enqueue()这两个方法开始计时,时间到后网络还未请求完成将调用cancel()方法
//        .pingInterval(5, TimeUnit.SECONDS)//只有http2和webSocket中有使用,如果设置了这个值会定时的向服务器发送一个消息来保持长连接
            .readTimeout(60, TimeUnit.SECONDS)//设置读超时
            .writeTimeout(60, TimeUnit.SECONDS)//设置写超时
            .addInterceptor(UserAgentInterceptor())//请求加头
//            .addInterceptor(RetryServerInterceptor())//重新构建请求
        if (isDebug) {
            builder.addInterceptor(LoggingInterceptor())//日志监听
                .addInterceptor(MemoryTrackingInterceptor())//请求内存监听
        }
        builder.build()
    }

    /**
     * 纯粹的网络请求，不加任何拦截
     */
    val okHttpClient by lazy {
        createOkHttpBuilder()
            .connectTimeout(6, TimeUnit.SECONDS)//设置连接超时
            .writeTimeout(2, TimeUnit.HOURS)//设置写超时
            .readTimeout(2, TimeUnit.HOURS)//设置读超时
            .build()
    }

    companion object {
        @JvmStatic
        val instance by lazy { OkHttpFactory() }
    }

    /**
     * 统一构建okhttp的builder
     */
    private fun createOkHttpBuilder(retryOnConnectionFailure: Boolean = true): OkHttpClient.Builder {
        return OkHttpClient.Builder().retryOnConnectionFailure(retryOnConnectionFailure)
    }

}