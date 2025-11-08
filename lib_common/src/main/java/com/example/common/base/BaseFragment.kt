package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.listener.OnPageChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.navigation
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.utils.DataBooleanCache
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.function.color
import com.example.common.utils.function.registerResultWrapper
import com.example.common.utils.manager.AppManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.isMainThread
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/6/4.
 * 在 Fragment 中使用协程时，必须使用 viewLifecycleOwner.lifecycleScope 而非 lifecycleScope，以确保协程在视图销毁时自动取消，避免内存泄漏
 *
 * onAttach()‌：当Fragment与Activity关联时调用。
 * onCreate()‌：在Fragment创建时调用。
 * onCreateView()‌：创建Fragment的用户界面。
 * onActivityCreated()‌：当与Fragment关联的Activity的onCreate()方法执行完毕时调用。
 * onStart()‌：当Fragment可见时调用。
 * onResume()‌：当Fragment获取焦点并可与用户交互时调用。
 * onPause()‌：当Fragment失去焦点或者被其他Fragment覆盖时调用。
 * onStop()‌：当Fragment不再可见时调用。
 * onDestroyView()‌：当Fragment的视图被移除时调用。
 * onDestroy()‌：当Fragment被销毁时调用。
 * onDetach()‌：当Fragment与Activity解除关联时调用‌
 *
 * 当在 Fragment 中使用 fragment.viewLifecycleOwnerLiveData.observe() 监听视图生命周期，
 * 且对应的 Activity 调用 finish() 时，Fragment 的生命周期执行流程及 viewLifecycleOwner 的行为如下：
 * 1. Activity 销毁流程
 * Activity.finish() 会触发 Activity 的销毁流程，顺序为：
 * Activity.onPause() → Activity.onStop() → Activity.onDestroy()
 * 2. Fragment 生命周期响应
 * Fragment 作为 Activity 的子组件，其生命周期会随 Activity 销毁而逐步执行，关键步骤：
 * Fragment.onPause() → Fragment.onStop() → Fragment.onDestroyView() → Fragment.onDestroy() → Fragment.onDetach()
 * viewLifecycleOwnerLiveData 的作用范围
 * viewLifecycleOwner 的生命周期：绑定 Fragment 的 视图生命周期（从 onCreateView() 到 onDestroyView()）。
 * 监听行为：当 Activity.finish() 导致 Fragment 视图销毁时（即触发 Fragment.onDestroyView()），
 * viewLifecycleOwner 的生命周期状态会变为 DESTROYED，所有基于它的观察者（包括 viewLifecycleOwnerLiveData 的回调）会自动停止观察，无需手动取消。
 */
@Suppress("UNCHECKED_CAST")
@SuppressLint("UseRequireInsteadOfGet")
abstract class BaseFragment<VDB : ViewDataBinding> : Fragment(), BaseImpl, BaseView, CoroutineScope {
    protected var lazyData = false
    protected var mBinding: VDB? = null
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity? get() { return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity }
    protected val mClassName get() = javaClass.simpleName.lowercase(Locale.getDefault())
    protected val mResultWrapper = registerResultWrapper()
    protected val mActivityResult = mResultWrapper.registerResult { onActivityResultListener?.invoke(it) }
    protected val mDialog by lazy { mActivity?.let { AppDialog(it) } }
    protected val mPermission by lazy { mActivity?.let { PermissionHelper(it) } }
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { mActivity?.let { LoadingDialog(it) } }
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Main.immediate + job

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isEventBusEnabled()) {
            EventBus.instance.subscribe(this) {
                it.onEvent()
            }
        }
        if (isCollectEnabled()) {
            EventBus.instance.collect(this) {
                this@BaseFragment.onCollect()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (isMainThread) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(resources)
        }
        return if (isBindingEnabled()) {
            try {
                val superclass = javaClass.genericSuperclass
                val aClass = (superclass as? ParameterizedType)?.actualTypeArguments?.get(0) as? Class<*>
                val method = aClass?.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
                mBinding = method?.invoke(null, inflater, container, false) as? VDB
                mBinding?.lifecycleOwner = viewLifecycleOwner
                mBinding?.root
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initEvent()
        if (!lazyData) initData()
    }

    protected open fun isBindingEnabled(): Boolean {
        return true
    }

    override fun initImmersionBar(statusBarDark: Boolean, navigationBarDark: Boolean, navigationBarColor: Int) {
        super.initImmersionBar(statusBarDark, navigationBarDark, navigationBarColor)
        mActivity?.window?.apply {
            setStatusBarLightMode(statusBarDark)
            setNavigationBarLightMode(navigationBarDark)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            immersionBar?.apply {
                reset()
                statusBarDarkFont(statusBarDark, 0.2f)
                navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarDark else false, 0.2f)
                init()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    /**
     * 在 Fragment 中，推荐在 onDestroyView() 而非 onDestroy() 中释放 / 销毁视图相关资源，主要原因与 Fragment 的生命周期特性和视图（View）的生命周期密切相关：
     * 1. Fragment 与 View 的生命周期分离
     * Fragment 的生命周期和其管理的视图（View）生命周期是不完全同步的：
     * onDestroyView()：当 Fragment 的布局（View 层级）被销毁时调用，此时视图相关资源（如 View、ViewModel 与视图的绑定、图片缓存等）已经不再需要。
     * onDestroy()：当 Fragment 本身即将被销毁时调用，此时 Fragment 实例可能仍存在于内存中（例如配置变更时，Fragment 可能被暂时保留，仅视图被重建）。
     * 如果在 onDestroy() 中释放视图资源，可能会出现资源释放时机过早或过晚的问题：
     * 若 Fragment 因配置变更（如旋转屏幕）暂时销毁视图但自身未被销毁（onDestroy() 不会调用），视图资源会泄漏。
     * 若在 onDestroy() 中释放，可能晚于视图实际销毁的时间，导致资源占用时间过长。
     *
     * 2. 避免配置变更场景的优化
     * Android 中，屏幕旋转等配置变更会触发 Fragment 的视图重建（onDestroyView() → onCreateView()），但 Fragment 实例本身可能被系统保留（通过 setRetainInstance(true) 或默认行为）。
     * 此时：
     * onDestroyView() 会被调用（视图销毁），适合释放视图相关资源（如 View 引用、监听器、图片等）。
     * onDestroy() 不会被调用（Fragment 未销毁），若在此处释放资源，会导致重建视图时无法复用必要的非视图资源（如网络请求、数据模型等）。
     *
     * 3. 避免内存泄漏
     * 视图（View）持有对 Fragment 的引用（通过 context），若在 onDestroyView() 后仍保留 View 相关资源的引用，会导致：
     * Fragment 实例被 View 间接引用，无法被 GC 回收。
     * 即使 Fragment 最终销毁（onDestroy()），视图资源的泄漏也已发生。
     * 因此，在 onDestroyView() 中及时释放 View 引用、监听器、适配器等，能更早切断引用链，避免内存泄漏。
     *
     * onDestroyView()：负责释放视图相关资源（View、Drawable、监听器等），与视图生命周期强绑定，是最安全的时机。
     * onDestroy()：负责释放Fragment 实例级资源（如全局监听器、服务连接等），仅在 Fragment 真正销毁时调用。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        clearOnActivityResultListener()
        mActivityResult.unregister()
        mBinding?.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        for ((key, value) in dataManager) {
            key.removeObserver(value)
        }
        dataManager.clear()
        job.cancel()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面管理方法">
    protected fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value ->
            if (value != null) {
                (value as? T)?.let { block(it) }
            }
        }
        dataManager[this] = observer
        observe(this@BaseFragment, observer)
    }

    protected fun setOnActivityResultListener(onActivityResultListener: ((result: ActivityResult) -> Unit)) {
        this.onActivityResultListener = onActivityResultListener
    }

    protected fun clearOnActivityResultListener() {
        onActivityResultListener = null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }

    protected open suspend fun CoroutineScope.onCollect() {
    }

    protected open fun isCollectEnabled(): Boolean {
        return false
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun showDialog(flag: Boolean, second: Long, block: () -> Unit) {
        loadingDialog?.apply { setDialogCancelable(flag) }?.show()
        if (second > 0) {
            TimerBuilder.schedule(this, {
                hideDialog()
                block.invoke()
            }, second)
        }
    }

    override fun hideDialog() {
        loadingDialog?.dismiss()
    }

    override fun showGuide(label: String, isOnly: Boolean, vararg pages: GuidePage, guideListener: OnGuideChangedListener?, pageListener: OnPageChangedListener?) {
        val labelTag = DataBooleanCache(label)
        if (!labelTag.get()) {
            if (isOnly) labelTag.set(true)
            val builder = NewbieGuide.with(this)//传入activity
                .setLabel(label)//设置引导层标示，用于区分不同引导层，必传！否则报错
                .setOnGuideChangedListener(guideListener)
                .setOnPageChangedListener(pageListener)
                .alwaysShow(true)
            for (page in pages) {
                page.backgroundColor = color(R.color.bgOverlay)//此处处理一下阴影背景
                builder.addGuidePage(page)
            }
            builder.show()
        }
    }

    override fun navigation(path: String, vararg params: Pair<String, Any?>?, options: ActivityOptionsCompat?): Activity? {
        mActivity?.navigation(path, params = params, activityResultValue = mActivityResult, options = options)
        return mActivity
    }
    // </editor-fold>

}

//fun Fragment.launch(
//    context: CoroutineContext = EmptyCoroutineContext,
//    start: CoroutineStart = CoroutineStart.DEFAULT,
//    block: suspend CoroutineScope.() -> Unit
//) = viewLifecycleOwner.lifecycleScope.launch(context, start, block)
//
//fun <T> Fragment.async(
//    context: CoroutineContext = EmptyCoroutineContext,
//    start: CoroutineStart = CoroutineStart.DEFAULT,
//    block: suspend CoroutineScope.() -> T
//) = viewLifecycleOwner.lifecycleScope.async(context, start, block)