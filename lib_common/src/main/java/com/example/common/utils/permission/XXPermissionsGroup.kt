package com.example.common.utils.permission

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
    val STORAGE = arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
}