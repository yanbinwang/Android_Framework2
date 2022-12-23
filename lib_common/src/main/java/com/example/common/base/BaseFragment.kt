package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.exception.NoRouteFoundException
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.create
import com.example.common.config.Extras
import com.example.common.utils.AppManager
import com.example.common.utils.ScreenUtil
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.widget.dialog.LoadingDialog
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
import java.io.Serializable
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/6/4.
 */
@SuppressLint("UseRequireInsteadOfGet")
abstract class BaseFragment<VDB : ViewDataBinding> : Fragment(), BaseImpl, BaseView, CoroutineScope {
    protected lateinit var binding: VDB
    protected var lazyData = false
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity get() { return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity ?: FragmentActivity() }
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { LoadingDialog(mActivity) }//刷新球控件，相当于加载动画
    private val activityResultValue by lazy { registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { onActivityResultListener?.invoke(it) }}
    private val TAG = javaClass.simpleName.lowercase(Locale.getDefault()) //额外数据，查看log，观察当前activity是否被销毁
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Main + job

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    companion object {
        private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null

        fun setOnActivityResultListener(onActivityResultListener: ((result: ActivityResult) -> Unit)) {
            this.onActivityResultListener = onActivityResultListener
        }

        fun clearOnActivityResultListener() {
            onActivityResultListener = null
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        log(TAG)
        if (Looper.getMainLooper() == Looper.myLooper()) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(resources)
        }
        try {
            val superclass = javaClass.genericSuperclass
            val aClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<*>
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
            binding = method.invoke(null, layoutInflater, container, false) as VDB
        } catch (_: Exception) {
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
        if (!lazyData) initData()
    }

    override fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM {
        return vmClass.create(mActivity.lifecycle, this).also { it.initialize(mActivity, this) }
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

    override fun onDetach() {
        super.onDetach()
        binding.unbind()
        job.cancel()
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
                    launch {
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
            postcard.context = mActivity
            try {
                LogisticsCenter.completion(postcard)
                activityResultValue.launch(Intent(mActivity, postcard.destination))
            } catch (_: NoRouteFoundException) {
            }
        }
        return mActivity
    }
    // </editor-fold>

}