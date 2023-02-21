package com.example.common.network.factory

import com.example.common.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2019/7/30.
 * retrofit单例
 */
class RetrofitFactory private constructor() {
    private val serverRetrofit by lazy {
        Retrofit.Builder()
            .client(OkHttpFactory.instance.okHttpClient)
            .baseUrl(BuildConfig.LOCALHOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //纯粹的网络请求，不加任何拦截
    private val retrofit by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder()
                    .connectTimeout(6, TimeUnit.SECONDS)//设置连接超时
                    .writeTimeout(2, TimeUnit.HOURS)//设置写超时
                    .readTimeout(2, TimeUnit.HOURS)//设置读超时
                    .retryOnConnectionFailure(true)
                    .build())
            .baseUrl(BuildConfig.LOCALHOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    companion object {
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
