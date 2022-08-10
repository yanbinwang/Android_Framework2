package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.transition.Slide
import android.transition.Visibility
import android.view.*
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import com.example.common.R
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
@SuppressLint("NewApi")
abstract class BasePopupWindow<VDB : ViewDataBinding> : PopupWindow {
    protected lateinit var binding: VDB
    private var weakActivity: WeakReference<Activity>? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var dark = false

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    constructor(activity: Activity) {
        init(activity)
    }

    constructor(activity: Activity, dark: Boolean = false) {
        init(activity, dark)
    }

    private fun init(activity: Activity, dark: Boolean = false) {
        this.weakActivity = WeakReference(activity)
        this.layoutParams = weakActivity?.get()?.window?.attributes
        this.dark = dark
    }

    protected open fun initialize() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vbClass?.getMethod("inflate", LayoutInflater::class.java)
                binding = method?.invoke(null, weakActivity?.get()?.layoutInflater) as VDB
            } catch (ignored: Exception) {
            }
            contentView = binding.root
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setTransition()
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            width = ViewGroup.LayoutParams.MATCH_PARENT
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            setDismissAttributes()
        }
    }

    //默认底部弹出，可重写
    protected fun setTransition(setting: Boolean = true) {
        if (setting) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                enterTransition = Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_IN
                    slideEdge = Gravity.BOTTOM
                }
                setExitTransition(Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_OUT
                    slideEdge = Gravity.BOTTOM
                })
            } else {
                animationStyle = R.style.pushBottomAnimStyle
            }
        } else {
//            enterTransition = null
//            exitTransition = null
            animationStyle = -1
        }
    }

    protected fun getActivity() = weakActivity?.get()
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    override fun showAsDropDown(anchor: View?) {
        setShowAttributes()
        super.showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        setShowAttributes()
        super.showAsDropDown(anchor, xoff, yoff)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        setShowAttributes()
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        setShowAttributes()
        super.showAtLocation(parent, gravity, x, y)
    }

    private fun setShowAttributes() {
        if (dark) {
            layoutParams?.alpha = 0.7f
            weakActivity?.get()?.window?.attributes = layoutParams
        }
    }

    private fun setDismissAttributes() {
        if (dark) {
            setOnDismissListener {
                layoutParams?.alpha = 1f
                weakActivity?.get()?.window?.attributes = layoutParams
            }
        }
    }
    // </editor-fold>

}