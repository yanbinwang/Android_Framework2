package com.example.thirdparty.pay.subscribe

import com.example.common.network.factory.RetrofitFactory
import okhttp3.RequestBody

/**
 * author:wyb
 * 充值接口类
 */
object PaySubscribe : PayApi {
    private val payApi by lazy { RetrofitFactory.instance.createByServer(PayApi::class.java) }

    /**
     * 获取充值接口
     */
    override suspend fun getRechargeApi(requestBody: RequestBody) = payApi.getRechargeApi(requestBody)

    /**
     * 获取继续支付接口
     */
    override suspend fun getOrderPayApi(orderNo: String, requestBody: RequestBody) = payApi.getOrderPayApi(orderNo, requestBody)

    /**
     * 获取保全币充值接口
     */
    override suspend fun getSitePayApi(attestationId: String) = payApi.getSitePayApi(attestationId)

    /**
     * 获取用户余额（包含套餐，余额）
     */
    override suspend fun getBalanceApi() = payApi.getBalanceApi()

}