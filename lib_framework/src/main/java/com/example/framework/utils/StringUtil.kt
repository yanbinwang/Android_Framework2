package com.example.framework.utils

import android.text.TextUtils
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * author:wyb
 * 字符串修改类
 */
object StringUtil {

    fun processedString(source: String?, defaultStr: String?): String {
        return if (source == null) {
            defaultStr!!
        } else {
            if (source.trim { it <= ' ' }.isEmpty()) {
                defaultStr!!
            } else {
                source
            }
        }
    }

    //提取链接中的参数
    fun getValueByName(url: String, name: String): String {
        var result = ""
        val index = url.indexOf("?")
        val temp = url.substring(index + 1)
        val keyValue = temp.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in keyValue) {
            if (str.contains(name)) {
                result = str.replace("$name=", "")
                break
            }
        }
        return result
    }

    //隐藏手机号码的中间4位
    fun Hide4BitLetter(input: String): String {
        var result = ""
        if (isMobilePhone(input)) {
            val ch = input.toCharArray()
            for (index in ch.indices) {
                if (index in 3..6) {
                    result = "$result*"
                } else {
                    result += ch[index]
                }
            }
        } else {
            result = input
        }
        return result
    }

    //验证手机号
    fun isMobilePhone(strMobilePhone: String): Boolean {
        val result: Boolean
        val patternString = "^1[0-9]{10}$"
        result = Pattern.matches(patternString, strMobilePhone)
        return result
    }

    //获取系统时间
    fun getTimeStamp(): String {
        return (System.currentTimeMillis() / 1000).toString()
    }

    //截取小数点后X位
    fun getFormat(doubleValue: String, decimalPlace: Int): String {
        if (TextUtils.isEmpty(doubleValue)) {
            return ""
        }
        val value = java.lang.Double.parseDouble(doubleValue)
        val format = StringBuilder()
        for (i in 0 until decimalPlace) {
            format.append("0")
        }
        val decimalFormat = DecimalFormat("0.$format")
        decimalFormat.roundingMode = RoundingMode.DOWN
        return decimalFormat.format(value)
    }

    //返回密码强度
    fun checkSecurity(pwd: String): Int {
        if (TextUtils.isEmpty(pwd)) {
            return 0
        }
        //纯数字、纯字母、纯特殊字符
        if (pwd.length < 8 || Pattern.matches("^\\d+$", pwd) || Pattern.matches(
                "^[a-z]+$",
                pwd
            ) || Pattern.matches("^[A-Z]+$", pwd) || Pattern.matches("^[@#$%^&]+$", pwd)
        ) {
            return 1
        }
        //字母+数字、字母+特殊字符、数字+特殊字符
        if (Pattern.matches(
                "^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$",
                pwd
            ) || Pattern.matches(
                "^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$",
                pwd
            ) || Pattern.matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$", pwd) || Pattern.matches(
                "^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$",
                pwd
            ) || Pattern.matches(
                "^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$",
                pwd
            ) || Pattern.matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$", pwd)
        ) {
            return 2
        }
        //字母+数字+特殊字符
        if (Pattern.matches(
                "^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$",
                pwd
            )
        ) {
            return 3
        }
        return 3
    }

}
