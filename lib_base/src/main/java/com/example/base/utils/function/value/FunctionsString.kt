package com.example.base.utils.function.value

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Base64
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 创建密码器
 */
private val cipher by lazy { Cipher.getInstance("AES/ECB/PKCS5Padding") }

/**
 * 加密
 * @param this      待加密内容
 * @param secretKey 加密密码，长度：16 或 32 个字符
 * @return 返回Base64转码后的加密数据
 */
@JvmOverloads
fun String.encrypt(secretKey: String = ""): String {
    try {
        //创建AES秘钥
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        //初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        //将加密以后的数据进行 Base64 编码
        return cipher.doFinal(toByteArray()).base64Encode()
    } catch (_: Exception) {
    }
    return ""
}

/**
 * 解密
 * @param this 加密的密文 Base64 字符串
 * @param secretKey  解密的密钥，长度：16 或 32 个字符
 * @return 返回Base64转码后的加密数据
 */
@JvmOverloads
fun String.decrypt(secretKey: String = ""): String {
    try {
        val data = base64Decode()
        //创建AES秘钥
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        //初始化解密器
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        //执行解密操作
        return String(cipher.doFinal(data), Charsets.UTF_8)
    } catch (_: Exception) {
    }
    return ""
}

/**
 * 将 字节数组 转换成 Base64 编码
 * 用Base64.DEFAULT模式会导致加密的text下面多一行（在应用中显示是这样）
 */
fun ByteArray.base64Encode() = Base64.encodeToString(this, Base64.NO_WRAP)

/**
 * 将 Base64 字符串 解码成 字节数组
 */
fun String.base64Decode() = Base64.decode(this, Base64.NO_WRAP)

/**
 * 提取链接中的参数
 */
fun String.getValueByName(name: String): String {
    var result = ""
    val index = indexOf("?")
    val temp = substring(index + 1)
    val keyValue = temp.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (str in keyValue) {
        if (str.contains(name)) {
            result = str.replace("$name=", "")
            break
        }
    }
    return result
}

/**
 * 隐藏手机号码的中间4位
 */
fun String.hide4BitLetter(): String {
    var result = ""
    if (isMobile()) {
        val ch = toCharArray()
        for (index in ch.indices) {
            if (index in 3..6) {
                result = "$result*"
            } else {
                result += ch[index]
            }
        }
    } else {
        result = this
    }
    return result
}

/**
 * 验证手机号
 */
fun String.isMobile() = Pattern.matches("^1[0-9]{10}$", this)

/**
 * 截取小数点后X位
 */
fun String.getFormat(decimalPlace: Int): String {
    if (TextUtils.isEmpty(this)) return ""
    val value = toDouble()
    val format = StringBuilder()
    for (i in 0 until decimalPlace) {
        format.append("0")
    }
    val decimalFormat = DecimalFormat("0.$format")
    decimalFormat.roundingMode = RoundingMode.DOWN
    return decimalFormat.format(value)
}

/**
 * 返回密码强度
 */
fun String.checkSecurity(): Int {
    if (TextUtils.isEmpty(this)) return 0
    //纯数字、纯字母、纯特殊字符
    if (this.length < 8 || Pattern.matches("^\\d+$", this) || Pattern.matches("^[a-z]+$", this) || Pattern.matches("^[A-Z]+$", this) || Pattern.matches("^[@#$%^&]+$", this)) return 1
    //字母+数字、字母+特殊字符、数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$", this) || Pattern.matches("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$", this) || Pattern.matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$", this) || Pattern.matches("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$", this) || Pattern.matches("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$", this) || Pattern.matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$", this)) return 2
    //字母+数字+特殊字符
    if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$", this)) return 3
    return 3
}

/**
 * 批量添加可点击
 * Spanned.SPAN_INCLUSIVE_EXCLUSIVE(前面包括，后面不包括)、
 * Spanned.SPAN_EXCLUSIVE_INCLUSIVE(前面不包括，后面包括)、
 * Spanned.SPAN_INCLUSIVE_INCLUSIVE(前后都包括)
 */
fun String?.setClickableSpan(vararg theme: Triple<ClickableSpan, Int, Int>): SpannableString {
    this ?: orEmpty()
    return SpannableString(this).apply {
        for (triple in theme) {
            setSpan(triple.first, triple.second, triple.third, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

fun String?.setForegroundColorSpan(vararg theme: Triple<ForegroundColorSpan, Int, Int>): SpannableString {
    this ?: orEmpty()
    return SpannableString(this).apply {
        for (triple in theme) {
            setSpan(triple.first, triple.second, triple.third, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

fun String?.setBackgroundColorSpan(vararg theme: Triple<BackgroundColorSpan, Int, Int>): SpannableString {
    this ?: orEmpty()
    return SpannableString(this).apply {
        for (triple in theme) {
            setSpan(triple.first, triple.second, triple.third, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}