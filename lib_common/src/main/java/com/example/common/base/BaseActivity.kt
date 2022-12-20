package com.example.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.preference.PreferenceManager.OnActivityResultListener
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.create
import com.example.common.config.Extras
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.utils.AppManager
import com.example.common.utils.ScreenUtil
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.*
import com.example.framework.utils.logE
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import org.greenrobot.eventbus.Subscribe
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的Activity和Context
 */
abstract class BaseActivity<VDB : ViewDataBinding> : AppCompatActivity(), BaseImpl, BaseView, CoroutineScope {
    protected lateinit var binding: VDB
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { LoadingDialog(this) }//刷新球控件，相当于加载动画
    private val TAG = javaClass.simpleName.lowercase(Locale.getDefault()) //额外数据，查看log，观察当前activity是否被销毁
    private val job = SupervisorJob()//https://blog.csdn.net/chuyouyinghe/article/details/123057776
    override val coroutineContext: CoroutineContext get() = Main + job//加上SupervisorJob，提升协程作用域

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    companion object {
        private var onActivityResultListener: OnActivityResultListener? = null
        var onFinishListener: OnFinishListener? = null

        fun Context.startActivity(cls: Class<out Activity>, vararg pairs: Pair<String, Any?>) {
            startActivity(getIntent(cls, *pairs).apply {
                if (this@startActivity is Application) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
        }

        fun Activity.startActivityForResult(cls: Class<out Activity>, requestCode: Int, vararg pairs: Pair<String, Any?>) {
            startActivityForResult(getIntent(cls, *pairs), requestCode)
        }

        fun setOnActivityResultListener(onActivityResultListener: OnActivityResultListener) {
            this.onActivityResultListener = onActivityResultListener
        }

        fun clearOnActivityResultListener() {
            onActivityResultListener = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.addActivity(this)
        if (isEventBusEnabled()) EventBus.instance.register(this, lifecycle)
        if (isImmersionBarEnabled()) initImmersionBar()
        initView()
        initEvent()
        initData()
    }

    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    override fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM {
        return vmClass.create(lifecycle, this).also { it.initialize(this, this) }
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

    override fun initView() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as Class<VDB>
                val method = vbClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, layoutInflater) as VDB
                binding.lifecycleOwner = this
                setContentView(binding.root)
            } catch (_: Exception) {
            }
        }
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun isEmpty(vararg objs: Any?): Boolean {
        objs.forEach {
            if (it == null) {
                return true
            } else if (it is String && it == "") {
                return true
            }
        }
        return false
    }

    override fun ENABLED(vararg views: View?, second: Long) {
        views.forEach {
            if (it != null) {
                it.disable()
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        launch { it.enable() }
                    }
                }, second)
            }
        }
    }

    override fun VISIBLE(vararg views: View?) {
        views.forEach { it?.visible() }
    }

    override fun INVISIBLE(vararg views: View?) {
        views.forEach { it?.invisible() }
    }

    override fun GONE(vararg views: View?) {
        views.forEach { it?.gone() }
    }

    override fun getResources(): Resources {
        //AutoSize的防止界面错乱的措施,同时确认其在主线程运行
        if (Looper.getMainLooper() == Looper.myLooper()) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(ScreenUtil.screenWidth)
                .setScreenHeight(ScreenUtil.screenHeight)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultListener?.onActivityResult(requestCode, resultCode, data)
    }

    override fun finish() {
        onFinishListener?.onFinish(this)
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.removeActivity(this)
        if (isEventBusEnabled()) EventBus.instance.unregister(this)
        binding.unbind()
        job.cancel()
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

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun log(msg: String) {
        msg.logE(TAG)
    }

    override fun showDialog(flag: Boolean, second: Long, block: () -> Unit) {
        loadingDialog.shown(flag)
        if (second >= 0) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    launch(Main) {
                        hideDialog()
                        block.invoke()
                    }
                }
            }, second)
        }
    }

    override fun hideDialog() {
        loadingDialog.hidden()
    }

    override fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity {
        val postcard = ARouter.getInstance().build(path)
        var requestCode: Int? = null
        if (params.isNotEmpty()) {
            for (param in params) {
                val key = param?.first
                val value = param?.second
                val cls = value?.javaClass
                if (key == Extras.REQUEST_CODE) {
                    requestCode = value as? Int
                    continue
                }
                when {
                    value is Parcelable -> postcard.withParcelable(key, value)
                    value is Serializable -> postcard.withSerializable(key, value)
                    cls == String::class.java -> postcard.withString(key, value as? String)
                    cls == Int::class.javaPrimitiveType -> postcard.withInt(key, (value as? Int).orZero)
                    cls == Long::class.javaPrimitiveType -> postcard.withLong(key, (value as? Long).orZero)
                    cls == Boolean::class.javaPrimitiveType -> postcard.withBoolean(key, (value as? Boolean).orFalse)
                    cls == Float::class.javaPrimitiveType -> postcard.withFloat(key, (value as? Float).orZero)
                    cls == Double::class.javaPrimitiveType -> postcard.withDouble(key, (value as? Double).orZero)
                    cls == CharArray::class.java -> postcard.withCharArray(key, value as? CharArray)
                    cls == Bundle::class.java -> postcard.withBundle(key, value as? Bundle)
                    else -> throw RuntimeException("不支持参数类型: ${cls?.simpleName}")
                }
            }
        }
        if (requestCode == null) {
            postcard.navigation()
        } else {
            postcard.navigation(this, requestCode)
        }
        return this
    }
    // </editor-fold>

}

interface OnFinishListener {
    fun onFinish(act: BaseActivity<*>)
}