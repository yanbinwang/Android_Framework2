package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.example.base.utils.TimerHelper

/**
 * Created by wangyanbin
 * 倒计时textview
 * 配置enable的xml和默認text文案即可
 */
@SuppressLint("AppCompatCustomView", "SetTextI18n")
class TimeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
    }

    fun countDown(time: Long = 60) {
        TimerHelper.startDownTask({ second: Long? ->
            isEnabled = false
            text = "已发送${second}S"
        }, {
            isEnabled = true
            text = "重发验证码"
        }, time)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        TimerHelper.stopDownTask()
    }

}