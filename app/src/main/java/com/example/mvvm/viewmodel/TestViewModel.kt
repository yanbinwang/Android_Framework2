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
        }
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
            val task1 = async(req, { CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
            val task2 = async(req, { CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
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