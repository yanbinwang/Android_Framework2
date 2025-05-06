package com.example.common.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Looper
import android.view.Gravity.CENTER
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import com.example.common.R
import com.example.common.utils.function.pt
import com.example.framework.utils.PropertyAnimator.Companion.elasticityEnter
import com.example.framework.utils.PropertyAnimator.Companion.elasticityExit
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
 */
@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class BaseDialog<VDB : ViewDataBinding>(context: Context, dialogWidth: Int = 320, dialogHeight: Int = WRAP_CONTENT, gravity: Int = CENTER, themeResId: Int = R.style.DialogStyle, animation: Boolean = true, close: Boolean = true) : Dialog(context, themeResId) {
    private val rootView get() = mBinding?.root
    protected var mBinding: VDB? = null

    init {
        initBinding()
        window?.let {
            val lp = it.attributes
            lp.width = if (dialogWidth < 0) dialogWidth else dialogWidth.pt
            lp.height = if (dialogHeight < 0) dialogHeight else dialogHeight.pt
            it.attributes = lp
            it.setGravity(gravity)
        }
        if (animation) {
            //当布局show出来的时候执行开始动画
            setOnShowListener { rootView?.startAnimation(context.elasticityEnter()) }
            //当布局销毁时执行结束动画
            setOnDismissListener { rootView?.startAnimation(context.elasticityExit()) }
        }
        if (close) {
            setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
            setCancelable(false)
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    private fun initBinding() {
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
    }

    open fun setType() {
        window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
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
        mBinding?.unbind()
    }

    open fun shown(flag: Boolean = false) {
        setCancelable(flag)
        if (!isShowing) show()
    }

    open fun hidden() {
        if (isShowing) dismiss()
    }
    // </editor-fold>

}