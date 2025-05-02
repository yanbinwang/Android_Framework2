package com.example.framework.utils.function.value

import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Base64
import com.example.framework.utils.function.value.ELFormat.MOBILE
import java.util.regex.Pattern
import kotlin.math.pow

/**
 * 将 字节数组 转换成 Base64 编码
 * 用Base64.DEFAULT模式会导致加密的text下面多一行（在应用中显示是这样）
 */
fun ByteArray?.base64Encode(): String {
    this ?: return ""
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

/**
 * 将 Base64 字符串 解码成 字节数组
 */
fun String?.base64Decode(): ByteArray {
    this ?: return "".toByteArray()
    return Base64.decode(this, Base64.NO_WRAP)
}

/**
 * Uri编码
 */
fun String?.uriEncode(): String? {
    this ?: return null
    return Uri.encode(this)
}

/**
 * Uri解码
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
 * 长度限制
 */
fun String.limitLength(maxLength: Int = 4000): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

/**
 * 检测正则
 */
fun String?.regCheck(reg: String): Boolean {
    this ?: return false
    val pattern = Pattern.compile(reg)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

/**
 * 富文本转化Spannable
 */
fun String?.toSpanned(): Spanned? {
    this ?: return null
//    return Html.fromHtml(this)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
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
    val text = if (list.safeSize > 1) list.safeGet(0) else this
    val tmp = StringBuffer().append(text).reverse()
    val retNum = Pattern.compile("(\\d{3})(?=\\d)").matcher(tmp.toString()).replaceAll("$1,")
    val value = StringBuffer().append(retNum).reverse().toString()
    return if (list.safeSize > 1) "${value}.${list.safeGet(1)}" else value
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
 * 添加网页链接中的Param
 * isNullOrEmpty()：该方法用于判断字符串是否为 null 或者长度为 0。也就是说，只要字符串为 null 或者是一个空字符串（""），此方法就会返回 true。
 * isNullOrBlank()：此方法不仅会检查字符串是否为 null 或者长度为 0，还会检查字符串是否只包含空白字符（如空格、制表符、换行符等）。若字符串为 null、空字符串或者只包含空白字符，isNullOrBlank() 都会返回 true。
 */
fun String?.addUrlParam(key: String?, value: String?): String? {
    // 若原字符串、键或值为空，直接返回原字符串
    if (this.isNullOrEmpty() || key.isNullOrBlank() || value.isNullOrBlank()) {
        return this
    }
    // 编码值以避免特殊字符问题
    val encodedValue = Uri.encode(value)
    // 判断原字符串是否已包含查询参数
    return if (this.contains("?")) {
        "$this&$key=${encodedValue}"
    } else {
        "$this?$key=${encodedValue}"
    }
}

/**
 * 隐藏手机号码的中间4位
 */
fun String?.hidePhoneNumber(): String {
    this ?: return ""
    var value = ""
    if (regCheck(MOBILE)) {
        val ch = toCharArray()
        for (index in ch.indices) {
            if (index in 3..6) {
                value = "$value*"
            } else {
                value += ch[index]
            }
        }
    } else {
        value = this
    }
    return value
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

// <editor-fold defaultstate="collapsed" desc="EL表达式">
object ELFormat {
    const val MOBILE = "^1[0-9]{10}$"
    const val EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$"
    const val PASSWORD = "^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{6,20}\$"
}
// </editor-fold>