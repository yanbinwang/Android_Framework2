package com.example.common.network.factory

import com.example.common.config.ServerConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * author: wyb
 * date: 2019/7/30.
 * retrofit单例
 */
class RetrofitFactory private constructor() {
    private val serverRetrofit by lazy {
        Retrofit.Builder()
            .client(OkHttpFactory.instance.serverOkHttpClient)
            .baseUrl(ServerConfig.serverUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(OkHttpFactory.instance.okHttpClient)
            .baseUrl(ServerConfig.serverUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    companion object {
        @JvmStatic
        val instance by lazy { RetrofitFactory() }
    }

    //获取一个请求API
    fun <T> createByServer(service: Class<T>): T {
        return serverRetrofit.create(service)
    }

    //获取一个不加头不加拦截器的API
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

}
