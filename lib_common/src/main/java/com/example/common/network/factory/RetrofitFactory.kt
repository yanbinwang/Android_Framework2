package com.example.common.network.factory

import com.example.common.config.ServerConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * author: wyb
 * date: 2019/7/30.
 * retrofit单例
 */
class RetrofitFactory private constructor() {
//    private val serverRetrofit by lazy {
//        Retrofit.Builder()
//            .client(OkHttpFactory.instance.serverOkHttpClient)
//            .baseUrl(ServerConfig.serverUrl())
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    private val retrofit by lazy {
//        Retrofit.Builder()
//            .client(OkHttpFactory.instance.okHttpClient)
//            .baseUrl(ServerConfig.serverUrl())
////            .addConverterFactory(GsonConverterFactory.create())//该方法的作用是把服务器返回的 JSON 数据转换为 Java 或 Kotlin 对象。如请求仅涉及下载文件，或者服务器返回的数据并非 JSON 格式，无需添加此转换器
//            .build()
//    }
    private val serverRetrofit by lazy {
        createRetrofitBuilder(OkHttpFactory.instance.serverOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofit by lazy {
        createRetrofitBuilder(OkHttpFactory.instance.okHttpClient)
            .build()
    }

    companion object {
        @JvmStatic
        val instance by lazy { RetrofitFactory() }
    }

    /**
     * 统一构建retrofit2的builder
     */
    private fun createRetrofitBuilder(client: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder().client(client).baseUrl(ServerConfig.serverUrl())
    }

    /**
     * 获取一个请求API
     */
    fun <T> createByServer(service: Class<T>): T {
        return serverRetrofit.create(service)
    }

    /**
     * 获取一个不加头不加拦截器的API
     */
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

}
