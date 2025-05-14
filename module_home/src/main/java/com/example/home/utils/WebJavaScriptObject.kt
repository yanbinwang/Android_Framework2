package com.example.home.utils

import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.toBrowser
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * activity继承WebImpl，实现对应的方法
 * 然后helper中添加订阅
 */
class WebJavaScriptObject(private val webImpl: WeakReference<WebImpl>) {
    private var webJob: Job? = null
    private val mScope by lazy { webImpl.get()?.getCoroutineScope() }
    private val mActivity by lazy { webImpl.get()?.getActivity() }

    init {
        mActivity.doOnDestroy {
            webJob?.cancel()
        }
    }

    @JavascriptInterface
    fun goBack(value: String?) {
        webJob?.cancel()
        webJob = mScope?.launch {
            withContext(Main.immediate) { webImpl.get()?.getGoBackJS(value) }
        }
    }

    @JavascriptInterface
    fun toast(value: String?) {
        webJob?.cancel()
        webJob = mScope?.launch {
            withContext(Main.immediate) { value.shortToast() }
        }
    }

    @JavascriptInterface
    fun download(value: String?) {
        webJob?.cancel()
        webJob = mScope?.launch {
            withContext(Main.immediate) { mActivity.toBrowser(value.orEmpty()) }
        }
    }

    @JavascriptInterface
    fun toKol(value: String?) {
        webJob?.cancel()
        webJob = mScope?.launch {
            withContext(Main.immediate) { webImpl.get()?.getToKolJS() }
        }
    }

}

interface WebImpl {
    /**
     * 获取父页面页面管理
     */
    fun getActivity(): FragmentActivity

    /**
     * 获取协程上下文
     */
    fun getCoroutineScope(): CoroutineScope

    /**
     * WEB调取关闭时候的回调
     */
    fun getGoBackJS(value: String?)

    /**
     * WEB调取跳转kol界面回调
     */
    fun getToKolJS()
}