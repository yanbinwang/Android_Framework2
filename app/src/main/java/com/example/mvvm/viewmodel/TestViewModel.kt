package com.example.mvvm.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.async
import com.example.common.base.bridge.launch
import com.example.common.network.repository.request
import com.example.common.network.repository.withHandling
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.value.safeAs
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * 串行/并发是否需要dialog需要主动调取，单纯一次性发起不需要
 */
class TestViewModel : BaseViewModel() {
//    val token by lazy { MutableLiveData<String?>() }
    /**
     * 1、flow数据处理分StateFlow和SharedFlow，后者适合事件流或多值发射，网络请求用前者
     * 2、StateFlow需要声明默认值，并且和协程高度重合
     * MutableStateFlow 本身是热流（Hot Flow），其生命周期独立于订阅者。
     * 但实际使用中需通过 协程作用域（如 lifecycleScope 或 viewModelScope）启动流收集，以便在组件销毁时自动取消协程：
     * kotlin
     * // 在 Activity/Fragment 中使用 lifecycleScope
     * lifecycleScope.launch {
     *     viewModel.uiState.collect { state ->
     *         // 更新 UI
     *     }
     * } // 组件销毁时自动取消协程[2](@ref)
     */
//    val token by lazy { MutableStateFlow("") }

    //用于存储协程 Job 的线程安全集合
    private val jobMap = ConcurrentHashMap<String, Job>()

    //管理协程 Job 的方法
    private fun manageJob(job: Job,key: String = getCallerMethodName()) {
        //如果之前的 Job 存在，取消并从集合中移除
        jobMap[key]?.let {
            it.cancel()
            jobMap.remove(key)
        }
        //新的 Job 添加到集合中
        jobMap[key] = job
    }

    // 自定义注解
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

    override fun onCleared() {
        super.onCleared()
        // 清除所有 Job
        jobMap.values.forEach { it.cancel() }
        jobMap.clear()
    }


    /**
     * 串行
     * task1/task2按照顺序依次执行
     */
    fun serialTask() {
        launch {
            flow {
                val task1 = request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
                val task2 = request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
                emit(Unit)
            }.withHandling(mView, {
                //每个请求如果失败了都会回调当前的err监听
            }).collect {

            }
//            /**
//             * 将一个监听回调的处理变为挂起函数的形式
//             * suspendCoroutine<T>---》T为返回的类型
//             * it.resume()---》回调的时候调取该方法，用于嵌套旧的一些api
//             */
//            suspendCoroutine {
////                it.resume()
//                //加try/catch接受
////                it.resumeWithException()
//            }
//            /**
//             * 区别于suspendCoroutine，代表此次转换的挂机方法是能够被cancel的
//             */
//            suspendCancellableCoroutine {
//
//            }
        }
        /**
         * 区别于常规协程，不需要类实现CoroutineScope，并且会阻塞当前线程
         * 在代码块中的逻辑执行完后才会执行接下来的代码
         */
        runBlocking { }
    }

    /**
     * 并发
     * task1/task2一起执行(req2会稍晚一点执行“被挂起的时间”)
     */
    fun concurrencyTask() {
        launch {
            flow {
                val task1 =
                    async { request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                val task2 =
                    async { request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                val taskList = awaitAll(task1, task2)
                emit(taskList)
            }.withHandling(mView).collect {
                it.safeAs<Any>(0)
                it.safeAs<Any>(1)
            }
        }
    }

    private fun getUserDataAsync(): Deferred<Any?> {
        return async { request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) }) }
    }

    /**
     * 普通一次性
     */
    fun task() {
        flow<Unit> {
            //拿对象
            val bean = request({ CommonSubscribe.getVerificationApi(mapOf("key" to "value")) })
        }.withHandling().launchIn(viewModelScope)
    }

}