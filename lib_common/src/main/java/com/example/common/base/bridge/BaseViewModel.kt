package com.example.common.base.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.example.common.base.page.Paging
import com.example.common.base.page.getEmptyView
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.MultiReqUtil
import com.example.common.network.repository.request
import com.example.common.network.repository.requestLayer
import com.example.common.utils.AppManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.EmptyLayout
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.fade
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Dispatchers.Main
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
    fun setExtraView(viewGroup: ViewGroup?, index: Int = 1) {
        this.weakEmpty = WeakReference(viewGroup.getEmptyView(index))
    }

    fun setExtraView(recycler: XRecyclerView?) {
        this.weakEmpty = WeakReference(recycler?.empty)
        this.weakRecycler = WeakReference(recycler)
    }

    fun setExtraView(refresh: SmartRefreshLayout?) {
        this.weakRefresh = WeakReference(refresh)
    }

    /**
     * 当前列表内的数据
     */
    fun setCurrentCount(currentCount: Int?) {
        paging.currentCount = currentCount.orZero
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
     * 空布局监听
     */
    fun setEmptyRefreshListener(onRefresh: (() -> Unit)) {
        mEmpty?.setOnEmptyRefreshListener(onRefresh)
    }

    /**
     * empty布局操作
     */
    fun loading() {
        finishRefreshing()
        mEmpty?.loading()
    }

    fun empty(resId: Int = -1, text: String? = null) {
        finishRefreshing()
        mEmpty?.empty(resId, text)
    }

    fun error(resId: Int = -1, text: String? = null, refreshText: String? = null) {
        finishRefreshing()
        mEmpty?.error(resId, text, refreshText)
    }

    /**
     * 带刷新或者空白布局的列表/详情页再接口交互结束时直接在对应的viewmodel调用该方法
     * hasNextPage是否有下一页
     */
    fun reset(hasNextPage: Boolean? = true) {
        finishRefreshing(hasNextPage)
        mEmpty?.fade(300)
    }

    private fun finishRefreshing(hasNextPage: Boolean? = true) {
        if (null == mRecycler) mRefresh?.finishRefreshing()
        mRecycler?.finishRefreshing(hasNextPage.orTrue)
    }

    /**
     * 常规发起一个网络请求
     */
    protected fun <T> launch(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>, // 请求
        resp: (T?) -> Unit = {},                                     // 响应
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},   // 错误处理
        end: () -> Unit = {},                                        // 最后执行方法
        isShowToast: Boolean = true,                                 // 是否toast
        isShowDialog: Boolean = true,                                // 是否显示加载框
        isClose: Boolean = true                                      // 请求结束前是否关闭dialog
    ): Job {
        if (isShowDialog) mView?.showDialog()
        return launch {
            request(
                { coroutineScope() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog || isClose) mView?.hideDialog()
                    end()
                },
                isShowToast
            )
        }
    }

    protected fun <T> launchLayer(
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        resp: (ApiResponse<T>?) -> Unit = {},
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {},
        end: () -> Unit = {},
        isShowToast: Boolean = true,
        isShowDialog: Boolean = true,
        isClose: Boolean = true
    ): Job {
        if (isShowDialog) mView?.showDialog()
        return launch {
            requestLayer(
                { coroutineScope() },
                { resp(it) },
                { err(it) },
                {
                    if (isShowDialog || isClose) mView?.hideDialog()
                    end()
                },
                isShowToast
            )
        }
    }

    /**
     * 不做回调，直接得到结果
     * 在不调用await（）方法时可以当一个参数写，调用了才会发起请求并拿到结果
     * //并发
     * launch{
     *   val task1 = async({ req.request(model.getUserData() })
     *   val task2 = async({ req.request(model.getUserData() })
     *   //单个请求主动发起，处理对象
     *   task1.await()
     *   task2.await()
     *   //同时发起多个请求，list拿取对象
     *   val taskList = awaitAll(task1, task2)
     *   taskList.safeGet(0)
     *   taskList.safeGet(1)
     * }
     * //串行
     * launch{
     *    val task1 = request({ model.getUserData() })
     *    val task2 = request({ model.getUserData() })
     * }
     */
    protected fun <T> async(
        req: MultiReqUtil,
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}
    ): Deferred<T?> {
        return async(Main, LAZY) { req.request({ coroutineScope() }, err) }
    }

    protected fun <T> asyncLayer(
        req: MultiReqUtil,
        coroutineScope: suspend CoroutineScope.() -> ApiResponse<T>,
        err: (e: Triple<Int?, String?, Exception?>?) -> Unit = {}
    ): Deferred<ApiResponse<T>?> {
        return async(Main, LAZY) { req.requestLayer({ coroutineScope() }, err) }
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

fun <VM : BaseViewModel> Class<VM>.create(lifecycle: Lifecycle, owner: ViewModelStoreOwner): VM {
    val viewModel = ViewModelProvider(owner)[this]
    lifecycle.addObserver(viewModel)
    return viewModel
}