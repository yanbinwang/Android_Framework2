package com.example.common.http.repository

import androidx.lifecycle.viewModelScope
import com.example.common.base.bridge.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败)
 */
//ViewModel的KTX库中具备扩展函数，但不能像继承CoroutineScope那样直接launch点出，这里再做一个扩展
fun BaseViewModel.launch(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch {
        block()
    }

//项目请求监听扩展
suspend fun <T> ApiResponse<T>.apiCall(subscriber: HttpSubscriber<T>?) = call(subscriber)

//请求监听扩展
suspend fun <T> T.call(resourceSubscriber: ResourceSubscriber<T>?) {
    val t = this
    resourceSubscriber?.onStart()
    try {
        val res: T? = withContext(IO) { t }
        res?.let {
            resourceSubscriber?.onNext(it)
        }
    } catch (e: Exception) {
        resourceSubscriber?.onError(e)
    } finally {
        resourceSubscriber?.onComplete()
    }
}


//object ApiRepository {
//
//    suspend fun <T> call(request: T?, resourceSubscriber: ResourceSubscriber<T>?) {
//        resourceSubscriber?.onStart()
//        try {
//            val res: T? = withContext(IO) { request }
//            res?.let {
//                resourceSubscriber?.onNext(it)
//            }
//        } catch (e: Exception) {
//            resourceSubscriber?.onError(e)
//        } finally {
//            resourceSubscriber?.onComplete()
//        }
//    }
//
//    suspend fun <T> apiCall(request: ApiResponse<T>?, subscriber: HttpSubscriber<T>?) {
//        call(request, subscriber)
//    }
//
//}