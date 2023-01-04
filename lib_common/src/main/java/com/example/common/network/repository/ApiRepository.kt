package com.example.common.network.repository

import com.example.common.R
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
fun <K, V> HashMap<K, V>?.params() = (if (null == this) "" else GsonUtil.objToJson(this).orEmpty()).toRequestBody("application/json; charset=utf-8".toMediaType())

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseMsg(){
    val strTemp = this
    (if (!NetWorkUtil.isNetworkAvailable()) resString(R.string.label_response_net_error) else { if(strTemp.isNullOrEmpty()) resString(R.string.label_response_error) else strTemp }).shortToast()
}

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
        "1:${Thread.currentThread().name}".logE("repository")
        //请求+响应数据
        val data = withContext(IO) {
            "2:${Thread.currentThread().name}".logE("repository")
            request()
        }
        val body = data.response()
        if (null != body) resp(body) else {
            if (isShowToast) data.msg.responseMsg()
            err(Triple(data.code, data.msg, null))
        }
    } catch (e: Exception) {
        if (isShowToast) "".responseMsg()
        err(Triple(-1, "", e))  //可根据具体异常显示具体错误提示
    } finally {
        "3:${Thread.currentThread().name}".logE("repository")
        end()
    }
}

/**
 * 直接获取到对象
 */
suspend fun <T> request(
    request: suspend CoroutineScope.() -> ApiResponse<T>,
    isShowToast: Boolean = true
): T? {
    var t: T? = null
    request({ request() }, {
        t = it
    }, isShowToast = isShowToast)
    return t
}

/**
 * 网络请求协程扩展-串行请求
 * 几个以上的挂起方法套在一个launch或async内都会是串行请求
 * 如项目中某个请求前必须先完成另一个请求，则可以使用当前的扩展，只有开始和完成
 * launch {
 * loadHttp({},
 * arrayOf({AccountSubscribe.getAuthInfoApi()},{AccountSubscribe.getAuthInfoApi()}),
 * end = {
 * })
 * }
 */
suspend fun request(
    requests: List<suspend CoroutineScope.() -> ApiResponse<*>>,
    end: (result: MutableList<Any?>?) -> Unit = {}
) {
    val respList = ArrayList<Any?>()
    try {
        withContext(IO) {
            "串行请求开始时间：${System.nanoTime()}".logE("repository")
            for (req in requests) {
                "请求${req}执行时间：${System.nanoTime()}".logE("repository")
                val data = req()
                if (200 == data.code) {
                    val body = data.response()
                    respList.add(body)
                    "请求${req}执行结果：${GsonUtil.objToJson(body ?: Any())}".logE("repository")
                } else {
                    "请求${req}执行结果：返回参数非200，中断执行".logE("repository")
                    break
                }
            }
        }
    } catch (e: Exception) {
        "串行请求返回结果：接口执行中出现异常".logE("repository")
    } finally {
        //如果返回的对象长度和发起的请求长度是一样的，说明此次串行都执行成功，直接拿取集合即可,其中一条失败就返回空
        "串行请求返回结果:${respList.size == requests.size}".logE("repository")
        end(if (respList.size == requests.size) respList else null)
    }
}

/**
 * 项目接口返回对象解析
 */
fun <T> ApiResponse<T>?.response(): T? {
    if (this == null) return null
    return if (200 == code) {
        if (null == data) Any() as? T else data
//        data
    } else {
        if (408 == code) AccountHelper.signOut()
        null
    }
}