package com.example.framework.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

/**
 * author:wyb
 * 统一提示框
 */
@SuppressLint("ShowToast")
object ToastUtil {
    private var toast: Toast? = null

    @JvmStatic
    fun mackToastSHORT(str: String, context: Context) {
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(str)
        }
        toast?.show()

    }

    @JvmStatic
    fun mackToastLONG(str: String, context: Context) {
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_LONG)
        } else {
            toast?.setText(str)
        }
        toast?.show()
    }

}