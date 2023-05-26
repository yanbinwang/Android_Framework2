package com.example.common.subscribe

import com.example.common.network.repository.ApiResponse
import retrofit2.http.*

/**
 * author:wyb
 * 通用接口类
 */
interface CommonApi {

    @Streaming
    @GET
    suspend fun getDownloadApi(@Url downloadUrl: String): okhttp3.ResponseBody

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    suspend fun getSendVerificationApi(@Header("Retry-Agent") retryAgent: String, @FieldMap map: Map<String, String>): ApiResponse<Any>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    suspend fun getVerificationApi(@FieldMap map: Map<String, String>): ApiResponse<Any>

    @GET("test")
    suspend fun getTestApi(): ApiResponse<Any>

}
