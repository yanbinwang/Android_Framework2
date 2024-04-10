package com.example.qiniu.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    @JvmStatic
    fun s(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun l(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}