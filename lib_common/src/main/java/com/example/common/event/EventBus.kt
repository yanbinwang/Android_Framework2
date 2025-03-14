package com.example.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.manager.JobManager
import com.example.common.utils.toJson
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.cancellation.CancellationException

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class EventBus private constructor() {
    //全局热流
    private val busDefault by lazy { MutableSharedFlow<Event>() }
    //存储所有订阅协程，每个 LifecycleOwner 独立
    private val busSubscriber by lazy { ConcurrentHashMap<WeakReference<LifecycleOwner>, ConcurrentLinkedQueue<Job>>() }
    //对应页面post的job
    private val eventManager by lazy { JobManager() }

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

    /**
     * 无需手动 unregister，依赖 LifecycleOwner 的 scope 自动取消
     */
    fun register(owner: LifecycleOwner, onReceive: (event: Event) -> Unit) {
        eventManager.addObserver(owner)
        val weakOwner = WeakReference(owner)
        val jobs = busSubscriber.getOrPut(weakOwner) { ConcurrentLinkedQueue() }
        val job = owner.lifecycleScope.launch {
            try {
                busDefault.collect { event ->
                    onReceive(event)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
        jobs.add(job)
        //监听 LifecycleOwner 的销毁事件
        owner.doOnDestroy {
            busSubscriber.entries.find { it.key.get() == owner }?.let { entry ->
                entry.value.forEach { it.cancel() }
                busSubscriber.remove(entry.key)
            }
        }
    }

    /**
     * emit 是一个挂起函数，如果缓冲区已满，它会挂起当前协程，直到有空间可用。
     * tryEmit 是非挂起函数，它会尝试立即发射数据。如果缓冲区已满，它会返回 false。
     */
    fun post(event: Event) {
        busSubscriber.forEach { (owner, _) ->
            val job = owner.get()?.lifecycleScope?.launch {
                try {
                    busDefault.emit(event)
                } catch (e: Exception) {
                    handleException(e)
                }
            }
            job?.let {
                eventManager.manageJob(it, event.toJson().orEmpty())
            }
        }
    }

    /**
     * 全局异常处理
     */
    private fun handleException(e: Exception) {
        if (e is CancellationException) {
            // 协程被取消，无需处理
        } else {
            // 可以根据不同的异常类型进行不同的处理
            e.printStackTrace()
        }
    }

}