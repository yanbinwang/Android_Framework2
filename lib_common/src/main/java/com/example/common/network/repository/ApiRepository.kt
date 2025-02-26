package com.example.common.network.repository

import com.example.common.R
import com.example.common.base.bridge.BaseView
import com.example.common.network.repository.ApiCode.FAILURE
import com.example.common.network.repository.ApiCode.SUCCESS
import com.example.common.network.repository.ApiCode.TOKEN_EXPIRED
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.utils.helper.AccountHelper
import com.example.common.utils.toJson
import com.example.framework.utils.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 网络请求协程扩展-直接获取到对象
 * 如果几个以上的请求，互相之间有关联，则使用当前方法
 * launch {
 *  val task1 = request(1api)
 *  val task2 = request(2api)
 * }
 */
class MultiReqUtil(
    private var view: BaseView? = null,
    private val isShowDialog: Boolean = true,
    private val err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}
) {
    private var results = false//一旦有请求失败，就会为true
    private var loadingStarted = false//是否开始加载

    /**
     * 发起请求
     */
    suspend fun <T> request(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): T? {
        return requestLayer(coroutineScope, err)?.data
    }

    suspend fun <T> requestLayer(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): ApiResponse<T>? {
        if (isShowDialog && !loadingStarted) {
            view?.showDialog()
            loadingStarted = true
        }
        var response: ApiResponse<T>? = null
        requestLayer({ coroutineScope() }, {
            response = it
        }, err, isShowToast = false)
        if (!response.successful()) results = true
        return response
    }

    suspend fun <T> requestAffair(
        coroutineScope: suspend CoroutineScope.() -> T,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): T? {
        if (isShowDialog && !loadingStarted) {
            view?.showDialog()
            loadingStarted = true
        }
        var result: T? = null
        requestAffair({ coroutineScope() }, {
            result = it
        }, {
            results = true
            err.invoke(it)
        }, isShowToast = false)
        return result
    }

//    suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T> {
//        return awaitAll(listOf(*deferreds))
//    }
//
//    suspend fun <T> awaitAll(list: List<Deferred<T>>): List<T> {
//        return list.awaitAll().apply { end() }
//    }

    /**
     * 当串行请求多个接口的时候，如果开发需要知道这多个串行请求是否都成功
     * 在end()被调取之前，可通过当前方法判断
     */
    fun successful(): Boolean {
        return !results
    }

    /**
     * 请求结束主动调取
     */
    fun end() {
        if (isShowDialog) {
            view?.hideDialog()
            loadingStarted = false
        }
        view = null
        results = false
    }

}

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.requestBody() =
    this?.toJson().orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType())

fun reqBodyOf(vararg pairs: Pair<String, Any?>): RequestBody {
    val map = hashMapOf<String, Any>()
    pairs.forEach {
        it.second?.let { v ->
            map[it.first] = v
        }
    }
    return map.requestBody()
}

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseToast() =
    (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.responseNetError) else {
        if (isNullOrEmpty()) resString(R.string.responseError) else this
    }).shortToast()

/**
 * 网络请求协程扩展-并行请求
 * 每个挂起方法外层都会套一个launch
 * requestLayer中已经对body做了处理，直接拿对象返回即可
 * 如果返回格式过于奇葩，放在body层级，则做特殊处理，其余情况不做改进！
 */
suspend fun <T> request(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    requestLayer(coroutineScope, { result ->
        //如果接口是成功的，但是body为空或者后台偷懒没给，我们在写Api时，给一个对象，让结果能够返回
        resp.invoke(result?.data.let {
            if (it is EmptyBean) {
                EmptyBean()
            } else {
                it
            } as? T
        })
    }, err, end, isShowToast)
}

suspend fun <T> requestLayer(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (ApiResponse<T>?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
//    try {
//        log("开始请求")
//        //请求+响应数据
//        withContext(IO) {
//            log("发起请求")
//            coroutineScope()
//        }.let {
//            log("处理结果")
//            if (it.successful()) {
//                resp(it)
//            } else {
//                //如果不是被顶号才会有是否提示的逻辑
//                if (!it.tokenExpired()) if (isShowToast) it.msg.responseToast()
//                //不管结果如何，失败的回调是需要执行的
//                err(Triple(it.code, it.msg, null))
//            }
//        }
//    } catch (e: Exception) {
//        if (isShowToast) "".responseToast()
//        //可根据具体异常显示具体错误提示,此处可能是框架/服务器报错（没有提供规定的json结构体）或者json结构解析错误
//        err(Triple(FAILURE, "", e))
//    } finally {
//        log("结束请求")
//        end()
//    }
    log("开始请求")
    flow {
        val value = withContext(IO) {
            log("发起请求")
            coroutineScope()
        }
        emit(value)
    }.flowOn(Main).catch {
        if (isShowToast) "".responseToast()
        //可根据具体异常显示具体错误提示,此处可能是框架/服务器报错（没有提供规定的json结构体）或者json结构解析错误
        err(Triple(FAILURE, "", it as? Exception))
    }.onCompletion {
        log("结束请求")
        end()
    }.collect {
        log("处理结果")
        if (it.successful()) {
            resp(it)
        } else {
            //如果不是被顶号才会有是否提示的逻辑
            if (!it.tokenExpired()) if (isShowToast) it.msg.responseToast()
            //不管结果如何，失败的回调是需要执行的
            err(Triple(it.code, it.msg, null))
        }
    }
}

suspend fun <T> requestAffair(
    coroutineScope: suspend CoroutineScope.() -> T,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
//    try {
//        resp(withContext(IO) { coroutineScope() })
//    } catch (e: Exception) {
//        if (isShowToast) "".responseToast()
//        err(Triple(FAILURE, "", e))
//    } finally {
//        end()
//    }
    flow {
        val value = withContext(IO) { coroutineScope() }
        emit(value)
    }.flowOn(Main).catch {
        if (isShowToast) "".responseToast()
        err(Triple(FAILURE, "", it as? Exception))
    }.onCompletion {
        end()
    }.collect {
        resp(it)
    }
}

private fun log(msg: String) = "${msg}->当前线程：${Thread.currentThread().name}".logE("repository")

/**
 * 判断此次请求是否成功
 */
fun <T> ApiResponse<T>?.successful(): Boolean {
    if (this == null) return false
    return SUCCESS == code
}

/**
 * 判断此次请求是否token过期
 */
fun <T> ApiResponse<T>?.tokenExpired(): Boolean {
    if (this == null) return false
    if (TOKEN_EXPIRED == code) {
        AccountHelper.signOut()
        return true
    }
    return false
}