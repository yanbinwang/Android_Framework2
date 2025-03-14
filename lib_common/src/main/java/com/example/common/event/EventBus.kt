package com.example.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件类
 */
class EventBus private constructor() {
    /**
     * 全局热流，设置缓冲区大小和溢出策略
     *
     * replay
     * 参数指定了 MutableSharedFlow 会向新的收集器重放多少个之前发射的值。也就是说，当一个新的协程开始收集这个 SharedFlow 时，它会立即接收到最近 replay 数量的发射值
     * 如果 replay 设置为 0，新的收集器不会接收到任何之前发射的值，只会从它开始收集的那一刻起接收新发射的值
     * 如果 replay 设置为大于 0 的值，例如 replay = 2，新的收集器会立即接收到最近发射的 2 个值
     *
     * extraBufferCapacity
     * 参数指定了 MutableSharedFlow 除了 replay 数量之外的额外缓冲区容量。当发射值的速度超过收集值的速度时，这些额外的缓冲区可以暂时存储发射的值
     * 如果 extraBufferCapacity 设置为 0，当没有可用的收集器时，发射协程会立即挂起，直到有收集器可以接收值
     * 如果 extraBufferCapacity 设置为大于 0 的值，例如 extraBufferCapacity = 10，当没有可用的收集器时，最多可以有 10 个值存储在缓冲区中，发射协程不会立即挂起，直到缓冲区满
     *
     * onBufferOverflow
     * 参数指定了当缓冲区（replay 缓冲区和 extraBufferCapacity 缓冲区）满时的处理策略。它是一个枚举类型，有以下几种取值：
     * BufferOverflow.SUSPEND：当缓冲区满时，发射协程会挂起，直到缓冲区有空间可用。
     * BufferOverflow.DROP_OLDEST：当缓冲区满时，会丢弃最旧的值，然后继续发射新的值。
     * BufferOverflow.DROP_LATEST：当缓冲区满时，会丢弃最新的值，继续等待缓冲区有空间
     */
    private val busDefault by lazy { MutableSharedFlow<Event>(0, 10, BufferOverflow.SUSPEND) }
    //存储所有订阅协程，每个 LifecycleOwner 独立
    private val busSubscriber by lazy { ConcurrentHashMap<LifecycleOwner, Job>() }
    //当前页面持有的消息协程
    private val busManager by lazy { AtomicReference(CopyOnWriteArrayList<Job>()) }

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }
    }

    /**
     * 无需手动 unregister，依赖 LifecycleOwner 的 scope 自动取消
     */
    fun register(owner: LifecycleOwner, onReceive: (event: Event) -> Unit) {
        val newJob = owner.lifecycleScope.launch {
            try {
                busDefault.collect { event ->
                    onReceive(event)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
        busSubscriber[owner] = newJob
        owner.doOnDestroy {
            //删除自身订阅
            busSubscriber[owner]?.cancel()
            busSubscriber.remove(owner)
            //删除所有post时创建的协程，保证每次一个页面销毁时，整一个发送的消息栈是干净的
            busManager.get().forEach { it.cancel() }
            busManager.get().clear()
        }
    }

    /**
     * emit 是一个挂起函数，如果缓冲区已满，它会挂起当前协程，直到有空间可用。
     * tryEmit 是非挂起函数，它会尝试立即发射数据。如果缓冲区已满，它会返回 false。
     */
    fun post(event: Event) {
        busManager.get().add(GlobalScope.launch(Main) {
            try {
                busDefault.emit(event)
            } catch (e: Exception) {
                handleException(e)
            }
        })
    }

    /**
     * 整体异常处理
     */
    private fun handleException(e: Exception) {
        when (e) {
            is CancellationException -> {
                //协程被取消，无需处理
            }
            else -> {
                //其他异常处理，例如记录日志
                e.printStackTrace()
            }
        }
    }

}