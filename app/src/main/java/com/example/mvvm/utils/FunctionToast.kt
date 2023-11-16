package com.example.mvvm.utils

import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.common.BaseApplication
import com.example.common.utils.builder.ToastBuilder
import com.example.common.utils.function.setPrimaryClip
import com.example.framework.utils.function.inflate
import com.example.mvvm.R

/**
 * @description 所有自定义布局的toast
 * @author yan
 */
fun String?.copy() {
    this.orEmpty().setPrimaryClip("Copy")
//    setToastView(R.string.refShareCopySuccess)
}

fun setToastView(message: Int, resId: Int = R.mipmap.ic_toast_successful) {
    ToastBuilder.short(message) { _, length ->
        val toast = Toast.makeText(BaseApplication.instance, null, length)
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.duration = length
        val view = BaseApplication.instance.inflate(R.layout.view_toast_image_style)
        view.findViewById<ImageView>(R.id.iv_state).setBackgroundResource(resId)
        view.findViewById<TextView>(R.id.tv_content).setText(message)
        toast?.view = view
        toast
    }
}