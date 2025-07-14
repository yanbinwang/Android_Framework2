package com.example.common.utils.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.PermissionNames

/**
 * 方便直接通过权限组提取权限
 * 特殊权限 (部分权限不属于任何组，需单独处理)
 * ACCESS_BACKGROUND_LOCATION：后台定位（需先获得 ACCESS_FINE_LOCATION）。
 * MANAGE_EXTERNAL_STORAGE：管理所有文件（Android 11+，需特殊申请）。
 * SYSTEM_ALERT_WINDOW：悬浮窗权限。
 */
object XXPermissionsGroup {
    // 静态文字权限组(用于系统检测)
    val CALENDAR = arrayOf(PermissionNames.READ_CALENDAR, PermissionNames.WRITE_CALENDAR)
    val CAMERA = arrayOf(PermissionNames.CAMERA)
    val CONTACTS = arrayOf(PermissionNames.READ_CONTACTS, PermissionNames.WRITE_CONTACTS, PermissionNames.GET_ACCOUNTS)
    val LOCATION = arrayOf(PermissionNames.ACCESS_FINE_LOCATION, PermissionNames.ACCESS_COARSE_LOCATION)
    val MICROPHONE = arrayOf(PermissionNames.RECORD_AUDIO)
    val PHONE = arrayOf(PermissionNames.READ_PHONE_STATE, PermissionNames.CALL_PHONE, PermissionNames.READ_CALL_LOG, PermissionNames.WRITE_CALL_LOG, PermissionNames.ADD_VOICEMAIL, PermissionNames.USE_SIP, PermissionNames.PROCESS_OUTGOING_CALLS)
    val SENSORS = arrayOf(PermissionNames.BODY_SENSORS)
    val SMS = arrayOf(PermissionNames.SEND_SMS, PermissionNames.RECEIVE_SMS, PermissionNames.READ_SMS, PermissionNames.RECEIVE_WAP_PUSH, PermissionNames.RECEIVE_MMS)
    val STORAGE = getStorageGroup()
    // 允许应用访问媒体文件中的地理位置信息（如照片的 EXIF 位置）-->Android 10 (API 29) 及以上。
    val MEDIA_LOCATION = arrayOf(PermissionNames.ACCESS_MEDIA_LOCATION)
    // 允许应用识别用户的身体活动（如步行、跑步、骑行）-->Android 10 (API 29) 及以上。
    val ACTIVITY_RECOGNITION = arrayOf(PermissionNames.ACTIVITY_RECOGNITION)

    // 动态权限组(用于库批量授权)
    val CALENDAR_GROUP = listOf(PermissionLists.getReadCalendarPermission(), PermissionLists.getWriteCalendarPermission())
    val CAMERA_GROUP = listOf(PermissionLists.getCameraPermission())
    val CONTACTS_GROUP = listOf(PermissionLists.getReadContactsPermission(), PermissionLists.getWriteContactsPermission(), PermissionLists.getGetAccountsPermission())
    val LOCATION_GROUP = listOf(PermissionLists.getAccessFineLocationPermission(), PermissionLists.getAccessCoarseLocationPermission())
    val MICROPHONE_GROUP = listOf(PermissionLists.getRecordAudioPermission())
    val PHONE_GROUP = listOf(PermissionLists.getReadPhoneStatePermission(), PermissionLists.getCallPhonePermission(), PermissionLists.getReadCallLogPermission(), PermissionLists.getWriteCallLogPermission(), PermissionLists.getAddVoicemailPermission(), PermissionLists.getUseSipPermission(), PermissionLists.getProcessOutgoingCallsPermission())
    val SENSORS_GROUP = listOf(PermissionLists.getBodySensorsPermission())
    val SMS_GROUP = listOf(PermissionLists.getSendSmsPermission(), PermissionLists.getReceiveSmsPermission(), PermissionLists.getReadSmsPermission(), PermissionLists.getReceiveWapPushPermission(), PermissionLists.getReceiveMmsPermission())
    val STORAGE_GROUP = listOf(PermissionLists.getReadMediaImagesPermission(), PermissionLists.getReadMediaVideoPermission(), PermissionLists.getReadMediaAudioPermission())
    val MEDIA_LOCATION_GROUP = listOf(PermissionLists.getAccessMediaLocationPermission())
    val ACTIVITY_RECOGNITION_GROUP = listOf(PermissionLists.getActivityRecognitionPermission())

    /**
     * 获取存储权限组
     */
    @JvmStatic
    fun getStorageGroup(): Array<String> {
        val deviceSdkInt = Build.VERSION.SDK_INT
        return when {
            // Android 13+ 设备，使用媒体权限
            deviceSdkInt >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(PermissionNames.READ_MEDIA_IMAGES, PermissionNames.READ_MEDIA_VIDEO, PermissionNames.READ_MEDIA_AUDIO)
            }
            // Android 10-12：使用 READ_EXTERNAL_STORAGE（已启用 requestLegacyExternalStorage=true）
            deviceSdkInt >= Build.VERSION_CODES.Q -> {
                arrayOf(PermissionNames.READ_EXTERNAL_STORAGE)
            }
            // Android 9 及以下，需要读写权限
            else -> {
                arrayOf(PermissionNames.READ_EXTERNAL_STORAGE, PermissionNames.WRITE_EXTERNAL_STORAGE)
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
fun Context.checkSelfStorage() = checkSelfPermission(this, *XXPermissionsGroup.STORAGE)

/**
 * 媒体位置权限组
 */
fun Context.checkSelfMediaLocation() = checkSelfPermission(this, *XXPermissionsGroup.MEDIA_LOCATION)

/**
 * 活动识别权限组
 */
fun Context.checkSelfActivityRecognition() = checkSelfPermission(this, *XXPermissionsGroup.ACTIVITY_RECOGNITION)

/**
 * 权限检测
 */
private fun checkSelfPermission(context: Context, vararg permissions: String): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}