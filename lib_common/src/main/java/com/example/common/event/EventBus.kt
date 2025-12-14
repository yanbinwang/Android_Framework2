package com.example.common.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * author: wyb
 * date: 2018/4/16.
 * 传递事件/接收数据类
 * @Synchronized 注解是对整个方法进行同步，相当于在方法体前添加 synchronized(this) 块，它会将整个方法的执行作为一个临界区，同一时间只有一个线程能够执行该方法。
 * 而 synchronized(postLock) 可以将同步的范围缩小到只对需要同步的代码块进行加锁，提高了代码的并发度和性能。
 * 综上所述，在 synchronized(postLock) 中传入一个 Any 类型的对象作为锁，是为了实现同步机制，确保在多线程环境下对共享资源的访问是线程安全的。
 * 通过使用自定义的锁对象，可以灵活控制同步的范围，提高代码的性能和并发度。
 * private val postLock by lazy { Any() }
 * synchronized(postLock) {
 *     //每个 post 方法调用都会创建一个独立的协程作用域
 *     val scope = CoroutineScope(SupervisorJob() + Main) // 独立作用域
 *     scope.launch {
 *         try {
 *             busDefault.emit(event)
 *         } catch (e: Exception) {
 *             handleException(e)
 *         }
 *     }.invokeOnCompletion {
 *         scope.cancel()//协程结束自动清理作用域
 *     }
 * }
 */
class EventBus private constructor() {
    /**
     * 全局复用的消息发送协程作用域，切记不可随意cancel
     * eventDispatchScope 使用 SupervisorJob() 作为父作业，SupervisorJob 不会因为某个子协程的失败而取消其他子协程，
     * 每个子协程的生命周期是相对独立的。当一个子协程完成（正常结束或因异常结束）后，它会自动释放相关资源，不会影响其他子协程
     * 协程自动清理机制：协程在执行完毕后会自动释放其占用的资源，包括内存。即使有多个 post 操作在 eventDispatchScope 中并发执行，每个协程完成后都会被清理，不会造成内存累积
     */
    private val eventDispatchScope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }
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
    private val eventFlow by lazy { MutableSharedFlow<Event>(0, 10, BufferOverflow.SUSPEND) }
    /**
     * 存储事件订阅的协程任务，按LifecycleOwner管理
     */
    private val subscriptionJobs by lazy { ConcurrentHashMap<LifecycleOwner, Job>() }
    /**
     * 存储状态收集的协程任务，按LifecycleOwner管理
     */
    private val collectionJobs by lazy { ConcurrentHashMap<LifecycleOwner, Job>() }

    companion object {
        @JvmStatic
        val instance by lazy { EventBus() }

        /**
         * 强制发射新值，即使数据与之前相同也会触发回调
         * fun <T> MutableStateFlow<ForceWrapper<T>>.forceEmit(value: T) {
         *     this.value = ForceWrapper(value) // 每次都是新对象，必然触发回调
         * }
         * 原始StateFlow直接定义为包装类类型（初始化时用空数据+初始tag）
         * private val _listFlow = MutableStateFlow(ForceWrapper(emptyList<String>()))
         * val listFlow: StateFlow<ForceWrapper<List<String>>> = _listFlow.asStateFlow()
         * 使用示例
         * fun updateList(newList: List<String>) {
         *     // 普通更新（需手动判断是否去重）
         *     if (_listFlow.value.data != newList) {
         *         _listFlow.value = ForceWrapper(newList)
         *     }
         *
         *     // 强制更新（即使newList相同，也触发回调）
         *     _listFlow.forceEmit(newList)
         * }
         * 订阅时：通过 .data 获取实际业务数据
         * listFlow.collect { wrapper ->
         *     val realList = wrapper.data // 只关注业务数据，忽略uniqueTag
         *     updateUI(realList)
         * }
         */
        fun <T> MutableStateFlow<ForceWrapper<T>>.forceEmit(value: T?) {
            this.value = ForceWrapper(value)
        }

        /**
         * 收集StateFlow时跳过初始值，只处理后续更新
         * MutableStateFlow第一次collect的时候就会返回默认值,加一层过滤
         * @param action 处理后续值的回调（不会收到第一个值）
         */
        suspend fun <T> StateFlow<T>.collectIn(action: suspend (T) -> Unit) {
            this.withIndex().collect { (index, value) ->
                // 只处理索引≠0的值（即跳过第一个）
                if (index != 0) {
                    action(value)
                }
            }
        }

        /**
         * 在生命周期感知下收集StateFlow的更新
         * 自动根据生命周期状态暂停/恢复收集
         */
        fun <T> StateFlow<T>.collectInLifecycle(
            lifecycleOwner: LifecycleOwner,
            minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
            action: (T) -> Unit
        ): Job {
            return lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(minActiveState) {
//            this@collectIn.collect(action)
                    this@collectInLifecycle.collectIn(action)
                }
            }
        }
    }

    /**
     * SharedFlow实现广播订阅
     * 无需手动 unregister，依赖 LifecycleOwner 的 scope 自动取消
     */
    fun subscribe(owner: LifecycleOwner, onReceive: (event: Event) -> Unit) {
        val newJob = owner.lifecycleScope.launch {
            try {
                eventFlow.collect { event ->
                    onReceive(event)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
        subscriptionJobs[owner] = newJob
        owner.doOnDestroy {
            // 删除自身订阅
            // ConcurrentHashMap.remove(owner) 做了两件事：
            // 1)从 map 中删除 owner 对应的键值对（即移除这个订阅任务的记录）
            // 2)返回被删除的 Job 实例（如果存在的话）
            subscriptionJobs.remove(owner)?.cancel()
        }
    }

    /**
     * StateFlow实现数据接收
     * 在一个协程中收集多个StateFlow，受生命周期管理
     * // viewModel
     * private val _age = MutableStateFlow(0)
     * val age = _age.asStateFlow()
     * suspend fun updateAge() {
     *     // _age.value = Random.nextInt(0, 100)
     *     // _age.emit(1)
     * }
     * // 页面
     * launch{
     *     viewmodel.age.collect {}
     * }
     */
    fun collect(owner: LifecycleOwner, onReceive: suspend CoroutineScope.() -> Unit) {
        val newJob = owner.lifecycleScope.launch {
//            /**
//             * 指定一个生命周期状态（如 STARTED、RESUMED 等），作为 “协程激活” 的最低门槛。
//             * 例如 minActiveState = Lifecycle.State.STARTED 时，只有当页面（Activity/Fragment）处于 STARTED 或 RESUMED 状态时，协程才会执行；
//             * 当页面退到后台（如 onStop 调用，生命周期变为 STOPPED），协程会自动暂停；
//             * 当页面重新回到前台（如 onStart 调用，生命周期回到 STARTED），协程会重新启动并继续执行
//             */
//            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                onReceive()
            } catch (e: Exception) {
                handleException(e)
            }
//            }
        }
        collectionJobs[owner] = newJob
        owner.doOnDestroy {
            collectionJobs.remove(owner)?.cancel()
        }
    }

    /**
     * emit 是一个挂起函数，如果缓冲区已满，它会挂起当前协程，直到有空间可用。
     * tryEmit 是非挂起函数，它会尝试立即发射数据。如果缓冲区已满，它会返回 false。
     */
    fun post(event: Event) {
        eventDispatchScope.launch {
            try {
                eventFlow.emit(event)
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

/**
 * 强制更新包装类，通过纳秒时间戳确保每次实例唯一
 */
data class ForceWrapper<T>(
    val data: T?,
    private val uniqueTag: Long = System.nanoTime() // 唯一标识，确保equals为false
)