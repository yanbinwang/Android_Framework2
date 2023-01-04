package com.example.mvvm.widget.automatic

/**
 * @description 自动文本类
 * @author yan
 * type
 * 0：类型1-》输入框
 * 1：类型2-》上传图片
 */
data class AutomaticBean(val type: Int, val key: String, val label: String)