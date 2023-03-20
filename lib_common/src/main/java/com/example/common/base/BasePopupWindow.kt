package com.example.common.base

import android.app.Activity
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
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orFalse
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 用于实现上下左右弹出的效果，如有特殊动画需求，重写animation
 * 默认底部弹出,不需要传view，并带有顶栏间距
 * 传view则默认view下方弹出
 * popupwindows在设置isClippingEnabled=false后会撑满整个屏幕变成全屏
 * 但这会使底部有虚拟栏的手机重叠，哪怕使用的margin底部高度的代码，部分手机兼容性上也会存在问题
 * 可以使用BaseBottomSheetDialogFragment替代，也可以使用调整windos透明度的方法
 */
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val activity: FragmentActivity, popupWidth: Int = MATCH_PARENT, popupHeight: Int = WRAP_CONTENT, private val gravity: Int = BOTTOM, private val animation: Boolean = true, private val light: Boolean = false) : PopupWindow() {
    private val window get() = activity.window
    private val layoutParams by lazy { window.attributes }
    protected val context get() = window.context
    protected lateinit var binding: VDB

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as Class<VDB>
                val method = vdbClass.getMethod("inflate", LayoutInflater::class.java)
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
    open fun setTransition() {
        if (animation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                enterTransition = Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_IN
                    slideEdge = gravity
                }
                setExitTransition(Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_OUT
                    slideEdge = gravity
                })
            } else {
                animationStyle = when (gravity) {
                    TOP -> R.style.PushTopAnimStyle
                    BOTTOM -> R.style.PushBottomAnimStyle
                    START, LEFT -> R.style.PushLeftAnimStyle
                    else -> R.style.PushRightAnimStyle
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
        if (!isShowing) showAtLocation(binding.root, gravity, 0, 0)
    }

    open fun shown(anchor: View?){
        if (!isShowing) showAsDropDown(anchor)
    }

    open fun hidden() {
        if (isShowing) dismiss()
    }
    // </editor-fold>

}