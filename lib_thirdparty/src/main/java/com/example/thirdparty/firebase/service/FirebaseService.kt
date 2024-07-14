package com.example.thirdparty.firebase.service

import com.example.common.utils.toJsonString
import com.example.framework.utils.logWTF
import com.example.thirdparty.firebase.FireBaseUtil
import com.example.thirdparty.firebase.FireBaseUtil.notificationHandler
import com.example.thirdparty.firebase.FireBaseUtil.notificationIntentGenerator
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 推送服务
 */
class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        FireBaseUtil.tokenRefreshListener?.invoke(token)
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        if (notificationHandler?.invoke(msg.data) != true) {
            val map = msg.data
            "msg:${msg.toJsonString()}\nmap:${map.toJsonString()}".logWTF
            showSimpleNotification(
                msg.notification?.title,
                msg.notification?.body,
                msg.notification?.imageUrl?.toString(),
                notificationIntentGenerator(this, map))
        }
    }

}