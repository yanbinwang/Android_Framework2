package com.example.base.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.widget.Toast
import java.lang.ref.WeakReference

/**
 * author:wyb
 * 统一提示框
 */
@SuppressLint("ShowToast")
object ToastUtil {
//    private var toast: Toast? = null
    private var toast: WeakReference<Toast>? = null

    @Synchronized
    @JvmStatic
    fun mackToastSHORT(str: String, context: Context) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        toast?.get()?.cancel()
        toast = WeakReference(Toast.makeText(context, str, Toast.LENGTH_SHORT))
        toast?.get()?.show()
//        if (toast == null) {
//            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
//        } else {
//            toast?.setText(str)
//        }
//        toast?.show()
    }

    @Synchronized
    @JvmStatic
    fun mackToastLONG(str: String, context: Context) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        toast?.get()?.cancel()
        toast = WeakReference(Toast.makeText(context, str, Toast.LENGTH_LONG))
        toast?.get()?.show()
//        if (toast == null) {
//            toast = Toast.makeText(context, str, Toast.LENGTH_LONG)
//        } else {
//            toast?.setText(str)
//        }
//        toast?.show()
    }

}