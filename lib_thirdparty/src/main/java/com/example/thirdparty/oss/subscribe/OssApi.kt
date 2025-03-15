package com.example.thirdparty.oss.subscribe

import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.thirdparty.oss.bean.OssSts
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * author:wyb
 * oss文件上传接口类
 */
interface OssApi {

    @GET("swallow/sts/aliyun/oss")
    suspend fun getOssTokenApi(): ApiResponse<OssSts>

    @PUT("swallow/attestation/site/info/{attestationId}")
    suspend fun getOssEditApi(@Path("attestationId") attestationId: String, @Body requestBody: RequestBody): ApiResponse<EmptyBean>

}
