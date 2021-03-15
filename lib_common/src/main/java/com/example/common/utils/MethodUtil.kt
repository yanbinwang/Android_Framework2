package com.example.common.utils

import android.annotation.SuppressLint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dataqin.common.utils.helper.TimeTaskHelper

/**
 *  Created by wangyanbin
 *  按钮，控件行为工具类
 */
@SuppressLint("SetTextI18n")
object MethodUtil {

    /**
     * 密码是否可见
     */
    fun passwordDisplay(isDisplay: Boolean, editText: EditText, imageView: ImageView): Boolean {
        if (!isDisplay) {
            // display password text, for example "123456"
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            try {
                editText.setSelection(editText.text.length)
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            imageView.setBackgroundResource(R.mipmap.ic_text_show)
        } else {
            // hide password, display "."
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            try {
                editText.setSelection(editText.text.length)
            } catch (e: Exception) {
                e.printStackTrace()
            }
//            imageView.setBackgroundResource(R.mipmap.ic_text_hide)
        }
        editText.postInvalidate()
        return !isDisplay
    }

    /**
     * 倒计时
     */
    fun countDown(text: TextView, second: Long) {
        TimeTaskHelper.startCountDown(second, object : TimeTaskHelper.OnCountDownListener {
            override fun onTick(second: Long) {
                text.isEnabled = false
                text.text = "已发送 $second S"
            }

            override fun onFinish() {
                text.isEnabled = true
                text.text = "重发验证码"
            }
        })
    }

    /**
     * 计时-销毁
     */
    fun destroy() {
        TimeTaskHelper.destroy()
    }

}