package com.example.mvvm.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.textview.edittext.PasswordEditText
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R

/**
 * 页面生命周期/父类布局（最外层）
 * 针对界面中部分模拟web端选中橙色，报错红色，正常灰色书写的输入框管理类
 */
class EditTextManager(observer: LifecycleOwner) {
    //first->所有输入框 second->所有异常原因
    private var list: ArrayList<Pair<View?, TextView?>>? = null
    //正常/选中/报错
    private val colorRes by lazy { Triple(drawable(R.color.inputNormal), drawable(R.color.inputFocused), drawable(R.color.inputError)) }

    init {
        observer.doOnDestroy {
            list?.clear()
        }
    }

    /**
     * 外层输入框线条绘制
     */
    private fun drawable(@ColorRes res: Int): Drawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, color(res))
            cornerRadius = 4.ptFloat
        }
    }

    /**
     * 绑定一整个页面所有的输入框/报错view
     */
    fun bind(vararg views: Pair<View?, TextView?>): EditTextManager {
        list = arrayListOf(*views)
        return this
    }

    /**
     * 构建
     */
    fun build(): EditTextManager {
        init()
        return this
    }

    private fun init() {
        list?.forEach {
            val view = it.first
            //针对控件赋值
            view?.background = colorRes.first
            //如果对应输入框没有快捷输入
            when (view) {
                is ClearEditText -> view.editText
                is PasswordEditText -> view.editText
                else -> view as? EditText
            }?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                normal()
                if (hasFocus) view?.background = colorRes.second
            }
            val textView = it.second
            textView.textColor(R.color.textRed)
            textView.gone()
        }
    }

    /**
     * 常态
     */
    fun normal() {
        list?.forEach {
            //隐藏所有reason错误
            it.first?.gone()
            //所有选中都初始化
            it.second?.background = colorRes.first
        }
    }

    /**
     * 传入对应下标，告知失败原因
     */
    fun reason(index: Int, reason: String? = null) {
        list?.forEach {
            it.second.gone()
        }
        val pair = list.safeGet(index)
        val view = pair?.first
        view?.background = colorRes.third
        val textView = pair?.second
        textView.visible()
        textView?.text = reason.orEmpty()
    }

}