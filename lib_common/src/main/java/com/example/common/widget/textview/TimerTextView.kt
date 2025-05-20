package com.example.common.widget.textview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.common.utils.i18n.string
import com.example.common.widget.i18n.I18nTextView
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.value.second
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize

/**
 * Created by wangyanbin
 * 倒计时textview
 */
@SuppressLint("SetTextI18n")
class TimerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : I18nTextView(context, attrs, defStyleAttr) {
    private var timerTag = javaClass.simpleName
    private var timerBuilder: TimerBuilder? = null

    init {
        setI18nRes(R.string.timerContent)
        gravity = Gravity.CENTER
        textColor(R.color.appTheme)
        textSize(R.dimen.textSize14)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getLifecycleOwner()?.let { timerBuilder = TimerBuilder(it) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timerBuilder?.stopCountDown(timerTag)
    }

    fun start(tag: String? = "", second: Int = 60) {
        if (!tag.isNullOrEmpty()) timerTag = tag
        timerBuilder?.startCountDown(timerTag, {
            disable()
            text = string(R.string.timerPressed, it.toString())
        }, {
            enable()
            text = string(R.string.timerDisabled)
        }, second.second)
    }

}