package com.example.common.subscribe

import com.example.common.http.repository.ApiResponse
import com.example.common.http.factory.RetrofitFactory
import com.example.common.http.repository.ApiRepository
import com.example.common.model.UploadModel
import okhttp3.MultipartBody

/**
 * author:wyb
 * 通用接口类
 */
object ApiSubscribe : BaseApi, ApiRepository() {

    private val baseApi by lazy {
        RetrofitFactory.instance.create(BaseApi::class.java)
    }

    override suspend fun download(downloadUrl: String): okhttp3.ResponseBody {
        return baseApi.download(downloadUrl)
    }

    override suspend fun getUploadFile(agent: String, partList: List<MultipartBody.Part>): ApiResponse<UploadModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getSendVerification(agent: String, map: Map<String, String>): ApiResponse<Any> {
        return apiCall { baseApi.getSendVerification(agent, map) }
    }

    override suspend fun getVerification(agent: String, map: Map<String, String>): ApiResponse<Any> {
        return apiCall { baseApi.getVerification(agent, map) }
    }

    override suspend fun getTestApi(): Any {
        return baseApi.getTestApi()
    }

//    //上传图片接口
//    fun getUploadFile(header: Int, partList: MutableList<MultipartBody.Part>, resourceSubscriber: ResourceSubscriber<BaseBean<UploadBean>>): Disposable {
//        val params = Params().getParams(timestamp)
//        partList.add(MultipartBody.Part.createFormData("timestamp", timestamp))
//        partList.add(MultipartBody.Part.createFormData("param", params["param"] ?: error("")))
//        val flowable = baseApi.getUploadFile(SecurityUtil.buildHeader(header, timestamp), partList)
//        return RetrofitFactory.getInstance().subscribeWith(flowable, resourceSubscriber)
//    }
//
//    //发送短信验证码-600
//    fun getSendVerification(header: Int, params: Params, resourceSubscriber: ResourceSubscriber<BaseBean<Any>>): Disposable {
//        val flowable = baseApi.getSendVerification(SecurityUtil.buildHeader(header, timestamp), params.getParams(timestamp))
//        return RetrofitFactory.getInstance().subscribeWith(flowable, resourceSubscriber)
//    }
//
//    //短信验证码验证-601
//    fun getVerification(header: Int, params: Params, resourceSubscriber: ResourceSubscriber<BaseBean<Any>>): Disposable {
//        val flowable = baseApi.getVerification(SecurityUtil.buildHeader(header, timestamp), params.getParams(timestamp))
//        return RetrofitFactory.getInstance().subscribeWith(flowable, resourceSubscriber)
//    }

}
