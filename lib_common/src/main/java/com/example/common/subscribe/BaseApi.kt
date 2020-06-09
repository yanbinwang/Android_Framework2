package com.example.common.subscribe

import androidx.lifecycle.LiveData
import com.example.common.constant.Constants.URL
import com.example.common.model.BaseModel
import com.example.common.model.UploadModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * author:wyb
 * 通用接口类
 */
interface BaseApi {

    @Streaming
    @GET
    fun download(@Url downloadUrl: String): LiveData<ResponseBody>

    @Multipart
    @Streaming
    @POST(URL)
    fun getUploadFile(@Header("User-Agent") agent: String, @Part partList: List<MultipartBody.Part>): LiveData<BaseModel<UploadModel>>

    @FormUrlEncoded
    @POST(URL)
    fun getSendVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<BaseModel<Any>>

    @FormUrlEncoded
    @POST(URL)
    fun getVerification(@Header("User-Agent") agent: String, @FieldMap map: Map<String, String>): LiveData<BaseModel<Any>>

}
