package com.example.common.network

import com.example.common.network.factory.RetrofitFactory
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * author:wyb
 * 通用接口类
 * 所有模块新建network
 * @Multipart
 *     @POST("api/file/upload/image")
 *     suspend fun getUploadApi(@Part file: MultipartBody.Part): ApiResponse<String>
 * request({ CommonSubscribe.getUploadApi(MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("multipart/form-data".toMediaTypeOrNull()))) })
 *
 * suspend fun getCertifiedUploadApi(type: RequestBody, file: MultipartBody.Part) = accountApi.getCertifiedUploadApi(type, file)
 * AccountSubscribe.getCertifiedUploadApi(reqBodyOf("type" to bean.extras.orEmpty()), MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("multipart/form-data".toMediaTypeOrNull())))
 */
interface CommonApi {

    companion object {
        val instance by lazy { RetrofitFactory.instance.createByServer(CommonApi::class.java) }
        // 下载专用
        val downloadInstance by lazy { RetrofitFactory.instance.create(CommonApi::class.java) }
    }

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