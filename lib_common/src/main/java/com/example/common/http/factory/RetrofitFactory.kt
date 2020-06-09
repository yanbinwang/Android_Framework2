package com.example.common.http.factory

import com.example.common.BuildConfig
import com.example.common.http.adapter.LiveDataCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * author: wyb
 * date: 2019/7/30.
 * retrofit单例
 */
class RetrofitFactory private constructor() {
    private val retrofit: Retrofit = Retrofit.Builder()
            .client(OkHttpFactory.instance.okHttpClient)
            .baseUrl(BuildConfig.LOCALHOST)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()

    companion object {
        val instance: RetrofitFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitFactory()
        }
    }

    //获取一个请求API
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

}
