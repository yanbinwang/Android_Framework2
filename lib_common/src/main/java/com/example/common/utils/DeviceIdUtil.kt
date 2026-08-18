package com.example.common.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.common.BaseApplication
import com.example.common.config.CacheData
import com.example.framework.utils.function.value.orZero
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/**
 * 获取唯一设备标识的工具类 (拿取安装实例 ID)
 * 1) 优先拼 → IMEI + AndroidID + Serial + 硬件特征 → SHA1
 * 2) 拿不到 → 用 本地随机 UUID（MMKV 存）
 * 3) 卸载 / 清数据 → MMKV 没了 → 新 ID
 */
@SuppressLint("MissingPermission", "HardwareIds")
object DeviceIdUtil {
    /**
     * 系统随机数
     */
    private val randomUUID = UUID.randomUUID().toString().replace("-", "")
    /**
     * 获得设备硬件标识
     * @return 设备硬件标识
     */
    val deviceId: String
        get() {
            if (CacheData.deviceId.get().isNullOrEmpty()) {
                CacheData.deviceId.set(getId().let {
                    return@let if (it.length > 30) it.substring(0, 30) else it
                })
            }
            return CacheData.deviceId.get().orEmpty()
        }

    /**
     * 获取 "设备标识"
     * 整合多种设备相关信息（IMEI、Android ID、设备序列号、硬件特征等），经过 SHA1 哈希处理生成一个统一的设备标识
     */
    private fun getId(): String {
        val context = BaseApplication.instance.applicationContext
        val sbDeviceId = StringBuilder()
        // 获取设备的 IMEI（国际移动设备识别码）或 MEID（移动设备识别码）（需要 ReadPhoneState 权限）
        val imei = getIMEI(context)
        // 获取设备唯一标识符（无需权限）
        val androidId = getAndroidId(context)
        // 获取设备序列号（需要 ReadPhoneState 权限）
        val serial = getSerial(context)
        // 获得硬件 uuid（根据硬件相关属性，生成 uuid）（无需权限）
        val uuid = getDeviceUUID(serial).replace("-", "")
        // 追加 imei
        if (!imei.isNullOrEmpty()) {
            sbDeviceId.append(imei)
            sbDeviceId.append("|")
        }
        // 追加 androidId
        if (!androidId.isNullOrEmpty()) {
            sbDeviceId.append(androidId)
            sbDeviceId.append("|")
        }
        // 追加 serial
        if (!serial.isNullOrEmpty()) {
            sbDeviceId.append(serial)
            sbDeviceId.append("|")
        }
        // 追加硬件 uuid
        if (uuid.isNotEmpty()) {
            sbDeviceId.append(uuid)
        }
        // 生成 SHA1，统一 DeviceId 长度
        if (sbDeviceId.isNotEmpty()) {
            try {
                val hash = getHashByString(sbDeviceId.toString())
                val sha1 = bytesToHex(hash)
                if (sha1.isNotEmpty()) {
                    // 返回最终的 DeviceId
                    return sha1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 如果以上硬件标识数据均无法获得，则 DeviceId 默认使用系统随机数，这样保证 DeviceId 不为空
        return getUUID()
    }

    /**
     * 获取设备的 IMEI（国际移动设备识别码）或 MEID（移动设备识别码）
     * 需要获得 READ_PHONE_STATE 权限，默认返回null
     *
     * 通过 TelephonyManager（电话管理服务）获取设备的 deviceId，这是移动设备的硬件标识符：
     * 对于 GSM 网络设备，通常返回IMEI（15 位数字）
     * 对于 CDMA 网络设备，通常返回MEID（14 位十六进制数）
     *
     * IMEI/MEID 属于敏感个人信息，Google Play 和国内应用市场对其使用有严格限制
     * 非必要场景（如设备唯一标识）不建议使用，可能导致应用审核不通过
     *
     * Android 6.0（API 23）及以上需要动态申请 READ_PHONE_STATE 权限
     * Android 10（API 29）及以上，普通应用即使有此权限也无法获取 deviceId（系统限制）
     * Android 11（API 30）及以上，deviceId字段已被标记为废弃
     */
    @Deprecated("主要用于兼容老旧设备，实际开发中需谨慎使用")
    private fun getIMEI(context: Context): String? {
        return try {
            // Android 10+ (API 29+)：系统级限制，普通应用无法获取，直接放弃
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return ""
            // Android 6.0 ~ 9.0：需要运行时权限检查
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return ""
            // 满足版本/权限获取设备号
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.deviceId
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取设备唯一标识符
     * ANDROID_ID 是一个 64 位的字符串，理论上在设备首次启动时生成，用于标识设备
     */
    private fun getAndroidId(context: Context): String? {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取设备序列号
     * Android 10 (API 29) 及以上版本有严格的权限限制：需要获得 READ_PHONE_STATE 权限，且即使拥有该权限，在非系统应用中也可能无法获取完整的序列号
     * Build.SERIAL 目前已经被官方明确标记为废弃，现在它的值始终是 Build.UNKNOWN
     */
    @Deprecated("主要用于兼容老旧设备，实际开发中需谨慎使用")
    private fun getSerial(context: Context): String? {
        return try {
            // Android 8.0 以下：直接用 Build.SERIAL（已废弃但有效）
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return Build.SERIAL.takeIf { it.isNotEmpty() && it != Build.UNKNOWN } ?: ""
            }
            // Android 8.0 ~ 11：需要 READ_PHONE_STATE 权限
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                return if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // 无权限，返回空（而非unknown）
                    ""
                } else {
                    Build.getSerial().takeIf { it != Build.UNKNOWN } ?: ""
                }
            }
            // Android 12+ (API 31+)：普通应用无法获取，直接放弃
            ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获得设备硬件 uuid
     * 使用硬件信息，计算出一个随机数
     */
    private fun getDeviceUUID(serial: String?): String {
        return try {
            val dev = "3883756" +
                    Build.BOARD.length % 10 +
                    Build.BRAND.length % 10 +
                    Build.DEVICE.length % 10 +
                    Build.HARDWARE.length % 10 +
                    Build.ID.length % 10 +
                    Build.MODEL.length % 10 +
                    Build.PRODUCT.length % 10 +
                    serial?.length.orZero % 10
            UUID(dev.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获得 UUID
     */
    fun getUUID(): String {
        return randomUUID
    }

    /**
     * SHA1 加密
     * 对输入的字符串 data 进行 SHA1 加密，返回加密后的字节数组（ByteArray）。
     */
    private fun getHashByString(data: String): ByteArray {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA1")
            messageDigest.reset()
            messageDigest.update(data.toByteArray(charset("UTF-8")))
            messageDigest.digest()
        } catch (e: Exception) {
            e.printStackTrace()
            "".toByteArray()
        }
    }

    /**
     * 将字节数组（ByteArray）转换为十六进制字符串
     */
    private fun bytesToHex(data: ByteArray): String {
        val sb = StringBuilder()
        var str: String
        for (n in data.indices) {
            str = Integer.toHexString(data[n].toInt() and 0xFF)
            if (str.length == 1) sb.append("0")
            sb.append(str)
        }
        return sb.toString().uppercase(Locale.CHINA)
    }

}