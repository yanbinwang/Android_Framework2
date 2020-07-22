package com.example.common.subscribe

import androidx.lifecycle.LiveData
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
    fun download(@Url downloadUrl: String): LiveData<okhttp3.ResponseBody>

    @Multipart
    @Streaming
    @POST("http://www.baidu.com")
    fun getUploadFile(@Header("User-Agent") agent: String, @Part partList: List<MultipartBody.Part>): LiveData<ApiResponse<UploadModel>>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    fun getSendVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("http://www.baidu.com")
    fun getVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<ApiResponse<Any>>

    @GET("test")
    fun getTestApi(): LiveData<ApiResponse<Any>>

}
