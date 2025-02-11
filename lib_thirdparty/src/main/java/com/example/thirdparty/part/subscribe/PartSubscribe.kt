package com.example.thirdparty.part.subscribe

import com.example.common.network.factory.RetrofitFactory
import okhttp3.MultipartBody

/**
 * author:wyb
 * 分片接口类
 */
object PartSubscribe : PartApi {
    private val partApi by lazy { RetrofitFactory.instance.create(PartApi::class.java) }

    /**
     *  获取分片上传
     */
    override suspend fun getPartUploadApi(partList: List<MultipartBody.Part>) = partApi.getPartUploadApi(partList)

    /**
     *  获取告知分片完成
     */
    override suspend fun getPartCombineApi(params: Map<String, String>) = partApi.getPartCombineApi(params)

    /**
     * 获取完整文件上传
     */
    override suspend fun getUploadApi(partList: List<MultipartBody.Part>) = partApi.getUploadApi(partList)

    /**
     * 获取证据缺失
     */
    override suspend fun getNoticeLackApi(params: Map<String, String>) = partApi.getNoticeLackApi(params)

}