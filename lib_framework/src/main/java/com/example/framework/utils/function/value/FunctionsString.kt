package com.example.framework.utils.function.value

import android.util.Base64
import java.util.regex.Pattern

/**
 * 将 字节数组 转换成 Base64 编码
 * 用Base64.DEFAULT模式会导致加密的text下面多一行（在应用中显示是这样）
 */
fun ByteArray?.base64Encode() = Base64.encodeToString(this, Base64.NO_WRAP)

/**
 * 将 Base64 字符串 解码成 字节数组
 */
fun String?.base64Decode() = Base64.decode(this, Base64.NO_WRAP)

/**
 * 千分位格式
 * 10000
 * ->10,000
 */
fun String?.thousandsFormat(): String {
    this ?: return "0"
    if (numberCompareTo("1000") == -1) return this
    val list = split(".")
    val text = if (list.size > 1) list.safeGet(0) else this
    val tmp = StringBuffer().append(text).reverse()
    val retNum = Pattern.compile("(\\d{3})(?=\\d)").matcher(tmp.toString()).replaceAll("$1,")
    val value = StringBuffer().append(retNum).reverse().toString()
    return if (list.size > 1) "${value}.${list.safeGet(1)}" else value
}

/**
 * 提取链接中的参数
 */
fun String?.getValueByName(name: String): String {
    this ?: return ""
    var value = ""
    val index = indexOf("?")
    val temp = substring(index + 1)
    val keyValue = temp.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (str in keyValue) {
        if (str.contains(name)) {
            value = str.replace("$name=", "")
            break
        }
    }
    return value
}

/**
 * 隐藏手机号码的中间4位
 */
fun String?.hide4BitLetter(): String {
    this ?: return ""
    var value = ""
    if (isMobile()) {
        val ch = toCharArray()
        for (index in ch.indices) {
            if (index in 3..6) {
                value = "$value*"
            } else {
                value += ch[index]
            }
        }
    } else value = this
    return value
}

/**
 * 验证手机号
 */
fun String?.isMobile() = Pattern.matches("^1[0-9]{10}$", this.orEmpty())

/**
 * 返回密码强度
 */
fun String?.checkSecurity(): Int {
    if (this.isNullOrEmpty()) return 0
    //纯数字、纯字母、纯特殊字符
    if (this.length < 8 || Pattern.matches("^\\d+$", this) || Pattern.matches("^[a-z]+$", this) || Pattern.matches("^[A-Z]+$", this) || Pattern.matches("^[@#$%^&]+$", this)) return 1
    //字母+数字、字母+特殊字符、数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$", this) || Pattern.matches("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$", this) || Pattern.matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$", this) || Pattern.matches("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$", this) || Pattern.matches("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$", this) || Pattern.matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$", this)) return 2
    //字母+数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$", this)) return 3
    return 3
}