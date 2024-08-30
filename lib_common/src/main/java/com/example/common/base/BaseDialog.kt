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
import com.example.framework.utils.AnimationUtil.Companion.enterAnimation
import com.example.framework.utils.AnimationUtil.Companion.exitAnimation
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logE
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 外层无需绘制额外布局，但需要指定宽高，默认情况下是居中的，可设置对应角度
 * 需要注意binding使用了lateinit，但是不可能会不给值（VDB），稳妥期间，引用到view的地方，使用dialogView
 * window?.setWindowAnimations(R.style.pushRightAnimStyle)
 * window?.setGravity(Gravity.TOP xor Gravity.END)
 */
@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class BaseDialog<VDB : ViewDataBinding>(context: Context, dialogWidth: Int = 320, dialogHeight: Int = WRAP_CONTENT, gravity: Int = CENTER, themeResId: Int = R.style.DialogStyle, animation: Boolean = true, close: Boolean = true) : Dialog(context, themeResId) {
    protected var mBinding: VDB? = null
    private var dialogView: View? = null

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vdbClass?.getMethod("inflate", LayoutInflater::class.java)
                mBinding = method?.invoke(null, layoutInflater) as? VDB
                mBinding?.root?.let { setContentView(it) }
                dialogView = mBinding?.root
            } catch (_: Exception) {
            }
        }
        window?.let {
            val lp = it.attributes
            lp.width = if (dialogWidth < 0) dialogWidth else dialogWidth.pt
            lp.height = if (dialogHeight < 0) dialogHeight else dialogHeight.pt
            it.attributes = lp
            it.setGravity(gravity)
        }
        if (animation) {
            //当布局show出来的时候执行开始动画
            setOnShowListener { dialogView?.startAnimation(context.enterAnimation()) }
            //当布局销毁时执行结束动画
            setOnDismissListener { dialogView?.startAnimation(context.exitAnimation()) }
        }
        if (close) {
            setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
            setCancelable(false)
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
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