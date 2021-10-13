package com.example.mvvm.bridge

import android.app.ProgressDialog.show
import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.http.repository.HttpParams
import com.example.common.http.repository.launch
import com.example.common.subscribe.CommonSubscribe.getTestApi
import com.example.common.subscribe.CommonSubscribe.getVerificationApi
import com.example.mvvm.model.UserInfoModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * Created by WangYanBin on 2020/6/3.
 */
class LoginViewModel : BaseViewModel() {
    val userInfoData by lazy { MutableLiveData<UserInfoModel>() }//接口得到的用户对象，泛型string也可替换为对象
    val testData by lazy { MutableLiveData<Any>() }
    val errorData by lazy { MutableLiveData<String>() }

    fun getData() {
        for (i in 0..999) {
            launch {
                val any = getTestApi().data
//                CommonSubscribe.apply {
//
//                    apiCall(getTestApi(), object : HttpSubscriber<Any> {
//
//                        override fun onSuccess(data: Any?) {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onFailed(e: Throwable?, msg: String?) {
//                            TODO("Not yet implemented")
//                        }
//
//                    })
//
//                }

//                apiCall(getTestApi(), object : HttpSubscriber<Any> {
//
//                    override fun onSuccess(data: Any?) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onFailed(e: Throwable?, msg: String?) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
//                getView()?.log("onComplete：当前第" + i + "个请求结束！")
            }
        }
    }

    fun login(account: String?, password: String?) {

        launch {
//            //串行执行
//            val token = getTestApi()
//            val profile = getVerificationApi(HttpParams().append("12", token.data.toString()).map)
            //并行执行
//            val profile = async { getTestApi() }
//            val articles = async { getVerificationApi(HttpParams().append("12", "11").map) }
//            awaitAll(profile, articles)
//            val profile = async { getTestApi() }.await()
//            val articles = async { getVerificationApi(HttpParams().append("12", "11").map) }.await()
//            check(profile,articles)

//            //串行执行
//            val token = async { getTestApi() }
//            val profile = async { getVerificationApi(HttpParams().append("12", token.await().toString()).map) }.await()
//            //并行执行
//            val profile = async { getTestApi() }
//            val articles = async { getVerificationApi(HttpParams().append("12", "11").map) }
//            awaitAll(profile, articles)


        }

        getView()?.showDialog()
        getView()?.showToast("当前执行了登录\n账号：$account\n密码：$password")
        getView()?.hideDialog()

        //do网络请求-将对象传递给下一个页面，或者直接当前页面处理，处理和mvc写法一致(失败的处理直接回调对应观察的数据,或者在baseviewmodel中处理)
        val model = UserInfoModel("老王万寿无疆", 88, "bilibili")
        userInfoData.postValue(model)
    }

//    private fun check(any: ApiResponse<Any>?, any2: ApiResponse<Any>?){
//        if(0==any?.code){
//
//        }
//    }

}