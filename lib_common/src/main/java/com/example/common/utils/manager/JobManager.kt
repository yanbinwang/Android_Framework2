package com.example.common.utils.manager

import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * 管理对应页面所有的job对象
 * val key = object {}.javaClass.enclosingMethod?.name ?: "unknown"
 */
class JobManager(observer: LifecycleOwner?) {
    // 用于存储协程 Job 的线程安全集合
    private val jobMap by lazy { ConcurrentHashMap<String, Job>() }
    // 为每个 MutableStateFlow 存储最近一次的发送任务
    private val mutableStateFlowJobs by lazy { ConcurrentHashMap<MutableStateFlow<*>, Job>() }

    init {
        observer.doOnDestroy {
            destroy()
        }
    }

//    companion object {
//
//        /**
//         * 内联函数，接收一个 Lambda 表达式
//         */
//        inline fun <T> withCallerMethodName(block: (String) -> T): T {
//            // 获取当前堆栈跟踪
//            val stackTrace = Thread.currentThread().stackTrace
//            // 通常，withCallerMethodName 本身会在堆栈中，我们需要获取调用它的方法
//            // 索引 2 通常对应调用 withCallerMethodName 的方法
//            val callerMethodName = if (stackTrace.size > 2) {
//                stackTrace[2].methodName
//            } else {
//                ""
//            }
//            // 执行 Lambda 表达式，并将调用者方法名作为参数传入
//            return block(callerMethodName)
//        }
//    }

    /**
     * 管理协程 Job 的方法
     * key: String = getCallerMethodName()
     */
    fun manageJob(job: Job, key: String) {
        // 如果之前的 Job 存在，取消并从集合中移除
        jobMap[key]?.let {
            it.cancel()
            jobMap.remove(key)
        }
        //新的 Job 添加到集合中
        jobMap[key] = job
    }

    /**
     * 管理StateFlow发送数据
     */
    fun manageValue(flow: MutableStateFlow<*>, job: Job) {
        // 取消上一次未完成的任务
        mutableStateFlowJobs[flow]?.cancel()
        // 启动新任务并保存引用
        mutableStateFlowJobs[flow] = job
        // 任务完成后移除引用（避免内存泄漏）
        job.invokeOnCompletion {
            mutableStateFlowJobs.remove(flow)
        }
    }

    /**
     * 释放/清除所有 Job
     */
    fun destroy() {
        jobMap.values.forEach { it.cancel() }
        jobMap.clear()
    }

}