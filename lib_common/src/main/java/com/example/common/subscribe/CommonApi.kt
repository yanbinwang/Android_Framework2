package com.example.common.subscribe

import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
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
    suspend fun getSendVerificationApi(@Header("Retry-Agent") retryAgent: String, @FieldMap map: Map<String, String>): ApiResponse<EmptyBean>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    suspend fun getVerificationApi(@FieldMap map: Map<String, String>): ApiResponse<EmptyBean>

    @GET("test")
    suspend fun getTestApi(@FieldMap map: Map<String, String>): ApiResponse<EmptyBean>

}
