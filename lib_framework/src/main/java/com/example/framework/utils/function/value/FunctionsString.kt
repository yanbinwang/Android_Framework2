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
fun ByteArray?.base64EncodeToString(flags: Int = Base64.NO_WRAP): String {
    this ?: return ""
    return Base64.encodeToString(this, flags)
}

fun ByteArray?.base64Encode(flags: Int = Base64.NO_WRAP): ByteArray {
    this ?: return ByteArray(0)
    return Base64.encode(this, flags)
}

/**
 * 将 Base64 字符串 解码成 字节数组
 */
fun String?.base64Decode(flags: Int = Base64.NO_WRAP): ByteArray {
    this ?: return "".toByteArray()
    return Base64.decode(this, flags)
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
        // 处理开头的普通字符串
        value.append(strings[0])
    }
    try {
        for (i in 1 until strings.size) {
            hex = strings[i]
            if (hex.length > 3) {
                mix = ""
                if (hex.length > 4) {
                    // 处理 Unicode 编码符号后面的普通字符串
                    mix = hex.substring(4, hex.length)
                }
                hex = hex.take(4)
                try {
                    hex.toInt(16)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 不能将当前 16 进制字符串正常转换为 10 进制数字，拼接原内容后跳出
                    value.append(prefix).append(strings[i])
                    continue
                }
                ascii = 0
                for (j in hex.indices) {
                    hexChar = hex[j]
                    // 将 Unicode 编码中的 16 进制数字逐个转为 10 进制
                    n = hexChar.toString().toInt(16)
                    // 转换为 ASCII 码
                    ascii += n * 16.0.pow(hex.length - j - 1).toInt()
                }
                // 拼接解码内容
                value.append(ascii.toChar()).append(mix)
            } else {
                // 不转换特殊长度的 Unicode 编码
                value.append(prefix).append(hex)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Unicode 编码格式有误，解码失败
        return null
    }
    return value.toString()
}

/**
 * 千分位格式
 * 10000 -> 10,000
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
 * 长度限制
 */
fun String.limitLength(maxLength: Int = 3500): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

/**
 * 富文本转化Spannable
 */
fun String?.htmlToSpanned(): Spanned? {
    this ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

/**
 * 从 URL 链接中提取指定名称的参数值
 * @param paramName 要提取的参数名
 * @return 匹配的参数值（无匹配/URL为空返回空字符串）
 */
fun String?.getUrlParam(paramName: String): String {
    this ?: return ""
    // 先判断是否包含?，避免index=-1时substring(0)截取整个字符串
    val queryStartIndex = indexOf("?")
    if (queryStartIndex == -1) return ""
    val queryStr = substring(queryStartIndex + 1)
    val paramPairs = queryStr.split("&")
    for (pair in paramPairs) {
        // 用split("=", limit=2)精准拆分键值对，避免参数值含=的情况（比如param=123=456）
        val keyValue = pair.split("=", limit = 2)
        if (keyValue.size == 2 && keyValue[0] == paramName) {
            return keyValue[1] // 直接返回值，无需replace，更高效
        }
    }
    return ""
}

/**
 * 给 URL 链接添加单个参数（自动编码参数值，避免特殊字符问题）
 * @param key 要添加的参数名（空白/空则不添加）
 * @param value 要添加的参数值（空白/空则不添加）
 * @return 新增参数后的新URL（原URL/键/值不合法则返回原URL）
 */
fun String?.withUrlParam(key: String?, value: String?): String? {
    if (this.isNullOrEmpty() || key.isNullOrBlank() || value.isNullOrBlank()) {
        return this
    }
    val encodedValue = Uri.encode(value)
    return if (this.contains("?")) {
        "$this&$key=$encodedValue"
    } else {
        "$this?$key=$encodedValue"
    }
}

/**
 * 检测正则
 */
fun String?.matchesRegex(regex: String): Boolean {
    this ?: return false
    return this.matches(Regex(regex))
}

/**
 * 隐藏手机号码的中间4位
 */
fun String?.hidePhoneNumber(): String {
    this ?: return ""
    // 先校验是否为合法手机号
    if (!this.matchesRegex(MOBILE)) return this
    return buildString {
        append(this@hidePhoneNumber.substring(0, 3)) // 前3位
        append("****") // 中间4位替换为*
        append(this@hidePhoneNumber.substring(7)) // 后4位
    }
}

// <editor-fold defaultstate="collapsed" desc="EL表达式">
object ELFormat {
    const val MOBILE = "^1[0-9]{10}$"
    const val EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$"
    const val PASSWORD = "^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{6,20}\$"
}
// </editor-fold>