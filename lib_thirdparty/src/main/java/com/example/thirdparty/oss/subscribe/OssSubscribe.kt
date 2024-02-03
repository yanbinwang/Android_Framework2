package com.example.thirdparty.oss.subscribe

import com.example.common.network.factory.RetrofitFactory
import okhttp3.RequestBody

/**
 * author:wyb
 * oss接口类
 */
object OssSubscribe : OssApi {
    private val ossApi by lazy { RetrofitFactory.instance.createByServer(OssApi::class.java) }

    /**
     * 获取oss授权token
     */
    override suspend fun getOssTokenApi() = ossApi.getOssTokenApi()

    /**
     * 编辑oss信息（告知服务器文件信息）
     */
    override suspend fun getOssEditApi(attestationId: String, requestBody: RequestBody) = ossApi.getOssEditApi(attestationId, requestBody)

}
