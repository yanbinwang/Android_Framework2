package com.example.common.utils.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hjq.permissions.Permission

/**
 * 方便直接通过权限组提取权限
 */
object XXPermissionsGroup {
    val CALENDAR = arrayOf(Permission.READ_CALENDAR, Permission.WRITE_CALENDAR)
    val CAMERA = arrayOf(Permission.CAMERA)
    val CONTACTS = arrayOf(Permission.READ_CONTACTS, Permission.WRITE_CONTACTS, Permission.GET_ACCOUNTS)
    val LOCATION = arrayOf(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
    val MICROPHONE = arrayOf(Permission.RECORD_AUDIO)
    val PHONE = arrayOf(Permission.READ_PHONE_STATE, Permission.CALL_PHONE, Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.ADD_VOICEMAIL, Permission.USE_SIP, Permission.PROCESS_OUTGOING_CALLS)
    val SENSORS = arrayOf(Permission.BODY_SENSORS)
    val SMS = arrayOf(Permission.SEND_SMS, Permission.RECEIVE_SMS, Permission.READ_SMS, Permission.RECEIVE_WAP_PUSH, Permission.RECEIVE_MMS)
    val STORAGE = getStorageGroup(true)

    /**
     * 获取存储权限组（适配第三方库限制）
     * PermissionChecker.java:158权限库针对targetSdkVersion >= 33直接抛出了异常，会导致我们使用时闪退，内部已经对旧的读写权限写了兼容代码
     * 我们需要在使用时只传入Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_AUDIO，而本地检测时（调用系统）使用版本判断
     * @param isRequest 是否为权限请求（true表示请求权限，false表示检查权限）
     */
    @JvmStatic
    fun getStorageGroup(isRequest: Boolean = false): Array<String> {
        return if (isRequest) {
            arrayOf(Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_AUDIO)
        } else {
            val deviceSdkInt = Build.VERSION.SDK_INT
            return when {
                // Android 13+ 设备，使用媒体权限
                deviceSdkInt >= Build.VERSION_CODES.TIRAMISU -> {
                    arrayOf(Permission.READ_MEDIA_IMAGES, Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_AUDIO)
                }
                // Android 12- 设备，使用旧存储权限
                deviceSdkInt >= Build.VERSION_CODES.P -> {
                    // Android 10-12：使用 READ_EXTERNAL_STORAGE
                    arrayOf(Permission.READ_EXTERNAL_STORAGE)
                }
                // Android 9 及以下，需要读写权限
                else -> {
                    arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }
}

/**
 * 日历权限组
 */
fun Context.checkSelfCalendar() = checkSelfPermission(this, *XXPermissionsGroup.CALENDAR)

/**
 * 相机权限组
 */
fun Context.checkSelfCamera() = checkSelfPermission(this, *XXPermissionsGroup.CAMERA)

/**
 * 联系人权限组
 */
fun Context.checkSelfContacts() = checkSelfPermission(this, *XXPermissionsGroup.CONTACTS)

/**
 * 定位权限组
 */
fun Context.checkSelfLocation() = checkSelfPermission(this, *XXPermissionsGroup.LOCATION)

/**
 * 麦克风权限组
 */
fun Context.checkSelfMicrophone() = checkSelfPermission(this, *XXPermissionsGroup.MICROPHONE)

/**
 * 手机权限组
 */
fun Context.checkSelfPhone() = checkSelfPermission(this, *XXPermissionsGroup.PHONE)

/**
 * 传感器权限组
 */
fun Context.checkSelfSensors() = checkSelfPermission(this, *XXPermissionsGroup.SENSORS)

/**
 * 短信权限组
 */
fun Context.checkSelfSMS() = checkSelfPermission(this, *XXPermissionsGroup.SMS)

/**
 * 存储权限组
 */
fun Context.checkSelfStorage() = checkSelfPermission(this, *XXPermissionsGroup.getStorageGroup(false))

/**
 * 权限检测
 */
private fun checkSelfPermission(context: Context, vararg permissions: String): Boolean {
//    permissions.forEach {
//        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, it)) return false
//    }
//    return true
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}