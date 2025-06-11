package com.example.common.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
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
    /**
     * 存储所有订阅协程，每个 LifecycleOwner 独立
     */
    private val busSubscriber by lazy { ConcurrentHashMap<LifecycleOwner, Job>() }

    /**
     * 全局复用的消息发送协程作用域，切记不可随意cancel
     * SupervisorJob 的特性：postScope 使用 SupervisorJob() 作为父作业，SupervisorJob 不会因为某个子协程的失败而取消其他子协程，
     * 每个子协程的生命周期是相对独立的。当一个子协程完成（正常结束或因异常结束）后，它会自动释放相关资源，不会影响其他子协程
     *
     * 协程自动清理机制：协程在执行完毕后会自动释放其占用的资源，包括内存。即使有多个 post 操作在 postScope 中并发执行，每个协程完成后都会被清理，不会造成内存累积
     */
    private val postScope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }
//    /**
//     * @Synchronized 注解是对整个方法进行同步，相当于在方法体前添加 synchronized(this) 块，它会将整个方法的执行作为一个临界区，同一时间只有一个线程能够执行该方法。
//     * 而 synchronized(postLock) 可以将同步的范围缩小到只对需要同步的代码块进行加锁，提高了代码的并发度和性能。
//     * 综上所述，在 synchronized(postLock) 中传入一个 Any 类型的对象作为锁，是为了实现同步机制，确保在多线程环境下对共享资源的访问是线程安全的。
//     * 通过使用自定义的锁对象，可以灵活控制同步的范围，提高代码的性能和并发度。
//     */
//    private val postLock by lazy { Any() }

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
        }
    }

    /**
     * emit 是一个挂起函数，如果缓冲区已满，它会挂起当前协程，直到有空间可用。
     * tryEmit 是非挂起函数，它会尝试立即发射数据。如果缓冲区已满，它会返回 false。
     */
    fun post(event: Event) {
//        synchronized(postLock) {
//            //每个 post 方法调用都会创建一个独立的协程作用域
//            val scope = CoroutineScope(SupervisorJob() + Main) // 独立作用域
//            scope.launch {
//                try {
//                    busDefault.emit(event)
//                } catch (e: Exception) {
//                    handleException(e)
//                }
//            }.invokeOnCompletion {
//                scope.cancel()//协程结束自动清理作用域
//            }
//        }
        postScope.launch {
            try {
                busDefault.emit(event)
            } catch (e: Exception) {
                handleException(e)
            }
        }
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