package com.example.common.widget.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.common.R
import com.example.common.utils.function.string

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.AndDialogStyle) {
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    fun setPositive(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure)): AndDialog {
        return setParams(title, message, positiveText, "")
    }

    fun setParams(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), negativeText: String? = string(R.string.cancel)): AndDialog {
        if (!title.isNullOrEmpty()) setTitle(title)
        setMessage(message)
        setPositiveButton(positiveText) { dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
            onConfirm?.invoke()
        }
        if (!negativeText.isNullOrEmpty()) {
            setNegativeButton(negativeText) { dialog: DialogInterface?, _: Int ->
                dialog?.dismiss()
                onCancel?.invoke()
            }
        }
        return this
    }

    fun setDialogListener(onConfirm: () -> Unit = {}, onCancel: () -> Unit = {}): AndDialog {
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        return this
    }

}