package com.example.common.base

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.Gravity.CENTER
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.utils.function.pt
import com.example.framework.utils.PropertyAnimator.Companion.elasticityEnter
import com.example.framework.utils.PropertyAnimator.Companion.elasticityExit
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logE
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类(外层无需绘制额外布局，但需要指定宽高，默认情况下是居中的，可设置对应角度)
 *
 * 1.传入 Activity 作为 Context
 * 如果传入的 Context 实际上是一个 Activity 实例（因为 Activity 继承自 Context）
 * 那么 Dialog 会正确关联到这个 Activity，在这种情况下，ownerActivity 会被正确设置为传入的 Activity
 * 2.传入 Application 上下文作为 Context
 * 如果传入的是 Application 上下文，那么会对 ownerActivity 产生不良影响:
 * (1) ownerActivity缺失：
 *   Application 上下文并非 Activity 实例，所以 Dialog 无法正确关联到一个 Activity，ownerActivity 不会被正确设置。这可能导致 Dialog 在显示和管理方面出现问题
 * (2) 生命周期管理问题：
 *   Application 上下文的生命周期贯穿整个应用程序的生命周期，而不是某个具体 Activity 的生命周期。如果使用 Application 上下文创建 Dialog，Dialog 不会随着 Activity 的销毁而销毁，可能会导致内存泄漏和显示异常
 * 3.Dialog 本身没有像 Fragment/Activity 那样完整的生命周期回调，但核心阶段可以对应为：初始化 → 显示（show）→ 隐藏（dismiss）→ 销毁
 */
@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class BaseDialog<VDB : ViewDataBinding>(activity: FragmentActivity, themeResId: Int = R.style.DialogStyle, private val dialogWidth: Int = 320, private val dialogHeight: Int = WRAP_CONTENT, private val gravity: Int = CENTER, private val hasAnimation: Boolean = true) : AppCompatDialog(activity, themeResId), BaseImpl {
    protected var mBinding: VDB? = null
    protected val rootView get() = mBinding?.root
    protected val lifecycleOwner get() = ownerActivity as? LifecycleOwner

    init {
        setOwnerActivity(activity)
        initView(null)
        initEvent()
        initData()
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun initView(savedInstanceState: Bundle?) {
        // 设置内部view
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vdbClass?.getMethod("inflate", LayoutInflater::class.java)
                mBinding = method?.invoke(null, layoutInflater) as? VDB
                mBinding?.root?.let { setContentView(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        /**
         * Dialog 的 window 是独立的，但会依赖宿主的生命周期和窗口层级：宿主（Activity）的 window 是顶级窗口（TYPE_APPLICATION），
         * 而 Dialog 的 window 是子窗口（默认 TYPE_APPLICATION_DIALOG），会依附于宿主窗口显示（宿主销毁时，子窗口会被系统强制回收）
         */
        window?.let {
            val lp = it.attributes
            lp.width = if (dialogWidth < 0) dialogWidth else dialogWidth.pt
            lp.height = if (dialogHeight < 0) dialogHeight else dialogHeight.pt
            it.attributes = lp
            it.setGravity(gravity)
        }
        // 绑定宿主生命周期
        lifecycleOwner.doOnDestroy {
            mBinding?.unbind()
            mBinding = null
        }
    }

    override fun initEvent() {
        if (hasAnimation) {
            //当布局show出来的时候执行开始动画
            setOnShowListener {
                rootView?.startAnimation(context.elasticityEnter())
            }
            //当布局销毁时执行结束动画
            setOnDismissListener {
                rootView?.startAnimation(context.elasticityExit())
            }
        }
        //默认情况下，拦截所有的点击事件，且不可关闭（只能点击按钮关闭）
        setDialogCancelable(false)
    }

    override fun initData() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    override fun show() {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (ownerActivity?.isFinishing.orFalse) return
        if (ownerActivity?.isDestroyed.orFalse) return
        if (isShowing) return
        try {
            super.show()
        } catch (e: Exception) {
            e.logE
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if (ownerActivity?.isFinishing.orFalse) return
        if (ownerActivity?.isDestroyed.orFalse) return
        if (window?.windowManager == null) return
        if (window?.decorView == null) return
        if (window?.decorView?.parent == null) return
        super.dismiss()
    }

    /**
     * 设置dialog弹出的状态
     * 当 cancelable 为 true 时，允许返回键关闭 Dialog，不拦截按键事件
     * 当 cancelable 为 false 时，拦截所有按键事件
     */
    open fun setDialogCancelable(cancelable: Boolean) {
        setCancelable(cancelable)
        setCanceledOnTouchOutside(cancelable)//新增
        if (cancelable) {
            setOnKeyListener(null)
        } else {
            setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
        }
    }

    /**
     * WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY：
     * 从 Android 8.0（API 级别 26）开始引入，用于在其他应用之上显示窗口。使用该类型需要在 AndroidManifest.xml 里声明 SYSTEM_ALERT_WINDOW 权限，并且用户需要在系统设置里手动授予此权限(<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>)
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(context)) {
     *     val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
     *     context.startActivity(intent)
     * }
     * WindowManager.LayoutParams.TYPE_SYSTEM_ALERT：
     * 在 Android 8.0 之前使用，同样用于在其他应用之上显示窗口，也需要 SYSTEM_ALERT_WINDOW 权限。不过从 Android 8.0 开始，此类型已被弃用，建议使用 TYPE_APPLICATION_OVERLAY 替代。
     */
    open fun setType() {
        window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
    }
    // </editor-fold>

}