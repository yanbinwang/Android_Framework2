package com.example.common.network.repository

import com.example.common.R
import com.example.common.base.bridge.BaseView
import com.example.common.network.repository.ApiCode.FAILURE
import com.example.common.network.repository.ApiCode.SUCCESS
import com.example.common.network.repository.ApiCode.TOKEN_EXPIRED
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.utils.function.toJsonString
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
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
    private var view: BaseView?,
    private val isShowDialog: Boolean = true,
    private val err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
) {
    private var loadingStarted = false//是否开始加载

    /**
     * 发起请求
     */
    suspend fun <T> request(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = this.err
    ): T? {
        if (isShowDialog && !loadingStarted) {
            view?.showDialog()
            loadingStarted = true
        }
        var t: T? = null
        request({ coroutineScope() }, {
            t = it
        }, err, isShowToast = false)
        return t
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
    }

}

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.params() =
    this?.toJsonString().orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType())

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseToast() = (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.response_net_error) else {
    if (isNullOrEmpty()) resString(R.string.response_error) else this
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
    requestLayer(coroutineScope, {
        resp.invoke(it?.data)
    }, err, end, isShowToast)
}

suspend fun <T> requestLayer(
    coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (ApiResponse<T>?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    try {
        log("开始请求")
        //请求+响应数据
        withContext(IO) {
            log("发起请求")
            coroutineScope()
        }.let {
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
    } catch (e: Exception) {
        if (isShowToast) "".responseToast()
        err(Triple(FAILURE, "", e))  //可根据具体异常显示具体错误提示
    } finally {
        log("结束请求")
        end()
    }
}

private fun log(msg: String) = "${msg}\n当前线程：${Thread.currentThread().name}".logE("repository")

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