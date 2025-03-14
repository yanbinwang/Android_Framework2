package com.example.common.utils.manager

import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap

/**
 * 管理对应页面所有的job对象
 */
class JobManager {
    //用于存储协程 Job 的线程安全集合
    private val jobMap by lazy { ConcurrentHashMap<String, Job>() }

    companion object {
        /**
         * 自定义注解
         */
        @Retention(AnnotationRetention.RUNTIME)
        @Target(AnnotationTarget.VALUE_PARAMETER)
        annotation class CallerFunctionName

        /**
         * 内联函数获取调用者方法名
         * val methodName = getCallerMethodName()
         */
        inline fun getCallerMethodName(@CallerFunctionName callerFunction: String = ""): String {
            return callerFunction
        }
    }

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