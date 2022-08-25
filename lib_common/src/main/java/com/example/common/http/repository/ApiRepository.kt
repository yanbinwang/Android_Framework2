package com.example.common.http.repository

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.bridge.BaseViewModel
import com.example.common.constant.ARouterPath
import com.example.common.utils.helper.AccountHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/9/2.
 * 针对协程返回的参数(协程只有成功和失败)
 */

/**
 * ViewModel的KTX库中具备扩展函数，但不能像继承CoroutineScope那样直接launch点出，这里再做一个扩展
 * CoroutineScope接受一个参数是线程的上下文，返回一个CoroutineScope对象
 */
fun BaseViewModel.launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(block = block)

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

fun AppCompatActivity.launch(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launch(block = block)

///**
// * 针对项目请求编号处理,需要处理的在请求文件里书写此扩展函数
// */
//fun <T> ApiResponse<T>.invoke(): ApiResponse<T> {
//    when (code) {
//        //账号还没有登录，解密失败，重新获取
//        100005, 100008 -> {
//            AccountHelper.signOut()
//            ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
//        }
//        //账号被锁定--进入账号锁定页（其余页面不关闭）
//        100002 -> {
////                         ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation()
//        }
//    }
//    return this
//}

/**
 * 项目请求监听扩展
 */
suspend fun <T> ApiResponse<T>.apiCall(subscriber: HttpSubscriber<T>?) = call(subscriber)

/**
 * 请求监听扩展
 */
suspend fun <T> T.call(subscriber: ResourceSubscriber<T>?): T {
    subscriber?.onStart()
    try {
        subscriber?.onNext(withContext(IO) { this@call })
    } catch (e: Exception) {
        subscriber?.onError(e)
    } finally {
        subscriber?.onComplete()
    }
    return this
}

/**
 *  部分请求需要监听开始和结束，采用此请求结构
 */
suspend fun <T> execute(block: T, subscriber: ResourceSubscriber<T>?) {
    subscriber?.onStart()
    block.call(subscriber)
    subscriber?.onComplete()
}

///**
// * 切换io线程，获取请求的对象
// */
//private suspend fun <T> execute(block: () -> T): T? = withContext(IO) { block() }