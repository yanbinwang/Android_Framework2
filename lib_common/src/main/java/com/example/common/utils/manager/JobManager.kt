package com.example.common.utils.manager

import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap

/**
 * 管理对应页面所有的job对象
 * val key = object {}.javaClass.enclosingMethod?.name ?: "unknown"
 */
class JobManager {
    //用于存储协程 Job 的线程安全集合
    private val jobMap by lazy { ConcurrentHashMap<String, Job>() }

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
        //如果之前的 Job 存在，取消并从集合中移除
        jobMap[key]?.let {
            it.cancel()
            jobMap.remove(key)
        }
        //新的 Job 添加到集合中
        jobMap[key] = job
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    fun addObserver(observer: LifecycleOwner) {
        observer.doOnDestroy {
            destroy()
        }
    }

    /**
     * 释放
     */
    fun destroy() {
        //清除所有 Job
        jobMap.values.forEach {
            it.cancel()
        }
        jobMap.clear()
    }

}