package com.example.common.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Looper
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import com.example.framework.utils.function.value.orFalse
import com.example.common.R
import com.example.common.utils.function.pt
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 用于实现上下左右弹出的效果，如有特殊需求，重写animation
 * 默认底部显示弹出
 */
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val window: Window, popupWidth: Int = MATCH_PARENT, popupHeight: Int = MATCH_PARENT, private val slideEdge: Int = BOTTOM,
    private val animation: Boolean = true, private val light: Boolean = false) : PopupWindow() {
    protected lateinit var binding: VDB
    protected val context: Context
    get() { return window.context }
    private val layoutParams by lazy { window.attributes }

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as Class<VDB>
                val method = vbClass.getMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, window.layoutInflater) as VDB
                contentView = binding.root
            } catch (_: Exception) {
            }
            width = if (popupWidth < 0) popupWidth else popupWidth.pt
            height = if (popupHeight < 0) popupHeight else popupHeight.pt
            isFocusable = true
            isOutsideTouchable = true
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            setTransition()
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                if (light) {
                    layoutParams?.alpha = 1f
                    window.attributes = layoutParams
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    //默认底部弹出，可重写
    protected open fun setTransition() {
        if (animation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                enterTransition = Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_IN
                    slideEdge = slideEdge
                }
                setExitTransition(Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_OUT
                    slideEdge = slideEdge
                })
            } else {
                animationStyle = when (slideEdge) {
                    TOP -> R.style.pushTopAnimStyle
                    BOTTOM -> R.style.pushBottomAnimStyle
                    START, LEFT -> R.style.pushLeftAnimStyle
                    else -> R.style.pushRightAnimStyle
                }
            }
        } else {
            animationStyle = -1
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    override fun showAsDropDown(anchor: View?) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor)
        } catch (_: Exception) {
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor, xoff, yoff)
        } catch (_: Exception) {
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor, xoff, yoff, gravity)
        } catch (_: Exception) {
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        if ((context as? Activity)?.isDestroyed.orFalse) return
        try {
            setAttributes()
            super.showAtLocation(parent, gravity, x, y)
        } catch (_: Exception) {
        }
    }

    private fun setAttributes() {
        if (light) {
            layoutParams?.alpha = 0.7f
            window.attributes = layoutParams
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        if ((context as? Activity)?.isDestroyed.orFalse) return
        if ((context as? Activity)?.window?.windowManager == null) return
        if (window.windowManager == null) return
        if (window.decorView.parent == null) return
        super.dismiss()
        binding.unbind()
    }

    open fun shown() {
        if (!isShowing) showAtLocation(binding.root, slideEdge, 0, 0)
    }

    open fun hidden() {
        if (isShowing) dismiss()
    }
    // </editor-fold>

}