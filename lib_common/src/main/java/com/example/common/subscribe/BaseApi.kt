package com.example.common.subscribe

import com.example.common.http.callback.ApiResponse
import com.example.common.model.UploadModel
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * author:wyb
 * 通用接口类
 */
interface BaseApi {

    @Streaming
    @GET
    suspend fun download(@Url downloadUrl: String): okhttp3.ResponseBody

    @Multipart
    @Streaming
    @POST("http://www.baidu.com")
    suspend fun getUploadFile(@Header("User-Agent") agent: String, @Part partList: List<MultipartBody.Part>): ApiResponse<UploadModel>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    suspend fun getSendVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): ApiResponse<Any>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    suspend fun getVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): ApiResponse<Any>

    @GET("test")
    suspend fun getTestApi(): Any

}
