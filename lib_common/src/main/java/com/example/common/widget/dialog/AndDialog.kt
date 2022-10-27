package com.example.common.widget.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.common.R

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.dialogStyle) {
    var onConfirm: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    fun setParams(title: String? = "", message: String? = "", positiveText: String? = "", negativeText: String? = ""): AndDialog {
        if (!title.isNullOrEmpty()) setTitle(title)
        setMessage(if (message.isNullOrEmpty()) "" else message)
        setPositiveButton(positiveText) { _: DialogInterface?, _: Int -> onConfirm?.invoke() }
        if (!negativeText.isNullOrEmpty()) setNegativeButton(negativeText) { _: DialogInterface?, _: Int -> onCancel?.invoke() }
        return this
    }

}