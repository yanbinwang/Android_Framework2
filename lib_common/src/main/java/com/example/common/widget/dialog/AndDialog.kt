package com.example.common.widget.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.common.R
import com.example.common.utils.i18n.string

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.AndDialogStyle) {
    private var onPositive: (() -> Unit)? = null
    private var onNegative: (() -> Unit)? = null

    fun setPositive(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), hasTitle: Boolean = true): AndDialog {
        return setParams(title, message, positiveText, "", hasTitle)
    }

    fun setParams(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), negativeText: String? = string(R.string.cancel), hasTitle: Boolean = true): AndDialog {
        if (hasTitle) setTitle(title)
        setMessage(message)
        setPositiveButton(positiveText) { _: DialogInterface?, _: Int ->
            onPositive?.invoke()
        }
        if (!negativeText.isNullOrEmpty()) setNegativeButton(negativeText) { _: DialogInterface?, _: Int ->
            onNegative?.invoke()
        }
        return this
    }

    fun setDialogListener(onPositive: () -> Unit = {}, onNegative: () -> Unit = {}): AndDialog {
        this.onPositive = onPositive
        this.onNegative = onNegative
        return this
    }

}