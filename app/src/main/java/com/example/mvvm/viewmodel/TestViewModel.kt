package com.example.mvvm.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.async
import com.example.common.base.bridge.launch
import com.example.common.network.CommonApi
import com.example.common.network.repository.request
import com.example.common.network.repository.safeAs
import com.example.common.network.repository.withHandling
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking

/**
 * 串行/并发是否需要dialog需要主动调取，单纯一次性发起不需要
 * private fun refreshNow() {
 *         lastRefreshTime = currentTimeNano
 *         getPageInfo(mRefresh?.autoRefreshAnimationOnly().orFalse)
 *     }
 *
 *     private fun getPageInfo(isAuto: Boolean = false) {
 *         launch {
 *             if (isAuto) delay(300)
 *             flow {
 *                 emit(request({ CommonApi.instance.getUserInfoApi() }))
 *             }.withHandling(end = {
 *                 reset(false)
 *                 location.postValue(Unit)
 *             }).collect {
 *                 AccountHelper.refresh(it)
 *             }
 *         }.manageJob()
 *     }
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

//    sealed class PageInfoResult {
//        data class DealDetailResult(val bean: DealBean?) : PageInfoResult()
//        data class PaymentListResult(val list: List<PaymentBean>?) : PageInfoResult()
//    }
//
//    fun getPageInfo(orderId: String?) {
//        launch {
//            flow {
//                //详情页数据
//                val dealDetailAsync = async {
//                    request({ OrderSubscribe.getDealDetailApi(reqBodyOf("orderId" to orderId)) }).apply {
//                        PageInfoResult.DealDetailResult(this)
//                    }
//                }
//                //底部筛选支付数据
//                val paymentListAsync = async {
//                    requestLayer({ OrderSubscribe.getPaymentListApi() }).data.apply {
//                        PageInfoResult.PaymentListResult(this)
//                    }
//                }
//                //并行发起
//                val asyncList = awaitAll(dealDetailAsync, paymentListAsync)
//                //发射数据
//                emit(asyncList)
//            }.withHandling({
//                //轮询失败直接报错遮罩，并且停止轮询倒计时
//                error()
//                reason.postValue(null)
//            }).collect {
//                reset(false)
//                var bean: DealBean? = null
//                var list: List<PaymentBean>? = null
//                it.forEach { result ->
//                    when (result) {
//                        is PageInfoResult.DealDetailResult -> bean = result.bean
//                        is PageInfoResult.PaymentListResult -> list = result.list
//                    }
//                }
//                //后端坑，没详情数据还返回成功，故而增加后续判断
//                if (null != bean && list.safeSize > 0) {
//                    pageInfo.postValue(DealBundle(bean, list))
//                }
//            }
//        }.manageJob()
//    }


    /**
     * 串行
     * task1/task2按照顺序依次执行
     */
    fun serialTask() {
        launch {
            flow {
                val task1 = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
                val task2 = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
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
                    async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                val task2 =
                    async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })?.apply { } }
                emit(awaitAll(task1, task2))
            }.withHandling(mView).collect {
                it.safeAs<Any>(0)
                it.safeAs<Any>(1)
            }
        }
    }

    private fun getUserDataAsync(): Deferred<Any?> {
        return async { request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) }) }
    }

    /**
     * 普通一次性
     */
    fun task() {
        flow<Unit> {
            //拿对象
            val bean = request({ CommonApi.instance.getVerificationApi(mapOf("key" to "value")) })
        }.withHandling().launchIn(viewModelScope)
    }

}