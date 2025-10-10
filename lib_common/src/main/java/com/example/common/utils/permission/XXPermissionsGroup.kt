package com.example.common.utils.permission

import android.content.Context
import com.example.common.utils.permission.XXPermissionsGroup.ACTIVITY_RECOGNITION_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.CALENDAR_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.CAMERA_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.CONTACTS_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.LOCATION_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.MEDIA_LOCATION_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.MICROPHONE_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.PHONE_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.SENSORS_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.SMS_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.STORAGE_GROUP
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists

/**
 * 方便直接通过权限组提取权限
 * 特殊权限 (部分权限不属于任何组，需单独处理)
 * ACCESS_BACKGROUND_LOCATION：后台定位（需先获得 ACCESS_FINE_LOCATION）。
 * MANAGE_EXTERNAL_STORAGE：管理所有文件（Android 11+，需特殊申请）。
 * SYSTEM_ALERT_WINDOW：悬浮窗权限。
 */
object XXPermissionsGroup {
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
}

/**
 * 日历权限组
 */
fun Context.checkSelfCalendar() = XXPermissions.isGrantedPermissions(this, CALENDAR_GROUP)

/**
 * 相机权限组
 */
fun Context.checkSelfCamera() = XXPermissions.isGrantedPermissions(this, CAMERA_GROUP)

/**
 * 联系人权限组
 */
fun Context.checkSelfContacts() = XXPermissions.isGrantedPermissions(this, CONTACTS_GROUP)

/**
 * 定位权限组
 */
fun Context.checkSelfLocation() = XXPermissions.isGrantedPermissions(this, LOCATION_GROUP)

/**
 * 麦克风权限组
 */
fun Context.checkSelfMicrophone() = XXPermissions.isGrantedPermissions(this, MICROPHONE_GROUP)

/**
 * 手机权限组
 */
fun Context.checkSelfPhone() = XXPermissions.isGrantedPermissions(this, PHONE_GROUP)

/**
 * 传感器权限组
 */
fun Context.checkSelfSensors() = XXPermissions.isGrantedPermissions(this, SENSORS_GROUP)

/**
 * 短信权限组
 */
fun Context.checkSelfSMS() = XXPermissions.isGrantedPermissions(this, SMS_GROUP)

/**
 * 存储权限组
 */
fun Context.checkSelfStorage() = XXPermissions.isGrantedPermissions(this, STORAGE_GROUP)

/**
 * 媒体位置权限组
 * 允许应用访问媒体文件中的地理位置信息（如照片的 EXIF 位置） --> Android 10 (API 29) 及以上。
 */
fun Context.checkSelfMediaLocation() = XXPermissions.isGrantedPermissions(this, MEDIA_LOCATION_GROUP)

/**
 * 活动识别权限组
 * 允许应用识别用户的身体活动（如步行、跑步、骑行） --> Android 10 (API 29) 及以上。
 */
fun Context.checkSelfActivityRecognition() = XXPermissions.isGrantedPermissions(this, ACTIVITY_RECOGNITION_GROUP)