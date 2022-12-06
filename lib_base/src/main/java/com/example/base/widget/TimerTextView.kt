package com.example.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.example.base.utils.TimerUtil

/**
 * Created by wangyanbin
 * 倒计时textview
 * 配置enable的xml和默認text文案即可
 */
@SuppressLint("AppCompatCustomView", "SetTextI18n")
class TimerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {
    private var timerTag = javaClass.simpleName

    init {
        gravity = Gravity.CENTER
    }

    fun start(tag: String? = "", time: Long = 60) {
        if (!tag.isNullOrEmpty()) timerTag = tag
        TimerUtil.startCountDown(timerTag, { second: Long? ->
            isEnabled = false
            text = "已发送${second}S"
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