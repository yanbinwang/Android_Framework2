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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

//------------------------------------针对协程返回的参数(协程只有成功和失败)------------------------------------
/**
 * ViewModel的KTX库中具备扩展函数，但不能像继承CoroutineScope那样直接launch点出，这里再做一个扩展
 */
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
 * 网络请求协程扩展
 */
fun <T> CoroutineScope.loadHttp(
    start: () -> Unit = {},
    request: suspend CoroutineScope.() -> ApiResponse<T>,
    resp: (T?) -> Unit = {},
    err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
    end: () -> Unit = {},
    isShowToast: Boolean = false
) {
    launch {
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
 * 串行执行网络请求
 */
fun <T> CoroutineScope.loadHttp(
    start: () -> Unit = {},
    requests: MutableList<suspend CoroutineScope.() -> ApiResponse<T>>,
    err: (e: Exception?) -> Unit = {},
    end: (result: Pair<Boolean, MutableList<T?>>) -> Unit = {}
) {
    launch {
        val respList = ArrayList<T?>()
        start()
        try {
            withContext(IO) {
                for (req in requests) {
                    LogUtil.e("repository", "串行执行时间：${System.nanoTime()}")
                    val body = req().response()
                    LogUtil.e("repository", "串行执行JSON：${GsonUtil.objToJson(body?:Any())}")
                    respList.add(body)
                }
            }
        } catch (e: Exception) {
            err(e)
        } finally {
            LogUtil.e("repository", "串行执行结果:${respList.size == requests.size}")
            //完成的时候判断一下请求是否都成功了
            end(Pair(respList.size == requests.size, respList))
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