package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.example.base.utils.LogUtil
import com.example.base.utils.ToastUtil
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.function.value.orZero
import com.example.base.utils.function.view.*
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.bridge.BaseViewModel
import com.example.common.constant.Extras
import com.example.common.utils.AppManager
import com.example.common.utils.builder.StatusBarBuilder
import com.example.common.widget.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    protected val mActivity: FragmentActivity
        get() {
            return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity ?: FragmentActivity()
        }
    protected val statusBarBuilder by lazy { StatusBarBuilder(mActivity.window) }//状态栏工具类
    private lateinit var baseViewModel: BaseViewModel//数据模型
    private val loadingDialog by lazy { LoadingDialog(mActivity) }//刷新球控件，相当于加载动画\
    private val TAG = javaClass.simpleName.lowercase(Locale.getDefault()) //额外数据，查看log，观察当前activity是否被销毁
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM {
        baseViewModel = ViewModelProvider(this)[vmClass]
        baseViewModel.initialize(mActivity, this)
        lifecycle.addObserver(baseViewModel)
        return baseViewModel as VM
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        log(TAG)
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
                        mActivity.runOnUiThread { it.enable() }
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
        LogUtil.e(TAG, msg)
    }

    override fun showToast(msg: String) {
        ToastUtil.mackToastSHORT(msg, requireContext().applicationContext)
    }

    override fun showDialog(flag: Boolean, second: Long) {
        loadingDialog.shown(flag)
        if (second >= 0) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    hideDialog()
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
            postcard.navigation(mActivity, requestCode)
        }
        return mActivity
    }
    // </editor-fold>

}