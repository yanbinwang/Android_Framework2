package com.example.common.http.repository

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.base.utils.LogUtil
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.page.responseMsg
import com.example.common.utils.analysis.GsonUtil
import com.example.common.utils.helper.AccountHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
fun BaseViewModel.launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(block = block)

fun BaseViewModel.async(block: suspend CoroutineScope.() -> Unit) = viewModelScope.async(block = block)

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

fun Fragment.async(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.async(block = block)

fun AppCompatActivity.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

fun AppCompatActivity.async(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.async(block = block)

/**
 * 请求转换
 * map扩展，如果只需传入map则使用
 * hashMapOf("" to "")不需要写此扩展
 */
fun <K, V> HashMap<K, V>?.params() = (if (null == this) "" else GsonUtil.objToJson(this).orEmpty()).toRequestBody("application/json; charset=utf-8".toMediaType())

/**
 * 网络请求协程扩展-并行请求
 * 每个挂起方法外层都会套一个launch
 */
fun <T> CoroutineScope.loadHttp(
    start: () -> Unit = {},
    request: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    launch(Main) {
        try {
            LogUtil.e("repository", "1:${Thread.currentThread().name}")
            start()
            //请求+响应数据
            val data = withContext(IO) {
                LogUtil.e("repository", "2:${Thread.currentThread().name}")
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
            LogUtil.e("repository", "3:${Thread.currentThread().name}")
            end()
        }
    }
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
fun CoroutineScope.loadHttp(
    start: () -> Unit = {},
    requests: Array<suspend CoroutineScope.() -> ApiResponse<*>>,
    end: (result: MutableList<Any?>?) -> Unit = {}
) {
    launch(Main) {
        val respList = ArrayList<Any?>()
        start()
        try {
            withContext(IO) {
                LogUtil.e("repository", "串行请求开始时间：${System.nanoTime()}")
                for (req in requests) {
                    LogUtil.e("repository", "请求${req}执行时间：${System.nanoTime()}")
                    val data = req()
                    if (200 == data.code) {
                        val body = data.response()
                        respList.add(body)
                        LogUtil.e("repository", "请求${req}执行结果：${GsonUtil.objToJson(body ?: Any())}")
                    } else {
                        LogUtil.e("repository", "请求${req}执行结果：返回参数非200，中断执行")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            LogUtil.e("repository", "串行请求返回结果：接口执行中出现异常")
        } finally {
            //如果返回的对象长度和发起的请求长度是一样的，说明此次串行都执行成功，直接拿取集合即可,其中一条失败就返回空
            LogUtil.e("repository", "串行请求返回结果:${respList.size == requests.size}")
            end(if (respList.size == requests.size) respList else null)
        }
    }
}

/**
 * 项目接口返回对象解析
 */
fun <T> ApiResponse<T>?.response(): T? {
    return if (null != this) {
        if (200 == code) {
            if (null == data) Any() as T else data
        } else {
            if (408 == code) AccountHelper.signOut()
            null
        }
    } else null
}