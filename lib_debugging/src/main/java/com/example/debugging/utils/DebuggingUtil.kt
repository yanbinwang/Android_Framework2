package com.example.debugging.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.common.config.ServerConfig
import com.example.common.utils.function.string
import com.example.debugging.R
import com.example.debugging.activity.LogActivity
import com.example.debugging.bean.ExtraInput
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.thirdparty.utils.NotificationUtil.getPendingIntentFlags

/**
 * 调试库
 */
object DebuggingUtil {

    /**
     * 调用前做好isDebug的判断
     */
    @JvmStatic
    fun init(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val intent = Intent(context, LogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent =
            PendingIntent.getActivity(
                context, -1, intent, getPendingIntentFlags(
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
        val notification = NotificationCompat.Builder(context, string(R.string.notificationChannelId))
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(ServerConfig.serverName())
                .setContentText("本程序包为 " + ServerConfig.serverName() + " 包")
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(currentTimeStamp)
                .setContentIntent(pendingIntent)
                .build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        manager?.notify(2, notification)

//        LogActivity.addExtraInput(ExtraInput(
//            "CheeseHub服务器", {
//                cheesehubUrl = it
//            }, {
//                cheesehubUrl
//            }, {
//                AppServerConfig.CHEESEHUB_SERVER
//            }
//        ))
    }

}