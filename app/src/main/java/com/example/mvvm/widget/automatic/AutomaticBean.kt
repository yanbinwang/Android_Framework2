package com.example.mvvm.widget.automatic

import android.text.Spannable

/**
 * @description 自动文本类
 * @author yan
 * type
 * 0：类型1-》输入框
 * 1：类型2-》上传图片
 */
data class AutomaticBean(
    val type: Int? = null,//控件類型
    val label: Spannable? = null,//顯示的文案
    val value: String? = null,//控件的值（輸出框為text，圖片為url）
    val maxLength: Int? = null,//輸入框支持的最大長度
    var required: Boolean? = null,//是否必填
    var enable: Boolean? = null,//是否可用（可點擊可操作）
    var extras: String? = null//補充json字符
)