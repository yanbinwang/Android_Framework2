package com.example.klinechart.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

/**
 * 时间工具类
 */
@SuppressLint("SimpleDateFormat")
object DateUtil {
    val longTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val shortTimeFormat = SimpleDateFormat("HH:mm")
    val DateFormat = SimpleDateFormat("yyyy/MM/dd")
}