package com.example.common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Looper
import android.transition.Slide
import android.transition.Visibility
import android.view.*
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import com.example.base.utils.function.value.orFalse
import com.example.common.R
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 * 外层手动绘制一个framelayout，不然宽高会有问题
 * 原则上不鼓励使用，因为要传activity
 */
@SuppressLint("NewApi")
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val activity: Activity, private val light: Boolean = false, private val anim: Boolean = true, private val edge: Int = Gravity.BOTTOM, ) : PopupWindow() {
    protected lateinit var binding: VDB
    protected val context: Context
        get() {
            return activity.baseContext
        }
    private val layoutParams by lazy { activity.window?.attributes }

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as Class<VDB>
                val method = vbClass.getMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, activity.layoutInflater) as VDB
                contentView = binding.root
            } catch (_: Exception) {
            }
            height = ViewGroup.LayoutParams.MATCH_PARENT
            width = ViewGroup.LayoutParams.MATCH_PARENT
            isFocusable = true
            isOutsideTouchable = true
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setTransition()
            setDismissAttributes()
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    //默认底部弹出，可重写
    protected open fun setTransition() {
        if (anim) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                enterTransition = Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_IN
                    slideEdge = edge
                }
                setExitTransition(Slide().apply {
                    duration = 500
                    mode = Visibility.MODE_OUT
                    slideEdge = edge
                })
            } else {
                animationStyle = when (edge) {
                    Gravity.TOP -> R.style.pushTopAnimStyle
                    Gravity.BOTTOM -> R.style.pushBottomAnimStyle
                    Gravity.START, Gravity.LEFT -> R.style.pushLeftAnimStyle
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
        if (checkShow()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor)
            } catch (_: Exception) {
            }
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (checkShow()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor, xoff, yoff)
            } catch (_: Exception) {
            }
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        if (checkShow()) {
            try {
                setShowAttributes()
                super.showAsDropDown(anchor, xoff, yoff, gravity)
            } catch (_: Exception) {
            }
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (checkShow()) {
            try {
                setShowAttributes()
                super.showAtLocation(parent, gravity, x, y)
            } catch (_: Exception) {
            }
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if (activity.isFinishing.orFalse) return
        if (activity.isDestroyed.orFalse) return
        if (activity.window?.windowManager == null) return
        super.dismiss()
        binding.unbind()
    }

    fun shown() {
        if (checkShow()) showAtLocation(binding.root, edge, 0, 0)
    }

    private fun setShowAttributes() {
        if (light) {
            layoutParams?.alpha = 0.7f
            activity.window?.attributes = layoutParams
        }
    }

    private fun checkShow(): Boolean {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) return false
        if (activity.isFinishing.orFalse) return false
        if (activity.isDestroyed.orFalse) return false
        if (isShowing) return false
        return true
    }

    private fun setDismissAttributes() {
        if (light) {
            setOnDismissListener {
                layoutParams?.alpha = 1f
                activity.window?.attributes = layoutParams
            }
        }
    }
    // </editor-fold>

}