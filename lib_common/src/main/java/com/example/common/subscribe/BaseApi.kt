package com.example.common.subscribe

import androidx.lifecycle.LiveData
import com.example.common.constant.Constants.URL
import com.example.common.http.ResponseBody
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
    @POST(URL)
    fun getUploadFile(@Header("User-Agent") agent: String, @Part partList: List<MultipartBody.Part>): LiveData<ResponseBody<UploadModel>>

    @FormUrlEncoded
    @POST(URL)
    fun getSendVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<ResponseBody<Any>>

    @FormUrlEncoded
    @POST(URL)
    fun getVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<ResponseBody<Any>>

    @GET("URL")
    fun getTestApi(): LiveData<ResponseBody<Any>>

}
