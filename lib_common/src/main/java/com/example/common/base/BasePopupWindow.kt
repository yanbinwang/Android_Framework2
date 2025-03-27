package com.example.common.base

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Looper
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.PopupAnimType.*
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 用于实现上下左右弹出的效果，如有特殊动画需求，重写animation
 * 默认底部弹出,不需要传view，并带有顶栏间距
 * 也可使用渐隐显示，默认view下方弹出
 * popupwindows在设置isClippingEnabled=false后会撑满整个屏幕变成全屏
 * 但这会使底部有虚拟栏的手机重叠，哪怕使用的margin底部高度的代码，部分手机兼容性上也会存在问题
 * 可以使用BaseBottomSheetDialogFragment替代，也可以使用调整windos透明度的方法
 * 需要注意binding使用了lateinit，但是不可能会不给值（VDB），稳妥期间，引用到view的地方，使用popupView
 */
@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val activity: FragmentActivity, popupWidth: Int = MATCH_PARENT, popupHeight: Int = WRAP_CONTENT, private val popupAnimStyle: PopupAnimType = NONE, private val light: Boolean = true) : PopupWindow() {
    private val window get() = activity.window
    private val layoutParams by lazy { window.attributes }
    private var popupView: View? = null
    protected val mContext get() = window.context
    protected var mBinding: VDB? = null
    protected var measuredWidth = 0
        private set
    protected var measuredHeight = 0
        private set

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vdbClass?.getMethod("inflate", LayoutInflater::class.java)
                mBinding = method?.invoke(null, window.layoutInflater) as? VDB
                contentView = mBinding?.root
                popupView = mBinding?.root
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        width = if (popupWidth < 0) popupWidth else popupWidth.pt
        height = if (popupHeight < 0) popupHeight else popupHeight.pt
        isFocusable = true
        isOutsideTouchable = true
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        setAnimation()
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnDismissListener {
            if (light) {
                layoutParams?.alpha = 1f
                window.attributes = layoutParams
            }
        }
        //获取自身的长宽高
        popupView?.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        measuredWidth = popupView?.measuredWidth.orZero
        measuredHeight = popupView?.measuredHeight.orZero
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    //默认底部弹出，可重写
    private fun setAnimation() {
        when (popupAnimStyle) {
            ALPHA -> animationStyle = R.style.PopupAlphaAnimStyle
            TRANSLATE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    enterTransition = Slide().apply {
                        duration = 500
                        mode = Visibility.MODE_IN
                        slideEdge = BOTTOM
                    }
                    setExitTransition(Slide().apply {
                        duration = 500
                        mode = Visibility.MODE_OUT
                        slideEdge = BOTTOM
                    })
                } else {
                    animationStyle = R.style.PopupTranslateAnimStyle
                }
            }
            NONE -> animationStyle = -1
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    override fun showAsDropDown(anchor: View?) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (popupView?.context == null) return
        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor)
        } catch (_: Exception) {
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (popupView?.context == null) return
        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor, xoff, yoff)
        } catch (_: Exception) {
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if (popupView?.context == null) return
        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
        try {
            setAttributes()
            super.showAsDropDown(anchor, xoff, yoff, gravity)
        } catch (_: Exception) {
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
        if ((mContext as? Activity)?.isFinishing.orFalse) return
        if ((mContext as? Activity)?.isDestroyed.orFalse) return
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
        if ((mContext as? Activity)?.isFinishing.orFalse) return
        if ((mContext as? Activity)?.isDestroyed.orFalse) return
        if ((mContext as? Activity)?.window?.windowManager == null) return
        if (window.windowManager == null) return
        if (window.decorView.parent == null) return
        super.dismiss()
        mBinding?.unbind()
    }

    /**
     * 默认底部坐标展示
     */
    open fun shown() {
        if (!isShowing) showAtLocation(popupView, BOTTOM, 0, 0)
    }

    /**
     * 控件上方显示(以v的中心位置/左边距->为开始位置)
     */
    open fun showUp(anchor: View?, center: Boolean = true) {
        if (!isShowing) {
            val location = IntArray(2)
            anchor?.getLocationOnScreen(location)
            showAtLocation(anchor, Gravity.NO_GRAVITY, if (center) ((location[0] + anchor?.width.orZero / 2) - measuredWidth / 2) else ((location[0]) - measuredWidth / 2), location[1] - measuredHeight)
        }
    }

    /**
     * 控件下方显示
     */
    open fun showDown(anchor: View?) {
        if (!isShowing) showAsDropDown(anchor)
    }

    open fun hidden() {
        if (isShowing) dismiss()
    }
    // </editor-fold>

}

enum class PopupAnimType {
    NONE, TRANSLATE, ALPHA
}