package com.example.mvvm.service.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.app.NotificationCompat
import com.example.common.utils.function.getBroadcastPendingIntent
import com.example.common.utils.helper.ConfigHelper.getPackageName
import com.example.mvvm.service.MusicService

/**
 * 媒体播放广播接收器
 */
class MediaPlaybackReceiver : BroadcastReceiver() {

    companion object {
        val ACTION_PREVIOUS = "${getPackageName()}.ACTION_PREVIOUS"
        val ACTION_PLAY_PAUSE = "${getPackageName()}.ACTION_PLAY_PAUSE"
        val ACTION_NEXT = "${getPackageName()}.ACTION_NEXT"

        /**
         * 创建广播按钮
         */
        fun Context.createMediaAction(action: String, iconRes: Int, label: String): NotificationCompat.Action {
            val intent = Intent(this, MediaPlaybackReceiver::class.java).apply {
                this.action = action
            }
            // 用 action hashCode 作为 requestCode，确保每个按钮独立
            val pendingIntent = getBroadcastPendingIntent(action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            // 构建通知栏行为按钮
            return NotificationCompat.Action.Builder(iconRes, label, pendingIntent).build()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // 将广播转发给 MediaSessionCompat.Callback
        intent ?: return
        val token = MusicService.mediaSession?.sessionToken ?: return
        val controller = MediaControllerCompat(context, token)
        when (intent.action) {
            ACTION_PREVIOUS -> {
                controller.transportControls.skipToPrevious()
            }
            ACTION_PLAY_PAUSE -> {
                controller.transportControls.play()
            }
            ACTION_NEXT -> {
                controller.transportControls.skipToNext()
            }
        }
    }

}