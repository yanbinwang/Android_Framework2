package com.example.mvvm.utils

import android.view.Gravity
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

fun setToastView(message: Int) {
    ToastBuilder.short(message) { resId, length ->
        val toast = Toast.makeText(BaseApplication.instance, null, length)
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.duration = length
        val view = BaseApplication.instance.inflate(R.layout.view_toast_image_style)
//        view.txtTitle.setI18nRes(resId)
        toast?.view = view
        toast
    }
}