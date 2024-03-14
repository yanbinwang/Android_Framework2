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

class InputBuilder(observer: LifecycleOwner) {
    //父类布局
    private var root: View? = null
    //所有异常原因
    private var textList: ArrayList<TextView?>? = null
    //所有输入框
    private var editList: ArrayList<View?>? = null
    //正常/选中/报错
    private val colorRes by lazy { Triple(drawable(R.color.inputNormal), drawable(R.color.inputFocused), drawable(R.color.inputError)) }

    init {
        observer.doOnDestroy {
            root = null
            editList?.clear()
            textList?.clear()
        }
    }

    /**
     * 绑定一整个页面所有的输入框
     */
    fun bindEdits(vararg edits: View?): InputBuilder {
        this.editList = arrayListOf(*edits)
        return this
    }

    /**
     * 绑定一整个页面所有的异常文案
     */
    fun bindTexts(vararg texts: TextView?): InputBuilder {
        this.textList = arrayListOf(*texts)
        return this
    }

    /**
     * 构建
     */
    fun build(): InputBuilder {
        init()
        return this
    }

    private fun init() {
        editList?.forEachIndexed { index, view ->
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
        }
        textList?.forEach {
            it.textColor(R.color.textRed)
            it.gone()
        }
    }

    private fun drawable(@ColorRes res: Int): Drawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, color(res))
            cornerRadius = 4.ptFloat
        }
    }

    /**
     * 初始化
     */
    fun normal() {
        //隐藏所有reason错误
        textList?.forEach { it.gone() }
        //所有选中都初始化
        editList?.forEach { it?.background = colorRes.first }
    }

    /**
     * 传入对应下标，告知失败原因
     */
    fun reason(index: Int, reason: String? = null) {
        val textView = textList.safeGet(index)
        textList?.forEach { it.gone() }
        textView.visible()
        textView?.text = reason.orEmpty()
        editList.safeGet(index)?.background = colorRes.third
    }

}