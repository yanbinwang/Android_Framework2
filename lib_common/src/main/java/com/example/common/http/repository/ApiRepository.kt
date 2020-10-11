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
suspend fun <T> ApiResponse<T>.apiCall(subscriber: HttpSubscriber<T>?): ApiResponse<T> =
    call(subscriber)

//请求监听扩展
suspend fun <T> T.call(resourceSubscriber: ResourceSubscriber<T>?): T {
    resourceSubscriber?.onStart()
    try {
        val res: T? = execute { this }
        res?.let { resourceSubscriber?.onNext(it) }
    } catch (e: Exception) {
        resourceSubscriber?.onError(e)
    } finally {
        resourceSubscriber?.onComplete()
    }
    return this
}

//切换io线程，获取请求的对象
private suspend fun <T> execute(block: () -> T): T = withContext(IO) { block() }