package com.example.thirdparty.part.subscribe

import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import okhttp3.MultipartBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

/**
 * author:wyb
 * 分片接口类
 */
interface PartApi {

    @Multipart
    @Streaming
    @POST("evidences/onlyPartUpload")
    suspend fun getPartUploadApi(@Part partList: List<MultipartBody.Part>): ApiResponse<EmptyBean>

    @FormUrlEncoded
    @POST("evidences/combine")
    suspend fun getPartCombineApi(@FieldMap params: Map<String, String>): ApiResponse<EmptyBean>

    @Multipart
    @Streaming
    @POST("evidences/upload")
    suspend fun getUploadApi(@Part partList: List<MultipartBody.Part>): ApiResponse<EmptyBean>

    @FormUrlEncoded
    @POST("evidences/noticeLack")
    suspend fun getNoticeLackApi(@FieldMap params: Map<String, String>): ApiResponse<EmptyBean>

}