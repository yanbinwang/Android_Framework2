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
