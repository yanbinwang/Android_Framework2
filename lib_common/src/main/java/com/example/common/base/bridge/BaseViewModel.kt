package com.example.common.base.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base.utils.function.view.gone
import com.example.common.base.page.getEmptyView
import com.example.common.base.page.responseMsg
import com.example.common.bus.Event
import com.example.common.bus.EventBus
import com.example.common.http.repository.ApiResponse
import com.example.common.http.repository.request
import com.example.common.http.repository.response
import com.example.common.utils.AppManager
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.refresh.XRefreshLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.greenrobot.eventbus.Subscribe
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
@SuppressLint("StaticFieldLeak")
abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    private var weakActivity: WeakReference<FragmentActivity>? = null//引用的activity
    private var softView: SoftReference<BaseView>? = null//基础UI操作

    //部分view的操作交予viewmodel去操作，不必让activity去操作
    private var softEmpty: SoftReference<EmptyLayout>? = null//遮罩UI
    private var softRecycler: SoftReference<XRecyclerView>? = null//列表UI
    private var softRefresh: SoftReference<XRefreshLayout>? = null//刷新控件

    //基础的注入参数
    protected val activity: FragmentActivity get() { return weakActivity?.get() ?: (AppManager.currentActivity() as? FragmentActivity) ?: FragmentActivity() }
    protected val context: Context get() { return activity }
    protected val view: BaseView? get() { return softView?.get() }

    //获取对应的控件
    val emptyView: EmptyLayout? get() { return softEmpty?.get() }
    val recyclerView: XRecyclerView? get() { return softRecycler?.get() }
    val xRefreshLayout: XRefreshLayout? get() { return softRefresh?.get() }

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(activity: FragmentActivity, view: BaseView) {
        this.weakActivity = WeakReference(activity)
        this.softView = SoftReference(view)
    }

    fun setEmptyView(container: ViewGroup) {
        this.softEmpty = SoftReference(container.getEmptyView())
    }

    //viewModel.setRecyclerView(binding.xrvChain.apply { listPag = paging })
    fun setRecyclerView(xRecyclerView: XRecyclerView) {
        this.softEmpty = SoftReference(xRecyclerView.empty)
        this.softRecycler = SoftReference(xRecyclerView)
    }

    fun setRefreshLayout(xRefreshLayout: XRefreshLayout) {
        this.softRefresh = SoftReference(xRefreshLayout)
    }

    protected fun reset() {
        xRefreshLayout?.finishRefresh()
        recyclerView?.finishRefresh()
        emptyView?.gone()
    }

    /**
     * 常规发起一个网络请求
     */
    protected fun <T> launch(
        request: suspend CoroutineScope.() -> ApiResponse<T>,      // 请求
        resp: (T?) -> Unit = {},                                   // 响应
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}, // 错误处理
        end: () -> Unit = {},                                      // 最后执行方法
        isShowToast: Boolean = true,                               // 是否toast
        isShowDialog: Boolean = true,                              // 是否显示加载框
        isClose: Boolean = true                                    // 请求结束前是否关闭dialog
    ): Job {
        return launch {
            request(
                { if (isShowDialog) view?.showDialog() },
                { request() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog || isClose) view?.hideDialog()
                    end()
                },
                isShowToast
            )
        }
    }

    /**
     * 串行发起多个网络请求
     */
    protected fun launch(
        start: () -> Unit = {},
        requests: List<suspend CoroutineScope.() -> ApiResponse<*>>,
        end: (result: MutableList<Any?>?) -> Unit = {}
    ): Job {
        return launch {
            request(start, requests, end)
        }
    }

    /**
     * 不做回调，直接得到结果
     * 套launch（主线程）
     */
    protected suspend fun <T> async(
        request: suspend CoroutineScope.() -> ApiResponse<T>,
        isShowToast: Boolean = true
    ): T? {
        return async(IO) {
            var t: T? = null
            try {
                val req = request()
                val body = req.response()
                if (null != body) {
                    t = body
                } else {
                    if (isShowToast) withContext(Main) { req.msg.responseMsg() }
                }
            } catch (e: Exception) {
                if (isShowToast) withContext(Main) { "".responseMsg() }
            }
            t
        }.await()
    }

    override fun onCleared() {
        super.onCleared()
        weakActivity?.clear()
        softView?.clear()
        softEmpty?.clear()
        softRecycler?.clear()
        softRefresh?.clear()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    @Subscribe
    fun onReceive(event: Event) {
        event.onEvent()
    }

    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (isEventBusEnabled()) EventBus.instance.register(this, owner.lifecycle)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        if (isEventBusEnabled()) EventBus.instance.unregister(this)
    }
    // </editor-fold>

}

fun ViewModel.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch(context, start, block)

fun <T> ViewModel.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = viewModelScope.async(context, start, block)