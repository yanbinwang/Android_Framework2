package com.example.thirdparty.pay.subscribe

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
    @POST("swallow/recharge/pay")
    suspend fun getRechargeApi(@Body requestBody: RequestBody): ApiResponse<PayBean>

    @POST("swallow/recharge/pay/{orderNo}")
    suspend fun getOrderPayApi(@Path("orderNo") orderNo: String, @Body requestBody: RequestBody): ApiResponse<PayBean>

    @PUT("swallow/attestation/site/pay/{attestationId}")
    suspend fun getSitePayApi(@Path("attestationId") attestationId: String): ApiResponse<EmptyBean>

    @GET("swallow-user/account/price/info")
    suspend fun getBalanceApi(): ApiResponse<BalanceBean>
}