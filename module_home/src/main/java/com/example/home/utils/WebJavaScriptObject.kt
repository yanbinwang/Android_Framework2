package com.example.home.utils

import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.toBrowser
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.execute
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * activity继承WebImpl，实现对应的方法
 * 然后helper中添加订阅
 */
class WebJavaScriptObject(private val webImpl: WeakReference<WebImpl>) {
    private var job: Job? = null

    init {
        webImpl.get()?.getActivity().doOnDestroy { job?.cancel() }
    }

    @JavascriptInterface
    fun toKol(result: String?) = webImpl.get()?.execute {
        job?.cancel()
        job = GlobalScope.launch(Main) { getToKolPage() }
    }

    @JavascriptInterface
    fun goBack(result: String?) = webImpl.get()?.execute {
        job?.cancel()
        job = GlobalScope.launch(Main) { getBack(result) }
    }

    @JavascriptInterface
    fun toast(result: String?) {
        job?.cancel()
        job = GlobalScope.launch(Main) { result.orEmpty().shortToast() }
    }

    @JavascriptInterface
    fun download(result: String?) = webImpl.get()?.execute {
        job?.cancel()
        job = GlobalScope.launch(Main) { getActivity().toBrowser(result.orEmpty()) }
    }

}

interface WebImpl {
    fun getActivity(): FragmentActivity
    fun getToKolPage()
    fun getBack(result: String?)
}