package com.example.mvvm.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.findIndexOf
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R

class SelectionManager(observer: LifecycleOwner) {
    private var editList: ArrayList<EditText>? = null //输入框
    private var textList: ArrayList<TextView>? = null//内容
    private val editTriple by lazy {
        Triple(GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, "#E8E8E8".parseColor())
            cornerRadius = 4.ptFloat
        }, GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, "#FFAF0E".parseColor())
            cornerRadius = 4.ptFloat
        }, GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, "#E25728".parseColor())
            cornerRadius = 4.ptFloat
        })//正常/选中/报错
    }

    init {
        observer.doOnDestroy {
            editList?.clear()
            textList?.clear()
        }
    }

    fun bind(editList: ArrayList<EditText>? = null, textList: ArrayList<TextView>? = null) {
        this.editList = editList
        this.textList = textList
        init()
    }

    private fun init() {
        editList?.forEachIndexed { _, editText ->
            editText.background = editTriple.first
            editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                //隐藏所有reason错误
                textList?.forEach { it.gone() }
                //所有选中都初始化
                editList?.forEach { it.background = editTriple.first }
                //选中的为橙色高亮
                if (hasFocus) editText.background = editTriple.second
            }
        }
        textList?.forEach {
            it.textColor(R.color.textRed)
            it.gone()
        }
    }

    fun setReason(textView: TextView?, reason: String) {
        textView ?: return
        textList?.forEach { it.gone() }
        textView.visible()
        textView.text = reason
        val index = textList?.findIndexOf { it == textView }.orZero
//        editList.safeGet(index)?.focus()
        editList.safeGet(index)?.background = editTriple.third
    }

}