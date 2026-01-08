package com.example.common.network.factory

import com.example.common.network.interceptor.LoggingInterceptor
import com.example.common.network.interceptor.UserAgentInterceptor
import com.example.framework.utils.function.value.isDebug
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2019/7/30.
 * okhttp单例
 */
class OkHttpFactory private constructor() {
    /**
     * 服务器网络请求（常规接口）
     */
    val serverOkHttpClient by lazy {
        val builder = createOkHttpBuilder()
            // 建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间 （常规请求6s）
            .connectTimeout(6, TimeUnit.SECONDS)
            // 从调用call.execute()和enqueue()这两个方法开始计时,时间到后网络还未请求完成将调用cancel()方法 （整个请求的总超时，覆盖所有阶段，避免无限挂起）
            .callTimeout(120, TimeUnit.SECONDS)
            // 设置读超时 （常规接口15s足够「合理区间」10-30s，避免阻塞过久）
            .readTimeout(15, TimeUnit.SECONDS)
            // 设置写超时
            .writeTimeout(15, TimeUnit.SECONDS)
//            // 只有http2和webSocket中有使用,如果设置了这个值会定时的向服务器发送一个消息来保持长连接
//            .pingInterval(5, TimeUnit.SECONDS)
            // 请求加头
            .addInterceptor(UserAgentInterceptor())
//            // 重新构建请求
//            .addInterceptor(RetryServerInterceptor())
            // 优化DNS解析（复用安卓系统自带的 DNS 解析功能，提升域名解析速度）
            .dns(Dns.SYSTEM)
        if (isDebug) {
            // 日志监听
            builder.addInterceptor(LoggingInterceptor())
        }
        builder.build()
    }

    /**
     * 纯粹的网络请求，不加任何拦截 （上传/下载专用，大文件传输）
     */
    val okHttpClient by lazy {
        // 上传下载关闭自动重试 (否则会导致重复上传 / 文件损坏)
        createOkHttpBuilder(false)
            .connectTimeout(6, TimeUnit.SECONDS)
            // 上传/下载超长超时（2小时）
            .callTimeout(2, TimeUnit.HOURS)
            .writeTimeout(2, TimeUnit.HOURS)
            .readTimeout(2, TimeUnit.HOURS)
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