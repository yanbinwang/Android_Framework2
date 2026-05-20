package com.example.thirdparty.media.oss.network

import com.example.common.network.factory.RetrofitFactory
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.thirdparty.media.oss.bean.OssSts
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

    companion object {
        val instance by lazy { RetrofitFactory.instance.createByServer(OssApi::class.java) }
    }

    /**
     * 获取oss授权token
     */
    @GET("swallow/sts/aliyun/oss")
    suspend fun getOssTokenApi(): ApiResponse<OssSts>

    /**
     * 编辑oss信息（告知服务器文件信息）
     */
    @PUT("swallow/attestation/site/info/{attestationId}")
    suspend fun getOssEditApi(@Path("attestationId") attestationId: String, @Body requestBody: RequestBody): ApiResponse<EmptyBean>

}
