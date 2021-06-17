package com.example.base.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * 粘贴板工具类
 */
fun Context.setPrimaryClip(label: String, text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.getPrimaryClip(): String {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    //判断剪切版时候有内容
    if (!clipboardManager.hasPrimaryClip()) return ""
    //获取 text
    return clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
}
