package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.async
import com.example.common.base.bridge.launch
import com.example.common.network.repository.MultiReqUtil
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.value.safeGet
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 串行/并发是否需要dialog需要主动调取，单纯一次性发起不需要
 */
class TestViewModel : BaseViewModel() {
//    val token by lazy { MutableLiveData<String?>() }

    /**
     * 串行
     * task1/task2按照顺序依次执行
     */
    fun serialTask() {
        launch {
            //每个请求如果失败了都会回调当前的err监听
            val req = MultiReqUtil(mView, err = {

            })
            val task1 = req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
            val task2 = req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
            req.end()
//            /**
//             * 将一个监听回调的处理变为挂起函数的形式
//             * suspendCoroutine<T>---》T为返回的类型
//             * it.resume()---》回调的时候调取该方法，用于嵌套旧的一些api
//             */
//            suspendCoroutine {
////                it.resume()
//                //加try/catch接受
////                it.resumeWithException()
//            }
//            /**
//             * 区别于suspendCoroutine，代表此次转换的挂机方法是能够被cancel的
//             */
//            suspendCancellableCoroutine {
//
//            }
        }
        /**
         * 区别于常规协程，不需要类实现CoroutineScope，并且会阻塞当前线程
         * 在代码块中的逻辑执行完后才会执行接下来的代码
         */
        runBlocking {  }
    }

    /**
     * 并发
     * task1/task2一起执行(req2会稍晚一点执行“被挂起的时间”)
     */
    fun concurrencyTask() {
        launch {
//            val req = MultiReqUtil(view)
//            val task1 = getUserDataAsync(req)
//            val task2 = getUserDataAsync(req)
//            val taskList = awaitAll(task1, task2)
//            req.end()
//            taskList.safeGet(0)
//            taskList.safeGet(1)
            val req = MultiReqUtil(mView)
            val task1 = async { req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })?.apply {  } }
            val task2 = async { req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })?.apply {  } }
            val taskList = awaitAll(task1, task2)
            taskList.safeGet(0)
            taskList.safeGet(1)
            req.end()
        }
    }

    private suspend fun getUserDataAsync(req: MultiReqUtil): Deferred<Any?> {
        return async(Dispatchers.Main, CoroutineStart.LAZY) { req.request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) }) }
    }

    /**
     * 普通一次性
     */
    fun task() {
        launch({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) }, {
            //拿对象
        })
    }

}