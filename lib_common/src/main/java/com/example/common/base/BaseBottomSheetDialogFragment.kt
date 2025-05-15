package com.example.common.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.listener.OnPageChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.common.BaseApplication
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
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.dialog.LoadingDialog
import com.example.common.widget.textview.edittext.SpecialEditText
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logE
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
 * 底部弹框使用的dialog
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseBottomSheetDialogFragment<VDB : ViewDataBinding?> : BottomSheetDialogFragment(), CoroutineScope, BaseImpl, BaseView {
    protected var mBinding: VDB? = null
    protected var mContext: Context? = null
    protected val mActivity: FragmentActivity? get() { return WeakReference(activity).get() ?: AppManager.currentActivity() as? FragmentActivity }
    protected val mClassName get() = javaClass.simpleName.lowercase(Locale.getDefault())
    protected val mResultWrapper = registerResultWrapper()
    protected val mActivityResult = mResultWrapper.registerResult { onActivityResultListener?.invoke(it) }
    protected val mDialog by lazy { mActivity?.let { AppDialog(it) } }
    protected val mPermission by lazy { mActivity?.let { PermissionHelper(it) } }
    private var showTime = 0L
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private val isShow: Boolean get() = dialog?.isShowing.orFalse && !isRemoving
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { mActivity?.let { LoadingDialog(it) } }//刷新球控件，相当于加载动画
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
            EventBus.instance.register(this) {
                it.onEvent()
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
                mBinding?.lifecycleOwner = this
                mBinding?.root
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle) {
            override fun dismiss() {
                hideSoftKeyboard()
                super.dismiss()
            }

            protected fun hideSoftKeyboard() {
                val inputMethodManager = BaseApplication.instance.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(mBinding?.root?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            /**
             * 点击EditText之外的部分关闭软键盘
             */
            private var flagMove = false
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> flagMove = false
                    MotionEvent.ACTION_MOVE -> flagMove = true
                    MotionEvent.ACTION_UP -> {
                        if (!flagMove) {
                            val v = currentFocus
                            if (isShouldHideInput(v, ev)) {
                                clearEditTextFocus(v)
                                hideInputMethod(v)
                            }
                            return super.dispatchTouchEvent(ev)
                        }
                    }
                }
                //必不可少，否则所有的组件都不会有TouchEvent了
                return if (window?.superDispatchTouchEvent(ev).orFalse) true else onTouchEvent(ev)
            }

            protected fun hideInputMethod(v: View?) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(v?.windowToken, 0)
            }

            /**
             * 设置EditText失去焦点
             */
            private fun clearEditTextFocus(view: View?) {
                if (view != null && view is EditText) {
                    view.clearFocus()
                }
            }

            private fun View.findSpecialEditTextParent(maxTimes: Int): View? {
                var view = this
                for (i in 0..maxTimes) {
                    if (view is SpecialEditText) return view
                    view = view.parent as? View ?: return null
                }
                return null
            }

            /**
             * 判断是否应该隐藏软键盘
             */
            private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
                if (v != null && (v is EditText || v is AppCompatEditText || v is SpecialEditText)) {
                    val leftTop = intArrayOf(0, 0)
                    val width: Int
                    val height: Int
                    //获取输入框当前的location位置
                    val parent = v.findSpecialEditTextParent(5)
                    if (parent != null) {
                        parent.getLocationInWindow(leftTop)
                        height = parent.height
                        width = parent.width
                    } else {
                        v.getLocationInWindow(leftTop)
                        height = v.height
                        width = v.width
                    }
                    val left = leftTop[0]
                    val top = leftTop[1]
                    val bottom = top + height
                    val right = left + width
                    //点击的是输入框区域，保留点击EditText的事件
                    return !(event.rawX.toInt() in left..right && event.rawY.toInt() in top..bottom)
                }
                return false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
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
        try {
            super.dismissAllowingStateLoss()
        } catch (e: Exception) {
            e.logE
        }
    }

    protected open fun isBindingEnabled(): Boolean {
        return true
    }

//    override fun <VM : BaseViewModel> VM.create(): VM? {
//        return javaClass.create(viewLifecycleOwner.lifecycle, this@BaseBottomSheetDialogFragment).also { it.initialize(mActivity, this@BaseBottomSheetDialogFragment) }
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
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun onStart() {
        super.onStart()
        //设置软键盘不自动弹出
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
//        //本身完全弹出
//        (view?.parent as? View)?.let {
//            runCatching {
//                BottomSheetBehavior.from(it).peekHeight = screenHeight
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearOnActivityResultListener()
        for ((key, value) in dataManager) {
            key.removeObserver(value)
        }
        dataManager.clear()
        mActivityResult.unregister()
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

    open fun show(manager: FragmentManager) {
        val tag = javaClass.simpleName.toLowerCase(Locale.getDefault())
        show(manager, tag)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }

    protected open fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value ->
            if (value != null) {
                (value as? T)?.let { block(it) }
            }
        }
        dataManager[this] = observer
        observe(this@BaseBottomSheetDialogFragment, observer)
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

    override fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity? {
        mActivity?.navigation(path, params = params, activityResultValue = mActivityResult)
        return mActivity
    }
    // </editor-fold>

}