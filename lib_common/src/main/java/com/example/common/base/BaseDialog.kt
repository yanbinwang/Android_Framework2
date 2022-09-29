package com.example.common.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import com.example.base.utils.function.inAnimation
import com.example.base.utils.function.outAnimation
import com.example.common.R
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类
 */
abstract class BaseDialog<VDB : ViewDataBinding>(context: Context, themeResId: Int = R.style.appDialogStyle, anim: Boolean = false, close: Boolean = false) : Dialog(context, themeResId) {
    protected lateinit var binding: VDB

    init {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vbClass?.getMethod("inflate", LayoutInflater::class.java)
                binding = method?.invoke(null, layoutInflater) as VDB
            } catch (ignored: Exception) {
            }
            setContentView(binding.root, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            if (anim) {
                val mAnimIn = context.inAnimation()
                val mAnimOut = context.outAnimation()
                //当布局show出来的时候执行开始动画
                setOnShowListener { binding.root.startAnimation(mAnimIn) }
                //当布局销毁时执行结束动画
                setOnDismissListener { binding.root.startAnimation(mAnimOut) }
            }
            if (close) {
                setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
                setCancelable(true)
            }
        }
    }

}