package com.example.common.subscribe

import androidx.lifecycle.LiveData
import com.example.common.http.callback.ApiResponse
import com.example.common.http.factory.RetrofitFactory
import com.example.common.model.UploadModel
import okhttp3.MultipartBody

/**
 * author:wyb
 * 通用接口类
 */
object BaseSubscribe : BaseApi {
    private val baseApi = RetrofitFactory.instance.create(BaseApi::class.java)

    override fun download(downloadUrl: String): LiveData<okhttp3.ResponseBody> {
        return baseApi.download(downloadUrl)
    }

    override fun getUploadFile(agent: String, partList: List<MultipartBody.Part>): LiveData<ApiResponse<UploadModel>> {
        TODO("Not yet implemented")
    }

    override fun getSendVerification(agent: String, map: Map<String, String>): LiveData<ApiResponse<Any>> {
        return baseApi.getSendVerification(agent, map)
    }

    override fun getVerification(agent: String, map: Map<String, String>): LiveData<ApiResponse<Any>> {
        return baseApi.getVerification(agent, map)
    }

    override fun getTestApi(): LiveData<ApiResponse<Any>> {
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
