package com.example.common.http.factory

import com.example.common.BuildConfig
import com.example.common.http.adapter.CustomGsonConverterFactory
import retrofit2.Retrofit

/**
 * author: wyb
 * date: 2019/7/30.
 * retrofit单例
 */
class RetrofitFactory private constructor() {
    private val retrofit = Retrofit.Builder()
        .client(OkHttpFactory.instance.okHttpClient)
        .baseUrl(BuildConfig.LOCALHOST)
        .addConverterFactory(CustomGsonConverterFactory())
        .build()

    companion object {
        @JvmStatic
        val instance: RetrofitFactory by lazy {
            RetrofitFactory()
        }
    }

    //获取一个请求API
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

}
