package com.example.common.http.repository

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.page.doResponse
import com.example.common.utils.helper.AccountHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败)
 */

/**
 * ViewModel的KTX库中具备扩展函数，但不能像继承CoroutineScope那样直接launch点出，这里再做一个扩展
 * CoroutineScope接受一个参数是线程的上下文，返回一个CoroutineScope对象
 * 顺序执行请求
 */
fun BaseViewModel.launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(block = block)

/**
 * 并发执行多个请求时使用，通过await()可以拿取到最后得到的值
 */
fun BaseViewModel.async(block: suspend CoroutineScope.() -> Unit) = viewModelScope.async(block = block)

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

fun Fragment.async(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.async(block = block)

fun AppCompatActivity.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

fun AppCompatActivity.async(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.async(block = block)

/**
 * 请求监听扩展
 */
suspend fun <T> T?.call(): T? {
    try {
        withContext(IO) { this@call }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**
 * 项目请求监听扩展
 */
fun <T> ApiResponse<T>?.apiCall(toast: Boolean = true): T? {
    return if (null != this) {
        if (200 == code) {
            if(null == data) Any() as T else data
        } else {
            if (408 == code) AccountHelper.signOut()
            if (toast) msg.doResponse()
            null
        }
    } else {
        "".doResponse()
        null
    }
}

///**
// * 切换io线程，获取请求的对象
// */
//private suspend fun <T> execute(block: () -> T): T? = withContext(IO) { block() }