package com.example.home.utils

import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.toBrowser
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

/**
 * activity继承WebImpl，实现对应的方法
 * 然后helper中添加订阅
 */
class WebJavaScriptObject(private val webImpl: WeakReference<WebImpl>) : CoroutineScope {
    private var operationJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Main + job

    init {
        webImpl.get()?.getActivity().doOnDestroy {
            operationJob?.cancel()
            job.cancel()
        }
    }

    @JavascriptInterface
    fun goBack(value: String?) = webImpl.get()?.execute {
        operationJob?.cancel()
        operationJob = launch {
            getGoBackJS(value)
        }
    }

    @JavascriptInterface
    fun toast(value: String?) {
        operationJob?.cancel()
        operationJob = launch {
            value.shortToast()
        }
    }

    @JavascriptInterface
    fun download(value: String?) = webImpl.get()?.execute {
        operationJob?.cancel()
        operationJob = launch {
            getActivity().toBrowser(value.orEmpty())
        }
    }

    @JavascriptInterface
    fun toKol(value: String?) = webImpl.get()?.execute {
        operationJob?.cancel()
        operationJob = launch {
            getToKolJS()
        }
    }

}

interface WebImpl {
    /**
     * 获取父页面页面管理
     */
    fun getActivity(): FragmentActivity

    /**
     * WEB调取关闭时候的回调
     */
    fun getGoBackJS(value: String?)

    /**
     * WEB调取跳转kol界面回调
     */
    fun getToKolJS()
}