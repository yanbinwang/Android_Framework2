package com.example.thirdparty.firebase.service

import com.example.common.utils.toJson
import com.example.framework.utils.logWTF
import com.example.thirdparty.firebase.utils.FireBaseUtil
import com.example.thirdparty.firebase.utils.FireBaseUtil.notificationHandler
import com.example.thirdparty.firebase.utils.FireBaseUtil.notificationIntentGenerator
import com.example.thirdparty.utils.NotificationUtil.showSimpleNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 推送服务
 */
class FirebaseService : FirebaseMessagingService() {

//    companion object {
//        private var debuggingListener: ((header: String?, method: String?, url: String?, params: String?, code: Int?, body: String?) -> Unit)? = null
//
//        /**
//         * 请求头，请求方式，请求地址，请求参数，响应编码，响应体
//         */
//        @JvmStatic
//        fun setOnDebuggingListener(listener: ((header: String?, method: String?, url: String?, params: String?, code: Int?, body: String?) -> Unit)) {
//            this.debuggingListener = listener
//        }
//    }

    override fun onNewToken(token: String) {
        FireBaseUtil.tokenRefreshListener?.invoke(token)
    }

    /**
     * RemoteMessage中的各个参数：
     *
     * data
     * 这是一个 Map<String, String> 类型的属性，用于存储消息中的自定义数据。当你通过 FCM 发送消息时，
     * 可以在消息中包含自定义的键值对数据，这些数据会存储在 data 中。你可以通过 msg.data 来获取这些数据
     *
     * notification
     * 这是一个 RemoteMessage.Notification 类型的属性，用于存储消息中的通知相关信息，例如通知的标题、内容、图标等
     * 如果消息是一个通知消息，你可以通过 msg.notification 来获取这些信息
     *
     * messageId
     * 这是一个 String 类型的属性，用于唯一标识这条消息。每条 FCM 消息都有一个唯一的消息 ID
     *
     * from
     * 这是一个 String 类型的属性，用于表示消息的来源。通常是发送消息的主题或者发送者的标识符
     *
     * sentTime
     * 这是一个 Long 类型的属性，用于表示消息的发送时间，单位是毫秒
     */
    override fun onMessageReceived(msg: RemoteMessage) {
        if (notificationHandler?.invoke(msg.data) != true) {
            val map = msg.data
            "msg:${msg.toJson()}\nmap:${map.toJson()}".logWTF
//            debuggingListener?.invoke("", "PUSH", mapOf("messageId" to msg.messageId, "from" to msg.from, "sentTime" to msg.sentTime).toJson(), msg.notification.toJson(), 200, map.toJson())
            showSimpleNotification(
                msg.notification?.title,
                msg.notification?.body,
                msg.notification?.imageUrl?.toString(),
                notificationIntentGenerator(this, map))
        }
    }

}