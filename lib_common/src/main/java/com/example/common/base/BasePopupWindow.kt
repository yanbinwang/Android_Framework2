package com.example.common.base

import android.app.Activity
import android.graphics.Color
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
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.PopupAnimType.ALPHA
import com.example.common.base.PopupAnimType.NONE
import com.example.common.base.PopupAnimType.TRANSLATE
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.doOnceAfterLayout
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
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val mActivity: FragmentActivity, popupWidth: Int = MATCH_PARENT, popupHeight: Int = WRAP_CONTENT, private val popupAnimStyle: PopupAnimType = NONE, private val light: Boolean = true) : PopupWindow() {
    private val window get() = mActivity.window
    private val layoutParams by lazy { window.attributes }
    private var popupView: View? = null
    protected var mBinding: VDB? = null
    protected val mContext get() = window.context
    protected var measuredWidth = 0
        private set
    protected var measuredHeight = 0
        private set

    init {
        initBinding()
        width = if (popupWidth < 0) popupWidth else popupWidth.pt
        height = if (popupHeight < 0) popupHeight else popupHeight.pt
        isFocusable = true
        isOutsideTouchable = true
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        setAnimation()
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setOnDismissListener {
            if (light) {
                layoutParams?.alpha = 1f
                window.attributes = layoutParams
            }
        }
        measurePopupView()
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    private fun initBinding() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vdbClass?.getMethod("inflate", LayoutInflater::class.java)
                mBinding = method?.invoke(null, window.layoutInflater) as? VDB
                mBinding?.root?.let { setContentView(it) }
                popupView = mBinding?.root
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取自身的长宽高
     * 一般情况：
     * 若 popupView 及其子视图的布局参数和内容都是固定的，在调用 measure 方法之后，measuredWidth 和 measuredHeight 能够反映出 PopupWindow 根视图确切的宽高。例如，popupView 是一个包含固定文本的 TextView 或者有固定尺寸的 ImageView 等，测量得到的宽高是准确的。
     * 特殊情况：
     * 依赖外部资源：要是 popupView 依赖于外部资源（如网络图片），在资源还未加载完成时进行测量，得到的宽高可能不准确。因为在资源加载完成之前，视图并不知道其最终的大小。
     * 布局依赖于父容器：如果 popupView 的布局依赖于父容器的大小或者其他动态因素，仅使用 View.MeasureSpec.UNSPECIFIED 进行测量可能无法得到确切的宽高。例如，popupView 中有一个 LinearLayout 其 layout_weight 属性生效，在这种情况下，需要根据实际的布局参数来创建合适的 MeasureSpec 进行测量。
     */
    private fun measurePopupView() {
//        popupView?.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
//        measuredWidth = popupView?.measuredWidth.orZero
//        measuredHeight = popupView?.measuredHeight.orZero
        popupView?.doOnceAfterLayout {
            it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            measuredWidth = it.measuredWidth.orZero
            measuredHeight = it.measuredHeight.orZero
        }
    }

    /**
     * 默认底部弹出
     */
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
//    override fun showAsDropDown(anchor: View?) {
//        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
//        if (popupView?.context == null) return
//        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
//        try {
//            setAttributes()
//            super.showAsDropDown(anchor)
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
//        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
//        if (popupView?.context == null) return
//        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
//        try {
//            setAttributes()
//            super.showAsDropDown(anchor, xoff, yoff)
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
//        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
//        if (popupView?.context == null) return
//        if ((popupView?.context as? Activity)?.isFinishing.orFalse) return
//        try {
//            setAttributes()
//            super.showAsDropDown(anchor, xoff, yoff, gravity)
//        } catch (_: Exception) {
//        }
//    }
//
//    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
//        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return
//        if ((mContext as? Activity)?.isFinishing.orFalse) return
//        if ((mContext as? Activity)?.isDestroyed.orFalse) return
//        try {
//            setAttributes()
//            super.showAtLocation(parent, gravity, x, y)
//        } catch (_: Exception) {
//        }
//    }

    override fun showAsDropDown(anchor: View?) {
        showPopup({ super.showAsDropDown(anchor) }, ::checkShowAsDropDownConditions)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        showPopup({ super.showAsDropDown(anchor, xoff, yoff) }, ::checkShowAsDropDownConditions)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        showPopup({ super.showAsDropDown(anchor, xoff, yoff, gravity) }, ::checkShowAsDropDownConditions)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        showPopup({ super.showAtLocation(parent, gravity, x, y) }, ::checkShowAtLocationConditions)
    }

    private fun checkShowAsDropDownConditions() =
        Looper.myLooper() != null &&
                Looper.myLooper() == Looper.getMainLooper() &&
                popupView?.context != null &&
                (popupView?.context as? Activity)?.isFinishing == false &&
                (popupView?.context as? Activity)?.isDestroyed == false

    private fun checkShowAtLocationConditions() =
        Looper.myLooper() != null &&
                Looper.myLooper() == Looper.getMainLooper() &&
                (mContext as? Activity)?.isFinishing == false &&
                (mContext as? Activity)?.isDestroyed == false

    private fun showPopup(showFunction: () -> Unit, checkCondition: () -> Boolean) {
        if (checkCondition()) {
            try {
                setAttributes()
                showFunction()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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