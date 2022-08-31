package com.example.common.widget.dialog

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import com.example.common.R

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.dialogStyle) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun setParams(title: String? = "", message: String? = "", positiveText: String? = "", negativeText: String? = ""): AndDialog {
        if (!TextUtils.isEmpty(title)) setTitle(title)
        setMessage(if (TextUtils.isEmpty(message)) "" else message)
        setPositiveButton(positiveText) { _: DialogInterface?, _: Int -> onConfirm }
        if (!TextUtils.isEmpty(negativeText)) setNegativeButton(negativeText) { _: DialogInterface?, _: Int -> onCancel }
        return this
    }

    fun setOnDialogListener(onConfirm: (() -> Unit), onCancel: (() -> Unit)? = {}): AndDialog {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        return this
    }

    companion object {
        @JvmStatic
        fun with(context: Context?): AndDialog {
            return AndDialog(context!!)
        }
    }

}