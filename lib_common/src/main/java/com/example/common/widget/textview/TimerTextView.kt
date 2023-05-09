package com.example.common.widget.textview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.example.common.R
import com.example.framework.utils.TimerUtil
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import java.text.MessageFormat

/**
 * Created by wangyanbin
 * 倒计时textview
 */
@SuppressLint("SetTextI18n")
class TimerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val tickTxt = "已发送{0}S"
    private var timerTag = javaClass.simpleName

    init {
        text = "发送验证码"
        gravity = Gravity.CENTER
        textColor(R.color.appTheme)
        textSize(R.dimen.textSize14)
    }

    fun start(tag: String? = "", time: Long = 60) {
        if (!tag.isNullOrEmpty()) timerTag = tag
        TimerUtil.startCountDown(timerTag, { second: Long? ->
            disable()
            text = MessageFormat.format(tickTxt, second.toString())
        }, {
            enable()
            text = "重发验证码"
        }, time)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        TimerUtil.stopCountDown(timerTag)
    }

}