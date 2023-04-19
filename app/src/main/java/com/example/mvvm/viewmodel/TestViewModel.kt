package com.example.mvvm.viewmodel

import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.network.repository.request
import com.example.common.subscribe.Subscribe
import com.example.framework.utils.function.value.safeGet
import kotlinx.coroutines.awaitAll

/**
 * 串行/并发是否需要dialog需要主动调取，单纯一次性发起不需要
 */
class TestViewModel : BaseViewModel() {

    /**
     * 串行
     * task1/task2按照顺序依次执行
     */
    fun serialTask() {
        launch {
            view?.showDialog()
            val task1 = request({ Subscribe.getVerificationApi(mapOf("key" to "value")) })
            val task2 = request({ Subscribe.getVerificationApi(mapOf("key" to "value")) })
            view?.hideDialog()
        }
    }

    /**
     * 并发
     * task1/task2一起执行(req2会稍晚一点执行“被挂起的时间”)
     */
    fun concurrencyTask() {
        launch {
            view?.showDialog()
            val task1 = async({ Subscribe.getVerificationApi(mapOf("key" to "value")) })
            val task2 = async({ Subscribe.getVerificationApi(mapOf("key" to "value")) })
            val taskList = awaitAll(task1, task2)
            view?.hideDialog()
            taskList.safeGet(0)
            taskList.safeGet(1)
        }
    }

    /**
     * 普通一次性
     */
    fun task() {
        launch({ Subscribe.getVerificationApi(mapOf("key" to "value")) },{
            //拿对象
        })
    }

    /**
     * 普通一次性（并不主动发起）
     */
    fun taskAsync() {
        launch {
            val task1 = async({ Subscribe.getVerificationApi(mapOf("key" to "value")) })
            task1.await()//不调取await不会执行接口请求
        }
    }
}