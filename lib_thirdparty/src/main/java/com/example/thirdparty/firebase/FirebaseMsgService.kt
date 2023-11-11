package com.example.thirdparty.firebase

import com.example.common.utils.NotificationUtil.showSimpleNotification
import com.example.thirdparty.firebase.FireBaseUtil.notificationHandler
import com.example.thirdparty.firebase.FireBaseUtil.notificationIntentGenerator
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * firebase服务
 * yan
 */
class FirebaseMsgService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        FireBaseUtil.tokenRefreshListener?.invoke(token)
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        if (notificationHandler?.invoke(msg.data) != true) {
            val map = msg.data
            showSimpleNotification(
                msg.notification?.title,
                msg.notification?.body,
                msg.notification?.imageUrl?.toString(),
                notificationIntentGenerator(this, map)
            )
        }
    }

}