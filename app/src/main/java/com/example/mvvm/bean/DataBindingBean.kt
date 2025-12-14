package com.example.mvvm.bean

import android.text.Spannable

/**
 * 测试BindingAdapter类的数据体
 */
data class DataBindingBean(
    var text: String? = null,
    var spannable: Spannable? = null,
    var textColor: Int? = null,
    var background: Int? = null,
    var visibility: Int? = null
)