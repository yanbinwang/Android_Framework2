package com.example.framework.utils.function.value

import android.util.Base64
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