package com.example.common.base.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.common.base.page.Paging
import com.example.common.base.page.getEmptyView
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.ResponseWrapper
import com.example.common.network.repository.request
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.requestLayer
import com.example.common.utils.manager.AppManager
import com.example.common.utils.manager.JobManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.EmptyLayout
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.fade
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
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
    //基础引用
    private var weakActivity: WeakReference<FragmentActivity>? = null//引用的activity
    private var weakView: WeakReference<BaseView>? = null//基础UI操作
    //部分view的操作交予viewmodel去操作，不必让activity去操作
    private var weakEmpty: WeakReference<EmptyLayout?>? = null//遮罩UI
    private var weakRecycler: WeakReference<XRecyclerView?>? = null//列表UI
    private var weakRefresh: WeakReference<SmartRefreshLayout?>? = null//刷新控件
    //分页
    private val paging by lazy { Paging() }
    //全局倒计时时间点
    protected var lastRefreshTime = 0L
    //基础的注入参数
    protected val mActivity: FragmentActivity get() = weakActivity?.get() ?: (AppManager.currentActivity() as? FragmentActivity) ?: FragmentActivity()
    protected val mContext: Context get() = mActivity
    protected val mView: BaseView? get() = weakView?.get()
    //获取对应的控件/分页类
    protected val mEmpty get() = weakEmpty?.get()
    protected val mRecycler get() = weakRecycler?.get()
    protected val mRefresh get() = weakRefresh?.get()
    //弹框/获取权限
    protected val mDialog by lazy { AppDialog(mContext) }
    protected val mPermission by lazy { PermissionHelper(mContext) }
    //协程管理类
    val jobManager by lazy { JobManager() }

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(activity: FragmentActivity, view: BaseView) {
        this.weakActivity = WeakReference(activity)
        this.weakView = WeakReference(view)
    }

    /**
     * 此处传入的是外层容器，而不是一个写好的EmptyView
     * 继承BaseTitleActivity的页面传父类的ViewGroup
     * 其余页面外层写FrameLayout，套上要使用的布局后再initView中调用该方法
     */
    fun setExtraView(view: View?, refresh: SmartRefreshLayout? = null) {
        view ?: return
        //处理 view 的类型，设置 weakEmpty 和 weakRecycler
        when (view) {
            //传入BaseTitleActivity中写好的容器viewGroup
            is FrameLayout -> weakEmpty = WeakReference(view.getEmptyView(1))
            //界面上绘制好empty
            is EmptyLayout -> weakEmpty = WeakReference(view)
            //传入用于刷新的empty
            is XRecyclerView -> {
                weakEmpty = WeakReference(view.empty)
                weakRecycler = WeakReference(view)
            }
            //外层下拉刷新的控件
            is SmartRefreshLayout -> {
                //仅在未显式传入 refresh 时从 view 中获取刷新控件
                if (refresh == null) {
                    weakRefresh = WeakReference(view)
                }
            }
        }
        // 显式传入刷新控件时覆盖之前的设置
        if (refresh != null) {
            weakRefresh = WeakReference(refresh)
        }
    }

    /**
     * 当前列表内的数据
     */
    fun setCurrentCount(currentCount: Int?) {
        paging.currentCount = currentCount.orZero
        reset(hasNextPage())
    }

    /**
     * 设置当前总记录数
     */
    fun setTotalCount(totalCount: Int?) {
        paging.totalCount = totalCount.orZero
    }

    /**
     * 获取当前页数
     */
    fun getCurrentPage() = paging.page.toString()

    /**
     * 当前列表数额
     */
    fun currentCount() = paging.currentCount

    /**
     * 当前是否是刷新
     */
    fun hasRefresh() = paging.hasRefresh

    /**
     * 是否有下一页
     */
    fun hasNextPage() = paging.hasNextPage()

    /**
     * 刷新监听
     */
    fun onRefresh(listener: () -> Unit = {}) {
        paging.onRefresh(listener)
    }

    /**
     * 加载更多监听
     */
    fun onLoad(listener: (noMore: Boolean) -> Unit = {}) {
        paging.onLoad(listener)
    }

    /**
     * 此次请求失败
     */
    fun onError() {
        paging.onError()
    }

    /**
     * 空布局监听
     */
    fun setOnEmptyRefreshListener(listener: ((result: Boolean) -> Unit)) {
        mEmpty?.setOnEmptyRefreshListener { listener.invoke(it) }
    }

    /**
     * empty布局操作
     */
    fun loading() {
        finishRefreshing()
        mEmpty?.loading()
    }

    fun empty(resId: Int? = null, resText: Int? = null, resRefreshText: Int? = null, width: Int? = null, height: Int? = null) {
        finishRefreshing()
        mEmpty?.empty(resId, resText, resRefreshText, width, height)
    }

    fun error(resId: Int? = null, resText: Int? = null, resRefreshText: Int? = null, width: Int? = null, height: Int? = null) {
        finishRefreshing()
        mEmpty?.error(resId, resText, resRefreshText, width, height)
    }

    /**
     * 带刷新或者空白布局的列表/详情页再接口交互结束时直接在对应的viewmodel调用该方法
     * hasNextPage是否有下一页
     */
    fun reset(hasNextPage: Boolean? = true) {
        finishRefreshing(hasNextPage)
        if (null != mRecycler) {
            if (currentCount() != 0) mEmpty?.fade(300)
        } else {
            mEmpty?.fade(300)
        }
    }

    private fun finishRefreshing(hasNextPage: Boolean? = true) {
        if (null == mRecycler) mRefresh?.finishRefreshing()
        mRecycler?.finishRefreshing(!hasNextPage.orTrue)
    }

    /**
     * 常规发起一个网络请求
     */
    protected fun <T> launch(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>, // 请求
        resp: (T?) -> Unit = {},                                     // 响应
        err: (ResponseWrapper) -> Unit = {},                         // 错误处理
        end: () -> Unit = {},                                        // 最后执行方法
        isShowToast: Boolean = true,                                 // 是否toast
        isShowDialog: Boolean = true                                // 是否显示加载框
    ): Job {
        if (isShowDialog) mView?.showDialog()
        return launch {
            request(
                { coroutineScope() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog) mView?.hideDialog()
                    end()
                },
                isShowToast
            )
        }.apply { manageJob() }
    }

    /**
     * 网络请求外层同时返回
     */
    protected fun <T> launchLayer(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        resp: (ApiResponse<T>?) -> Unit = {},
        err: (ResponseWrapper) -> Unit = {},
        end: () -> Unit = {},
        isShowToast: Boolean = true,
        isShowDialog: Boolean = true
    ): Job {
        if (isShowDialog) mView?.showDialog()
        return launch {
            requestLayer(
                { coroutineScope() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog) mView?.hideDialog()
                    end()
                },
                isShowToast
            )
        }.apply { manageJob() }
    }

    /**
     * 只处理挂起的协程方法
     */
    protected fun <T> launchAffair(
        coroutineScope: suspend CoroutineScope.() -> T,
        resp: (T?) -> Unit = {},
        err: (ResponseWrapper) -> Unit = {},
        end: () -> Unit = {},
        isShowToast: Boolean = true,
        isShowDialog: Boolean = true,
        isClose: Boolean = true
    ): Job {
        if (isShowDialog) mView?.showDialog()
        return launch {
            requestAffair(
                { coroutineScope() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog || isClose) mView?.hideDialog()
                    end()
                },
                isShowToast
            )
        }.apply { manageJob() }
    }

    /**
     * 协程一旦启动，内部不调用cancel是会一直存在的，故而加一个管控
     */
    protected inline fun Job.manageJob() {
        val methodName = object {}.javaClass.enclosingMethod?.name ?: "unknown"
        jobManager.manageJob(this, methodName)
    }

    override fun onCleared() {
        super.onCleared()
        weakActivity?.clear()
        weakView?.clear()
        weakEmpty?.clear()
        weakRecycler?.clear()
        weakRefresh?.clear()
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
        jobManager.addObserver(owner)
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

/**
 * activity中构建viewmodel使用此方法
 * ViewModelStoreOwner
 */
fun <VM : BaseViewModel> Class<VM>.create(lifecycle: Lifecycle, owner: AppCompatActivity): VM {
    val viewModel = ViewModelProvider(owner)[this]
    lifecycle.addObserver(viewModel)
    lifecycle.doOnDestroy { lifecycle.removeObserver(viewModel) }
    return viewModel
}

/**
 * fragment中构建viewmodel使用此方法
 */
fun <VM : BaseViewModel> Class<VM>.create(lifecycle: Lifecycle, owner: Fragment): VM {
    val viewModel = ViewModelProvider(owner)[this]
    lifecycle.addObserver(viewModel)
    lifecycle.doOnDestroy { lifecycle.removeObserver(viewModel) }
    return viewModel
}