package com.example.common.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class EventBus private constructor() {
    //全局热流
    private val busDefault by lazy { MutableSharedFlow<Event>() }
    //存储所有订阅协程，每个 LifecycleOwner 独立
    private val busSubscriber by lazy { AtomicReference(ArrayList<Job>()) }

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

    /**
     * 无需手动 unregister，依赖 LifecycleOwner 的 scope 自动取消
     */
    fun register(scope: CoroutineScope, onReceive: (event: Event) -> Unit) {
        val job = scope.launch {
            try {
                busDefault.collect { event ->
                    onReceive(event)
                }
            } catch (e: Exception) {
                //处理异常，例如记录日志
                e.printStackTrace()
            }
        }
        busSubscriber.get() += job
        //协程结束时自动移除（通过 finally）
        job.invokeOnCompletion {
            busSubscriber.get().remove(job)
        }
    }

    /**
     * emit 是一个挂起函数，如果缓冲区已满，它会挂起当前协程，直到有空间可用。
     * tryEmit 是非挂起函数，它会尝试立即发射数据。如果缓冲区已满，它会返回 false。
     */
    fun post(event: Event) {
        busDefault.tryEmit(event)
    }

}