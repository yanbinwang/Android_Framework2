package com.example.common.http.repository

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
//ViewModel的KTX库中具备扩展函数，但不能像继承CoroutineScope那样直接launch点出，这里再做一个扩展
fun BaseViewModel.launch(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch {
        block()
    }

//针对项目请求编号处理,需要处理的在请求文件里书写此扩展函数
fun <T> ApiResponse<T>.invoke(): ApiResponse<T> {
    when (code) {
        //账号还没有登录，解密失败，重新获取
        100005, 100008 -> {
            AccountHelper.signOut()
//                         instance.post(RxBusEvent(Constants.APP_USER_LOGIN_OUT))
            ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
        }
        //账号被锁定--进入账号锁定页（其余页面不关闭）
        100002 -> {
//                         ARouter.getInstance().build(ARouterPath.UnlockIPActivity).navigation()
        }
    }
    return this
}

//项目请求监听扩展
suspend fun <T> ApiResponse<T>.apiCall(subscriber: HttpSubscriber<T>?) = call(subscriber)

suspend fun <T> ApiResponse<T>.apiCall2(subscriber: HttpSubscriber2<T>?) = call(subscriber)

//请求监听扩展
suspend fun <T> T.call(resourceSubscriber: ResourceSubscriber<T>?): T {
    resourceSubscriber?.onStart()
    try {
        val res: T? = execute { this }
        res?.let { resourceSubscriber?.onNext(it) }
    } catch (e: Exception) {
        resourceSubscriber?.onError(e)
    } finally {
        resourceSubscriber?.onComplete()
    }
    return this
}

//切换io线程，获取请求的对象
private suspend fun <T> execute(block: () -> T): T = withContext(IO) { block() }