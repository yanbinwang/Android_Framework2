package com.example.common.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.example.base.utils.function.value.currentTimeNano
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.function.value.orZero
import com.example.base.utils.function.view.*
import com.example.base.utils.logE
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.bridge.BaseViewModel
import com.example.common.constant.Extras
import com.example.common.utils.AppManager
import com.example.common.utils.builder.StatusBarBuilder
import com.example.common.widget.dialog.LoadingDialog
import com.example.topsheet.TopSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @description
 * @author 顶部弹出的dialog
 * 可实现顶部弹出后，导航栏于弹框一致
 */
abstract class BaseTopSheetDialogFragment<VDB : ViewDataBinding> : TopSheetDialogFragment(), CoroutineScope, BaseImpl, BaseView {
    protected lateinit var binding: VDB
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity get() { return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity ?: FragmentActivity() }
    protected val statusBarBuilder by lazy { StatusBarBuilder(mActivity.window) }//状态栏工具类
    private var showTime = 0L
    private val isShow: Boolean get() = dialog.let { it?.isShowing.orFalse } && !isRemoving
    private val loadingDialog by lazy { LoadingDialog(mActivity) }//刷新球控件，相当于加载动画\
    private val TAG = javaClass.simpleName.lowercase(Locale.getDefault()) //额外数据，查看log，观察当前activity是否被销毁
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Main + job

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM {
        val viewModel = ViewModelProvider(this)[vmClass]
        viewModel.initialize(mActivity, this)
        lifecycle.addObserver(viewModel)
        return viewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        initData()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (isAdded) return
        if (isShow) return
        if (activity?.isFinishing.orFalse) return
        if (manager.findFragmentByTag(tag) != null) return
        if (manager.isDestroyed) return
        //防止因为意外情况连续call两次show，设置500毫秒的最低间隔
        if (currentTimeNano - showTime < 500) return
        showTime = currentTimeNano
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.logE
        }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun initView() {
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
            postcard.navigation(mActivity, requestCode)
        }
        return mActivity
    }
    // </editor-fold>

}