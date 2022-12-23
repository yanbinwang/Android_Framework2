package com.example.common.widget.textview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.example.framework.utils.TimerUtil
import com.example.framework.utils.function.string

/**
 * Created by wangyanbin
 * 倒计时textview
 * 配置enable的xml和默認text文案即可
 */
@SuppressLint("SetTextI18n")
class TimerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val tickTxt = "已发送{0}S"
    private var timerTag = javaClass.simpleName

    init {
        gravity = Gravity.CENTER
    }

    fun start(tag: String? = "", time: Long = 60) {
        if (!tag.isNullOrEmpty()) timerTag = tag
        TimerUtil.startCountDown(timerTag, { second: Long? ->
            isEnabled = false
            text = context.string(tickTxt, second.toString())
        }, {
            isEnabled = true
            text = "重发验证码"
        }, time)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        TimerUtil.stopCountDown(timerTag)
    }


}