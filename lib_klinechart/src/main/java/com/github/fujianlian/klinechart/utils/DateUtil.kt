package com.github.fujianlian.klinechart.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

/**
 * 时间工具类
 * Created by tifezh on 2016/4/27.
 */
@SuppressLint("SimpleDateFormat")
object DateUtil {
    var longTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var shortTimeFormat = SimpleDateFormat("HH:mm")
    var DateFormat = SimpleDateFormat("yyyy/MM/dd")
}