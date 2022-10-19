package com.example.common.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.view.Gravity.CENTER
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.databinding.ViewDataBinding
import com.example.base.utils.function.inAnimation
import com.example.base.utils.function.outAnimation
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.logE
import com.example.common.R
import com.example.common.utils.dp
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 外层无需绘制额外布局，但需要指定宽高
 * 默认情况下是居中的，可设置对应角度
 * window?.setWindowAnimations(R.style.pushRightAnimStyle)
 * window?.setGravity(Gravity.TOP xor Gravity.END)
 */
abstract class BaseDialog<VDB : ViewDataBinding>(context: Context, dialogWidth: Int = 320, dialogHeight: Int = WRAP_CONTENT, gravity: Int = CENTER, themeResId: Int = R.style.appDialogStyle, anim: Boolean = false, close: Boolean = false) : Dialog(context, themeResId) {
    protected lateinit var binding: VDB

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as Class<VDB>
                val method = vbClass.getMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, layoutInflater) as VDB
                setContentView(binding.root)
            } catch (_: Exception) {
            }
            window?.let { it ->
                val lp = it.attributes
                lp.width = dialogWidth.dp
                lp.height = if (dialogHeight != ViewGroup.LayoutParams.WRAP_CONTENT) dialogHeight.dp else dialogHeight
                it.attributes = lp
                it.setGravity(gravity)
            }
            if (anim) {
                //当布局show出来的时候执行开始动画
                setOnShowListener { binding.root.startAnimation(context.inAnimation()) }
                //当布局销毁时执行结束动画
                setOnDismissListener { binding.root.startAnimation(context.outAnimation()) }
            }
            if (close) {
                setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
                setCancelable(true)
            }
        }
    }

    fun shown(flag: Boolean) {
        setCancelable(flag)
        if (!isShowing) show()
    }

    fun hidden() {
        if (isShowing) dismiss()
    }

    override fun show() {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (ownerActivity?.isFinishing.orFalse) return
        if (ownerActivity?.isDestroyed.orFalse) return
        if (isShowing) return
        try {
            super.show()
        } catch (e: Exception) {
            e.toString().logE
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if (ownerActivity?.isFinishing.orFalse) return
        if (ownerActivity?.isDestroyed.orFalse) return
        if (window?.windowManager == null) return
        super.dismiss()
    }

}