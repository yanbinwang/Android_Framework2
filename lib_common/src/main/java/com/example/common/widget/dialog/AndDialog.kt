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
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.AndDialogStyle) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun setParams(title: String? = null, message: String? = null, positiveText: String? = null, negativeText: String? = null): AndDialog {
        if (!title.isNullOrEmpty()) setTitle(title)
        setMessage(message)
        setPositiveButton(positiveText) { _: DialogInterface?, _: Int -> onConfirm?.invoke() }
        if (!negativeText.isNullOrEmpty()) setNegativeButton(negativeText) { _: DialogInterface?, _: Int -> onCancel?.invoke() }
        return this
    }

    fun setDialogListener(onConfirm: () -> Unit = {}, onCancel: () -> Unit = {}): AndDialog {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        return this
    }

}