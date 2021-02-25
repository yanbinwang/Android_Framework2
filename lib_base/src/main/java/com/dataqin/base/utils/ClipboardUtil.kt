package com.dataqin.base.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * 粘贴板工具类
 */
object ClipboardUtil {

    @JvmStatic
    fun putTextIntoClip(context: Context, label: String, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建ClipData对象
        val clipData = ClipData.newPlainText(label, text)
        //添加ClipData对象到剪切板中
        clipboardManager.primaryClip = clipData
    }

    @JvmStatic
    fun getTextFromClip(context: Context): String {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //判断剪切版时候有内容
        if (!clipboardManager.hasPrimaryClip()) return ""
        val clipData = clipboardManager.primaryClip
        //        //获取 ClipDescription
        //        ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();
        //        //获取 lable
        //        String lable = clipDescription.getLabel().toString();
        //        //获取 text
        //        String text = clipData.getItemAt(0).getText().toString();
        //获取 text
        return clipData!!.getItemAt(0).text.toString()
    }

}
