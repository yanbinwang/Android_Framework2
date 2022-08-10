import android.text.TextUtils
import android.util.Base64
import com.example.base.utils.LogUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * author:wyb
 * 字符串修改类
 */
object StringUtil {

    private val cipher by lazy { Cipher.getInstance("AES/ECB/PKCS5Padding") }//创建密码器

    /**
     * 加密
     * @param data      待加密内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    @JvmOverloads
    @JvmStatic
    fun encrypt(data: String, secretKey: String = ""): String {
        try {
            //创建AES秘钥
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            //初始化加密器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            //将加密以后的数据进行 Base64 编码
            return base64Encode(cipher.doFinal(data.toByteArray()))
        } catch (e: Exception) {
            handleException("encrypt", e)
        }
        return ""
    }

    /**
     * 解密
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    @JvmOverloads
    @JvmStatic
    fun decrypt(base64Data: String, secretKey: String = ""): String {
        try {
            val data = base64Decode(base64Data)
            //创建AES秘钥
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            //初始化解密器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            //执行解密操作
            return String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (e: Exception) {
            handleException("decrypt", e)
        }
        return ""
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     * 用Base64.DEFAULT模式会导致加密的text下面多一行（在应用中显示是这样）
     */
    private fun base64Encode(data: ByteArray) = Base64.encodeToString(data, Base64.NO_WRAP)

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    private fun base64Decode(data: String) = Base64.decode(data, Base64.NO_WRAP)

    /**
     * 处理异常
     */
    private fun handleException(methodName: String, e: Exception) = LogUtil.e("CipherUtil", "${methodName}---->${e}")

    /**
     * 如果值为空，展示默认值
     */
    @JvmOverloads
    @JvmStatic
    fun processedString(source: String?, defaultStr: String = ""): String {
        return if (source == null) {
            defaultStr
        } else {
            if (source.trim { it <= ' ' }.isEmpty()) defaultStr else source
        }
    }

    /**
     * 提取链接中的参数
     */
    @JvmStatic
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

    /**
     * 隐藏手机号码的中间4位
     */
    @JvmStatic
    fun hide4BitLetter(input: String): String {
        var result = ""
        if (isMobile(input)) {
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

    /**
     * 验证手机号
     */
    @JvmStatic
    fun isMobile(strMobilePhone: String) = Pattern.matches("^1[0-9]{10}$", strMobilePhone)

    /**
     * 截取小数点后X位
     */
    @JvmStatic
    fun getFormat(doubleValue: String, decimalPlace: Int): String {
        if (TextUtils.isEmpty(doubleValue)) return ""
        val value = doubleValue.toDouble()
        val format = StringBuilder()
        for (i in 0 until decimalPlace) {
            format.append("0")
        }
        val decimalFormat = DecimalFormat("0.$format")
        decimalFormat.roundingMode = RoundingMode.DOWN
        return decimalFormat.format(value)
    }

    /**
     * 获取对应大小的文字
     */
    @JvmStatic
    fun getFormatSize(size: Double): String {
        val byteResult = size / 1024
        if (byteResult < 1) return "<1K"
        val kiloByteResult = byteResult / 1024
        if (kiloByteResult < 1) return BigDecimal(byteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K"
        val mByteResult = kiloByteResult / 1024
        if (mByteResult < 1) return BigDecimal(kiloByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M"
        val gigaByteResult = mByteResult / 1024
        if (gigaByteResult < 1) return BigDecimal(mByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        val teraByteResult = BigDecimal(gigaByteResult)
        return (teraByteResult.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB")
    }

    /**
     * 返回密码强度
     */
    @JvmStatic
    fun checkSecurity(pwd: String): Int {
        if (TextUtils.isEmpty(pwd)) return 0
        //纯数字、纯字母、纯特殊字符
        if (pwd.length < 8 || Pattern.matches("^\\d+$", pwd) || Pattern.matches("^[a-z]+$", pwd) || Pattern.matches("^[A-Z]+$", pwd) || Pattern.matches("^[@#$%^&]+$", pwd)) return 1
        //字母+数字、字母+特殊字符、数字+特殊字符
        if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)[a-z\\d]+$", pwd) || Pattern.matches("^(?!\\d+$)(?![A-Z]+$)[A-Z\\d]+$", pwd) || Pattern.matches("^(?![a-z]+$)(?![@#$%^&]+$)[a-z@#$%^&]+$", pwd) || Pattern.matches("^(?![A-Z]+$)(?![@#$%^&]+$)[A-Z@#$%^&]+$", pwd) || Pattern.matches("^(?![a-z]+$)(?![A-Z]+$)[a-zA-Z]+$", pwd) || Pattern.matches("^(?!\\d+)(?![@#$%^&]+$)[\\d@#$%^&]+$", pwd)) return 2
        //字母+数字+特殊字符
        if (Pattern.matches("^(?!\\d+$)(?![a-z]+$)(?![A-Z]+$)(?![@#$%^&]+$)[\\da-zA-Z@#$%^&]+$", pwd)) return 3
        return 3
    }

}