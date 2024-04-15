package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.create
import com.example.common.base.page.navigation
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.socket.WebSocketRequest
import com.example.common.utils.AppManager
import com.example.common.utils.DataBooleanCacheUtil
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.function.color
import com.example.common.utils.function.registerResult
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.view.*
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import org.greenrobot.eventbus.Subscribe
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Suppress("UNCHECKED_CAST")
@SuppressLint("UseRequireInsteadOfGet")
abstract class BaseFragment<VDB : ViewDataBinding> : Fragment(), BaseImpl, BaseView, CoroutineScope {
    protected var lazyData = false
    protected var mBinding: VDB? = null
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity get() { return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity ?: FragmentActivity() }
    protected val mDialog by lazy { AppDialog(mActivity) }
    protected val mPermission by lazy { PermissionHelper(mActivity) }
    protected val mActivityResult = activity.registerResult { onActivityResultListener?.invoke(it) }
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private val immersionBar by lazy { ImmersionBar.with(mActivity) }
    private val loadingDialog by lazy { LoadingDialog(mActivity) }//刷新球控件，相当于加载动画
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Main + job

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebSocketRequest.addObserver(this)
        if (isEventBusEnabled()) EventBus.instance.register(this, lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (isMainThread) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(resources)
        }
        return try {
            val superclass = javaClass.genericSuperclass
            val aClass = (superclass as? ParameterizedType)?.actualTypeArguments?.get(0) as? Class<*>
            val method = aClass?.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            mBinding = method?.invoke(null, layoutInflater, container, false) as? VDB
            mBinding?.root
        } catch (_: Exception) {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initEvent()
        if (!lazyData) initData()
    }

//    override fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM {
//        return vmClass.create(mActivity.lifecycle, this).also { it.initialize(mActivity, this) }
//    }

    override fun <VM : BaseViewModel> VM.create(): VM? {
        return javaClass.create(mActivity.lifecycle, this@BaseFragment).also { it.initialize(mActivity, this@BaseFragment) }
    }

    override fun initImmersionBar(titleDark: Boolean, naviTrans: Boolean, navigationBarColor: Int) {
        immersionBar?.apply {
            reset()
            //如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色
            //如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
            statusBarDarkFont(titleDark, 0.2f)
            navigationBarColor(navigationBarColor)?.navigationBarDarkIcon(naviTrans, 0.2f)
            init()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun enabled(vararg views: View?, second: Long) {
        views.forEach {
            if (it != null) {
                it.disable()
                WeakHandler(Looper.getMainLooper()).postDelayed({ it.enable() }, second)
            }
        }
    }

    override fun visible(vararg views: View?) {
        views.forEach { it?.visible() }
    }

    override fun invisible(vararg views: View?) {
        views.forEach { it?.invisible() }
    }

    override fun gone(vararg views: View?) {
        views.forEach { it?.gone() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isEventBusEnabled()) EventBus.instance.unregister(this)
    }

    override fun onDetach() {
        super.onDetach()
        for ((key, value) in dataManager) {
            key.removeObserver(value ?: return)
        }
        dataManager.clear()
        mBinding?.unbind()
        job.cancel()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面管理方法">
    open fun setOnActivityResultListener(onActivityResultListener: ((result: ActivityResult) -> Unit)) {
        this.onActivityResultListener = onActivityResultListener
    }

    open fun clearOnActivityResultListener() {
        onActivityResultListener = null
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

    protected open fun <T> MutableLiveData<T>?.observe(block: T?.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value -> block(value as? T) }
        dataManager[this] = observer
        observe(this@BaseFragment, observer)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun showDialog(flag: Boolean, second: Long, block: () -> Unit) {
        loadingDialog.shown(flag)
        if (second > 0) {
            TimerBuilder.schedule({
                hideDialog()
                block.invoke()
            }, second)
//            WeakHandler(Looper.getMainLooper()).postDelayed({
//                hideDialog()
//                block.invoke()
//            }, second)
        }
    }

    override fun hideDialog() {
        loadingDialog.hidden()
    }

    override fun showGuide(label: String, isOnly: Boolean, vararg pages: GuidePage, guideListener: OnGuideChangedListener?, pageListener: OnPageChangedListener?) {
        val labelTag = DataBooleanCacheUtil(label)
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

    override fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity {
        mActivity.navigation(path, params = params, activityResultValue = mActivityResult)
        return mActivity
    }
    // </editor-fold>

}