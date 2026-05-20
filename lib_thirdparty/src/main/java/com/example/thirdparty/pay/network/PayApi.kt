package com.example.thirdparty.pay.network

import com.example.common.network.factory.RetrofitFactory
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.thirdparty.pay.bean.BalanceBean
import com.example.thirdparty.pay.bean.PayBean
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * author:wyb
 * 充值接口类
 */
interface PayApi {

    companion object {
        val instance by lazy { RetrofitFactory.instance.createByServer(PayApi::class.java) }
    }

    /**
     * 获取充值接口
     */
    @POST("swallow/recharge/pay")
    suspend fun getRechargeApi(@Body requestBody: RequestBody): ApiResponse<PayBean>

    /**
     * 获取继续支付接口
     */
    @POST("swallow/recharge/pay/{orderNo}")
    suspend fun getOrderPayApi(@Path("orderNo") orderNo: String, @Body requestBody: RequestBody): ApiResponse<PayBean>

    /**
     * 获取保全币充值接口
     */
    @PUT("swallow/attestation/site/pay/{attestationId}")
    suspend fun getSitePayApi(@Path("attestationId") attestationId: String): ApiResponse<EmptyBean>

    /**
     * 获取用户余额（包含套餐，余额）
     */
    @GET("swallow-user/account/price/info")
    suspend fun getBalanceApi(): ApiResponse<BalanceBean>
}