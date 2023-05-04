package com.example.framework.utils.function.value

import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.util.Base64
import com.example.framework.utils.function.value.ELFormat.FULL_WIDTH_STRING
import com.example.framework.utils.function.value.ELFormat.MOBILE
import java.util.regex.Pattern
import kotlin.math.pow

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
 * Url编码
 */
fun String?.uriEncode(): String? {
    this ?: return null
    return Uri.encode(this)
}

/**
 * Url解码
 */
fun String?.uriDecode(): String? {
    this ?: return null
    return Uri.decode(this)
}

/**
 * Unicode 编码转字符串
 * 支持 Unicode 编码和普通字符混合的字符串
 * @return 解码后的字符串
 */
fun String?.unicodeDecode(): String? {
    this ?: return null
    val prefix = "\\u"
    if (indexOf(prefix).orZero < 0) return this
    val value = StringBuilder(length shr 2)
    val strings = split("\\\\u").toTypedArray()
    var hex: String
    var mix: String
    var hexChar: Char
    var ascii: Int
    var n: Int
    if (strings[0].isNotEmpty()) {
        //处理开头的普通字符串
        value.append(strings[0])
    }
    try {
        for (i in 1 until strings.size) {
            hex = strings[i]
            if (hex.length > 3) {
                mix = ""
                if (hex.length > 4) {
                    //处理 Unicode 编码符号后面的普通字符串
                    mix = hex.substring(4, hex.length)
                }
                hex = hex.substring(0, 4)
                try {
                    hex.toInt(16)
                } catch (e: Exception) {
                    //不能将当前 16 进制字符串正常转换为 10 进制数字，拼接原内容后跳出
                    value.append(prefix).append(strings[i])
                    continue
                }
                ascii = 0
                for (j in hex.indices) {
                    hexChar = hex[j]
                    //将 Unicode 编码中的 16 进制数字逐个转为 10 进制
                    n = hexChar.toString().toInt(16)
                    //转换为 ASCII 码
                    ascii += n * 16.0.pow(hex.length - j - 1).toInt()
                }
                //拼接解码内容
                value.append(ascii.toChar()).append(mix)
            } else {
                //不转换特殊长度的 Unicode 编码
                value.append(prefix).append(hex)
            }
        }
    } catch (e: Exception) {
        //Unicode 编码格式有误，解码失败
        return null
    }
    return value.toString()
}

/**
 * 检测正则
 */
fun String?.regCheck(reg: String): Boolean {
    this ?: return false
    val p = Pattern.compile(reg)
    val m = p.matcher(this)
    return m.matches()
}

/**
 * 富文本转化Spannable
 */
fun String?.toSpanned(): Spanned? {
    this ?: return null
    return Html.fromHtml(this)
}

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
 * 将一组字符串根据全角半角转换成一组对应的空格字符
 */
fun String?.getSpaceValue(extras: Int = 0): String {
    this ?: return ""
    val charArray = toCharArray()
    var value = ""
    charArray.forEach {
        val text = it.toString()
        value = if (text.regCheck(FULL_WIDTH_STRING)) {
            value + "\u3000"
        } else {
            value + "\u0020"
        }
    }
    //额外增多几个半角的中文空格，padding决定
    for (index in 0 until extras) {
        value += "\u3000"
    }
    return value
}

/**
 * 添加网页链接中的Param
 */
fun String?.addUrlParam(key: String?, value: String?): String? {
    key ?: return this
    value ?: return this
    this ?: return this
    return if (this.contains("?")) {
        "$this&$key=${Uri.encode(value)}"
    } else {
        "$this?$key=${Uri.encode(value)}"
    }
}

/**
 * 接取固定长度的字符串
 */
fun String?.fixLength(size: Int): String {
    if (this == null) return ""
    return if (length.orZero > size) {
        substring(0, size)
    } else {
        this
    }
}

/**
 * 隐藏手机号码的中间4位
 */
fun String?.hidePhoneNumber(): String {
    this ?: return ""
    var value = ""
    if (regexMatch(MOBILE)) {
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
 * 检测
 */
fun String?.regexMatch(regex:String) = Pattern.matches(regex, this.orEmpty())

// <editor-fold defaultstate="collapsed" desc="EL表达式">
object ELFormat {
    const val MOBILE = "^1[0-9]{10}$"
    const val EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$"
    const val PASSWORD = "^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{6,20}\$"
    const val FULL_WIDTH_STRING = "[^\\x00-\\xff]"
}
// </editor-fold>