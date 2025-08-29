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
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.common.base.BaseActivity
import com.example.common.base.BaseBottomSheetDialogFragment
import com.example.common.base.BaseFragment
import com.example.common.base.BaseTopSheetDialogFragment
import com.example.common.base.page.Paging
import com.example.common.base.page.getEmptyView
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.network.repository.valueOn
import com.example.common.utils.manager.AppManager
import com.example.common.utils.manager.JobManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.EmptyLayout
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.common.widget.xrecyclerview.refresh.getAutoRefreshTime
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.logWTF
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by WangYanBin on 2020/6/3.
 * 所有ViewModel的基类，将本该属于BaseActivity的部分逻辑和操作View的相关方法放入该类实现
 * 注入BaseView，LifecycleOwner，开发的时候可以随时存取和调用基类Activity的基础控件和方法
 * LifecycleObserver-->观察宿主的生命周期
 */
@SuppressLint("StaticFieldLeak")
abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    //基础引用
    private var weakActivity: WeakReference<FragmentActivity?>? = null//引用的activity
    private var weakView: WeakReference<BaseView?>? = null//基础UI操作
    //部分view的操作交予viewmodel去操作，不必让activity去操作
    private var weakEmpty: WeakReference<EmptyLayout?>? = null//遮罩UI
    private var weakRecycler: WeakReference<XRecyclerView?>? = null//列表UI
    private var weakRefresh: WeakReference<SmartRefreshLayout?>? = null//刷新控件
    private var weakLifecycleOwner: WeakReference<LifecycleOwner?>? = null//全局生命周期订阅
    //分页
    private val paging by lazy { Paging() }
    //全局倒计时时间点
    protected var lastRefreshTime = 0L
    //基础的注入参数
    protected val mActivity: FragmentActivity? get() = weakActivity?.get() ?: AppManager.currentActivity() as? FragmentActivity
    protected val mContext: Context? get() = mActivity
    protected val mView: BaseView? get() = weakView?.get()
    //获取对应的控件/分页类/生命周期订阅者/类名
    protected val mEmpty get() = weakEmpty?.get()
    protected val mRecycler get() = weakRecycler?.get()
    protected val mRefresh get() = weakRefresh?.get()
    protected val mLifecycleOwner get() = weakLifecycleOwner?.get()
    protected val mClassName get() = javaClass.simpleName.lowercase(Locale.getDefault())
    //弹框/获取权限/协程管理类/viewmodel命名
    protected val mDialog by lazy { mActivity?.let { AppDialog(it) } }
    protected val mPermission by lazy { mActivity?.let { PermissionHelper(it) } }
    protected val mJobManager by lazy { JobManager(mLifecycleOwner) }

    // <editor-fold defaultstate="collapsed" desc="构造和内部方法">
    fun initialize(activity: FragmentActivity?, view: BaseView?) {
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
            is FrameLayout -> {
                weakEmpty = WeakReference(view.getEmptyView(1))
                mEmpty?.setWindows(true)
            }
            //界面上绘制好empty
            is EmptyLayout -> weakEmpty = WeakReference(view)
            //传入用于刷新的empty
            is XRecyclerView -> {
                weakRecycler = WeakReference(view)
                weakEmpty = WeakReference(view.empty)
                //如果recyclerview是带有刷新的，且外层并未在该方法内注入refresh控件
                if (view.isRefresh() && refresh == null) {
                    weakRefresh = WeakReference(view.refresh)
                }
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
        if (!paging.hasRefresh) return
        paging.totalCount = totalCount.orZero
    }

    /**
     * 获取当前页数
     */
    fun getCurrentPage(): String {
        return paging.currentPage.toString()
    }

    /**
     * 当前列表数额
     */
    fun currentCount(): Int {
        return paging.currentCount
    }

    /**
     * 当前是否是刷新
     */
    fun hasRefresh(): Boolean {
        return paging.hasRefresh
    }

    /**
     * 是否有下一页
     */
    fun hasNextPage(): Boolean {
        return paging.hasNextPage()
    }

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
        mEmpty?.setOnEmptyRefreshListener {
            listener.invoke(it)
        }
    }

    /**
     * empty布局操作
     */
    fun loading() {
        finishRefreshing()
        mEmpty?.loading()
    }

    fun empty(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        finishRefreshing()
        mEmpty?.empty(resId, text, refreshText, width, height)
    }

    fun error(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        finishRefreshing()
        mEmpty?.error(resId, text, refreshText, width, height)
    }

    /**
     * 带刷新或者空白布局的列表/详情页再接口交互结束时直接在对应的viewmodel调用该方法
     * hasNextPage是否有下一页
     */
    fun reset(hasNextPage: Boolean? = true) {
        finishRefreshing(hasNextPage)
        val recycler = mRecycler // 缓存变量，减少重复调用
        if (recycler != null && currentCount() != 0 || recycler == null) {
            mEmpty?.fade(300)
        }
    }

    private fun finishRefreshing(hasNextPage: Boolean? = true) {
        mRefresh?.finishRefreshing(!hasNextPage.orTrue)
//        val recycler = mRecycler
//        if (recycler == null) {
//            mRefresh?.finishRefreshing()
//        } else {
//            recycler.finishRefreshing(!hasNextPage.orTrue)
//        }
    }

    /**
     * 自动刷新控件浮现，记得setExtraView
     */
    protected suspend fun autoRefresh() {
        delay(mRefresh?.getAutoRefreshTime().orZero)
    }

    /**
     * 协程一旦启动，内部不调用cancel是会一直存在的，故而加一个管控
     */
    protected inline fun Job.manageJob(key: String? = null) {
        val methodName = object {}.javaClass.enclosingMethod?.name ?: "unknown"
        val mJobKey = "${mClassName}::${if (!key.isNullOrEmpty()) key else methodName}"
        mJobKey.logWTF("manageJob")
        mJobManager.manageJob(this, mJobKey)
    }

    /**
     * 如果使用StateFlow发送数据,使用该方法
     */
    protected inline fun <T> MutableStateFlow<T>.value(value: T) {
        mLifecycleOwner?.let {
            mJobManager.manageValue(this, valueOn(it, value))
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            weakActivity?.clear()
            weakView?.clear()
            weakEmpty?.clear()
            weakRecycler?.clear()
            weakRefresh?.clear()
            weakLifecycleOwner?.clear()
        }.onFailure { e ->
            //处理清除 WeakReference 时的异常
            e.printStackTrace()
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        weakLifecycleOwner = WeakReference(owner)
        if (isEventBusEnabled()) {
            EventBus.instance.register(owner) {
                it.onEvent()
            }
        }
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
 * viewModel委托类
 * private val viewModel :MarketListViewModel by viewModels()
 */
class ViewModelDelegate<VM : BaseViewModel>(
    private val viewModelClass: Class<VM>,
    private val lifecycle: Lifecycle,
    private val owner: ViewModelStoreOwner,
    private val activity: FragmentActivity?,
    private val view: BaseView?
) : ReadOnlyProperty<Any, VM> {

    /**
     * 延迟初始化 ViewModel 实例。
     * 只有在首次访问时才会创建 ViewModel 实例，并进行必要的初始化操作。
     */
    private var lazyViewModel: Lazy<VM> = lazy {
        // 创建 ViewModel 实例
        val viewModel = ViewModelProvider(owner)[viewModelClass]
        // 将 ViewModel 注册为生命周期观察者
        lifecycle.addObserver(viewModel)
        // 初始化 ViewModel
        viewModel.initialize(activity, view)
        viewModel
    }

    /**
     * 获取 ViewModel 实例。
     * 调用该方法时会触发 lazyViewModel 的初始化（如果还未初始化）。
     */
    override fun getValue(thisRef: Any, property: KProperty<*>): VM {
        return lazyViewModel.value
    }
}

inline fun <reified VM : BaseViewModel> AppCompatActivity.viewModels(lifecycle: Lifecycle = this.lifecycle, owner: ViewModelStoreOwner = this): ViewModelDelegate<VM> {
    val view: BaseView = if (this is BaseActivity<*>) {
        this
    } else {
        throw IllegalArgumentException("Unsupported Activity type: ${this::class.simpleName}")
    }
    return ViewModelDelegate(VM::class.java, lifecycle, owner, this, view)
}

inline fun <reified VM : BaseViewModel> Fragment.viewModels(lifecycle: Lifecycle = this.lifecycle, owner: ViewModelStoreOwner = this): ViewModelDelegate<VM> {
    val view: BaseView = when (this) {
        is BaseFragment<*> -> this
        is BaseTopSheetDialogFragment<*> -> this
        is BaseBottomSheetDialogFragment<*> -> this
        else -> throw IllegalArgumentException("Unsupported Fragment type: ${this::class.simpleName}")
    }
    /**
     * Fragment 未附加到 Activity：在 Fragment 被创建之后，但还没调用 onAttach 方法与 Activity 关联时，activity 为 null。
     * Fragment 已从 Activity 分离：当 Fragment 调用 onDetach 方法和 Activity 分离后，activity 会变为 null。
     * Activity 被销毁：若 Activity 被销毁，Fragment 的 activity 属性也会变成 null。
     */
    return ViewModelDelegate(VM::class.java, lifecycle, owner, activity, view)
}