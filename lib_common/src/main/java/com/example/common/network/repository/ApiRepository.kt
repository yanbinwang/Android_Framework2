package com.example.common.network.repository

import com.example.common.R
import com.example.common.network.repository.ApiCode.FAILURE
import com.example.common.network.repository.ApiCode.SUCCESS
import com.example.common.network.repository.ApiCode.TOKEN_EXPIRED
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.analysis.GsonUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.params() = (if (null == this) "" else GsonUtil.objToJson(this)
    .orEmpty()).toRequestBody("application/json; charset=utf-8".toMediaType())

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseToast() = (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.label_response_net_error) else {
    if (isNullOrEmpty()) resString(R.string.label_response_error) else this
}).shortToast()

/**
 * 网络请求协程扩展-并行请求
 * 每个挂起方法外层都会套一个launch
 */
suspend fun <T> request(
    request: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    try {
        log("开始请求")
        //请求+响应数据
        withContext(IO) {
            log("发起请求")
            request()
        }.let {
            log("处理结果")
            if (it.process()) resp(it.response()) else {
                if (isShowToast) it.msg.responseToast()
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

/**
 * 网络请求协程扩展-直接获取到对象
 * 如果几个以上的请求，互相之间有关联，则使用当前方法
 * launch {
 *  val req1 = request(1api)
 *  val req2 = request(2api)
 * }
 */
suspend fun <T> request(
    request: suspend CoroutineScope.() -> ApiResponse<T>,
    isShowToast: Boolean = false
): T? {
    var t: T? = null
    request({ request() }, {
        t = it
    }, isShowToast = isShowToast)
    return t
}

///**
// * 网络请求协程扩展-串行请求
// * 几个以上的挂起方法套在一个launch或async内都会是串行请求
// * 如项目中触发多个请求，并且毫无关联，则可以使用当前扩展，只有开始和完成
// * launch {
// * request({},
// * arrayOf({AccountSubscribe.getAuthInfoApi()},{AccountSubscribe.getAuthInfoApi()}),
// * end = {
// * })
// * }
// */
//suspend fun request(
//    requests: List<suspend CoroutineScope.() -> ApiResponse<*>>,
//    end: (result: MutableList<Any?>?) -> Unit = {}
//) {
//    val respList = ArrayList<Any?>()
//    try {
//        withContext(IO) {
//            log("串行请求开始时间：${System.nanoTime()}")
//            for (req in requests) {
//                log("请求${req}执行时间：${System.nanoTime()}")
//                val data = req()
//                if (data.process()) {
//                    val body = data.response()
//                    respList.add(body)
//                    log("请求${req}执行结果：${GsonUtil.objToJson(body ?: Any())}")
//                } else {
//                    log("请求${req}执行结果：返回参数非200，中断执行")
//                    break
//                }
//            }
//        }
//    } catch (e: Exception) {
//        log("串行请求返回结果：接口执行中出现异常")
//    } finally {
//        //如果返回的对象长度和发起的请求长度是一样的，说明此次串行都执行成功，直接拿取集合即可,其中一条失败就返回空
//        log("串行请求返回结果:${respList.size == requests.size}")
//        end(if (respList.size == requests.size) respList else null)
//    }
//}

private fun log(msg: String) = "${msg}\n当前线程：${Thread.currentThread().name}".logE("repository")

/**
 * 项目接口返回对象解析
 */
fun <T> ApiResponse<T>?.response(): T? {
    if (this == null) return null
    return if (process()) {
        data
    } else {
        tokenExpired()
        null
    }
}

/**
 * 判断此次请求是否成功
 */
fun <T> ApiResponse<T>?.process(): Boolean {
    if (this == null) return false
    return SUCCESS == code
}

/**
 * 判断此次请求是否成功
 */
fun <T> ApiResponse<T>?.tokenExpired() {
    if (this == null) return
    if (TOKEN_EXPIRED == code) AccountHelper.signOut()
}