package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Looper
import android.transition.Slide
import android.transition.Visibility
import android.view.*
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import com.example.base.utils.LogUtil
import com.example.base.utils.function.value.orFalse
import com.example.common.R
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
@SuppressLint("NewApi")
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val activity: Activity, private val dark: Boolean = false) : PopupWindow() {
    protected lateinit var binding: VDB
    private val layoutParams by lazy { activity.window?.attributes }
    protected val mActivity: Activity
        get() {
            return WeakReference(activity).get() ?: activity
        }

    init {
        initialize()
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected fun initialize() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vbClass?.getMethod("inflate", LayoutInflater::class.java)
                binding = method?.invoke(null, activity.layoutInflater) as VDB
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    override fun showAsDropDown(anchor: View?) {
        if (show()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor)
            } catch (e: Exception) {
                LogUtil.e(e.toString())
            }
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (show()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor, xoff, yoff)
            } catch (e: Exception) {
                LogUtil.e(e.toString())
            }
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        if (show()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor, xoff, yoff, gravity)
            } catch (e: Exception) {
                LogUtil.e(e.toString())
            }
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (show()) {
            try {
                setShowAttributes()
                super.showAtLocation(parent, gravity, x, y)
            } catch (e: Exception) {
                LogUtil.e(e.toString())
            }
        }
    }

    private fun show(): Boolean {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return false
        if (activity.isFinishing.orFalse) return false
        if (activity.isDestroyed.orFalse) return false
        if (isShowing) return false
        return true
    }

    override fun dismiss() {
        if (!isShowing) return
        if (activity.isFinishing.orFalse) return
        if (activity.isDestroyed.orFalse) return
        if (activity.window?.windowManager == null) return
        super.dismiss()
    }

    private fun setShowAttributes() {
        if (dark) {
            layoutParams?.alpha = 0.7f
            activity.window?.attributes = layoutParams
        }
    }

    private fun setDismissAttributes() {
        if (dark) {
            setOnDismissListener {
                layoutParams?.alpha = 1f
                activity.window?.attributes = layoutParams
            }
        }
    }
    // </editor-fold>

}