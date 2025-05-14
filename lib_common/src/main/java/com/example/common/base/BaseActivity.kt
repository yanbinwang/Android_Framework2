package com.example.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.listener.OnPageChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.interf.TransparentOwner
import com.example.common.base.page.navigation
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.network.socket.topic.WebSocketObserver
import com.example.common.utils.DataBooleanCache
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.function.registerResultWrapper
import com.example.common.utils.manager.AppManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.color
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.value.hasAnnotation
import com.example.framework.utils.function.value.isMainThread
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import java.lang.reflect.ParameterizedType
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的Activity和Context
 * 無xml的界面，泛型括號裡傳ViewDataBinding
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<VDB : ViewDataBinding?> : AppCompatActivity(), BaseImpl, BaseView, CoroutineScope {
    protected var mBinding: VDB? = null
    protected val mClassName get() = javaClass.simpleName.lowercase(Locale.getDefault())
    protected val mResultWrapper = registerResultWrapper()
    protected val mActivityResult = mResultWrapper.registerResult { onActivityResultListener?.invoke(it) }
    protected val mDialog by lazy { AppDialog(this) }
    protected val mPermission by lazy { PermissionHelper(this) }
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { LoadingDialog(this) }//刷新球控件，相当于加载动画
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }
    private val job = SupervisorJob()//https://blog.csdn.net/chuyouyinghe/article/details/123057776
    override val coroutineContext: CoroutineContext get() = Main + job//加上SupervisorJob，提升协程作用域

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    companion object {
        var onFinishListener: OnFinishListener? = null
        var isAnyActivityStarting = false

        fun Context.startActivity(cls: Class<out Activity>, vararg pairs: Pair<String, Any?>) {
            startActivity(getIntent(cls, *pairs).apply {
                if (this@startActivity is Application) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
            if (BaseActivity::class.java.isAssignableFrom(cls)) isAnyActivityStarting = true
        }

        fun Activity.startActivityForResult(cls: Class<out Activity>, requestCode: Int, vararg pairs: Pair<String, Any?>) {
            startActivityForResult(getIntent(cls, *pairs), requestCode)
            if (BaseActivity::class.java.isAssignableFrom(cls)) isAnyActivityStarting = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (needTransparentOwner) {
            overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_none)
            requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        AppManager.addActivity(this)
        WebSocketObserver.addObserver(this)
        isAnyActivityStarting = false
        if (isEventBusEnabled()) {
            EventBus.instance.register(this) {
                it.onEvent()
            }
        }
        if (isImmersionBarEnabled()) initImmersionBar()
        initView(savedInstanceState)
        initEvent()
        initData()
    }

    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    /**
     * 不需要binding的页面《》内传Nothing，并调取该方法
     */
    protected open fun isBindingEnabled(): Boolean {
        return true
    }

//    override fun <VM : BaseViewModel> VM.create(): VM? {
//        return javaClass.create(lifecycle, this@BaseActivity).also { it.initialize(this@BaseActivity, this@BaseActivity) }
//    }

    override fun initImmersionBar(titleDark: Boolean, naviTrans: Boolean, navigationBarColor: Int) {
        super.initImmersionBar(titleDark, naviTrans, navigationBarColor)
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
        if (isBindingEnabled()) {
            val type = javaClass.genericSuperclass
            if (type is ParameterizedType) {
                try {
                    val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                    val method = vdbClass?.getDeclaredMethod("inflate", LayoutInflater::class.java)
                    mBinding = method?.invoke(null, layoutInflater) as? VDB
                    mBinding?.lifecycleOwner = this
                    setContentView(mBinding?.root)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun getResources(): Resources {
        //AutoSize的防止界面错乱的措施,同时确认其在主线程运行
        if (isMainThread) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        }
        return super.getResources()
    }

    override fun onStop() {
        super.onStop()
        AutoSizeConfig.getInstance().stop(this)
    }

    override fun onRestart() {
        super.onRestart()
        AutoSizeConfig.getInstance().restart()
    }

    override fun finish() {
        onFinishListener?.onFinish(this)
        super.finish()
        if (needTransparentOwner) overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_in)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearOnActivityResultListener()
        AppManager.removeActivity(this)
        for ((key, value) in dataManager) {
            key.removeObserver(value)
        }
        dataManager.clear()
        mActivityResult.unregister()
        mBinding?.unbind()
        job.cancel()//之后再起的job无法工作
//        coroutineContext.cancelChildren()//之后再起的可以工作
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面管理方法">
    /**
     * 实际开发中，严禁new一个activity，这是安卓框架所不允许的，故而跳转的回调也只能开给子类，但是fragment可以公开
     */
    protected open fun setOnActivityResultListener(onActivityResultListener: ((result: ActivityResult) -> Unit)) {
        this.onActivityResultListener = onActivityResultListener
    }

    protected open fun clearOnActivityResultListener() {
        onActivityResultListener = null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }

    /**
     * ViewModel 中定义无值事件（用 Unit 替代 Any）
     * val reason by lazy { MutableLiveData<Unit>() } // 无值事件
     * Unit 类型的 value 是 Unit 实例（非 null），会触发回调
     */
    protected open fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value ->
            if (value != null) {
                (value as? T)?.let { block(it) }
            }
        }
        dataManager[this] = observer
        observe(this@BaseActivity, observer)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun showDialog(flag: Boolean, second: Long, block: () -> Unit) {
        loadingDialog.apply { setDialogCancelable(flag) }.show()
        if (second > 0) {
            TimerBuilder.schedule(this, {
                hideDialog()
                block.invoke()
            }, second)
        }
    }

    override fun hideDialog() {
        loadingDialog.dismiss()
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

    override fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity? {
        navigation(path, params = params, activityResultValue = mActivityResult)
        return this
    }
    // </editor-fold>

}

fun AppCompatActivity.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context, start, block)

fun <T> AppCompatActivity.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = lifecycleScope.async(context, start, block)

val BaseActivity<*>.needTransparentOwner get() = hasAnnotation(TransparentOwner::class.java)

interface OnFinishListener {
    fun onFinish(act: BaseActivity<*>)
}

interface OnCreateListener {
    fun onCreate(act: BaseActivity<*>)
}