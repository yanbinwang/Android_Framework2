package com.example.mvvm.widget.automatic.holder

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.example.framework.utils.function.view.size
import com.example.mvvm.widget.automatic.AutomaticBean

interface AutomaticInterface {
    /**
     * 获取自动绘制对象
     */
    fun getBean(): AutomaticBean

    /**
     * 获取view的值
     */
    fun getValue(): String

    /**
     * 校验取值
     */
    fun getCheckValue(): Boolean

    /**
     * 获取这个view本身
     */
    fun getView(): View

    /**
     * 默认实现的方法，因为不同view可能有的需要事先插入父viewgroup中才能操作宽高
     * 如有特殊操作，在对应view中重写
     * override fun addToParent(parent: ViewGroup, change: Boolean) {
     *  val view = getView()
     *  parent.addView(view)
     *  view.size(100.pt, WRAP_CONTENT)
     * }
     */
    fun addToParent(parent: ViewGroup, change: Boolean) {
        val view = getView()
        parent.addView(view)
        view.size(MATCH_PARENT, WRAP_CONTENT)
    }

}