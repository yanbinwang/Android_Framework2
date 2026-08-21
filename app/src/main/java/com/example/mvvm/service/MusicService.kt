package com.example.mvvm.service

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.common.base.page.Extra
import com.example.common.utils.function.decodeResource
import com.example.common.utils.function.intentString
import com.example.framework.utils.function.TrackableLifecycleService
import com.example.framework.utils.function.value.toSafeLong
import com.example.mvvm.R
import com.example.mvvm.service.receiver.MusicPlaybackReceiver
import com.example.mvvm.service.receiver.MusicPlaybackReceiver.Companion.createMediaAction
import com.example.thirdparty.media.utils.MediaHelper
import com.example.thirdparty.utils.NotificationUtil.NOTIFY_ID_AUDIO_MEDIA
import com.example.thirdparty.utils.NotificationUtil.buildMediaNotification
import java.util.Locale

class MusicService : TrackableLifecycleService() {
    private val media by lazy { MediaHelper(this, false, false) }
    private val mediaSession by lazy {
        MediaSessionCompat(this, javaClass.simpleName.lowercase(Locale.getDefault())).apply {
            setCallback(object : MediaSessionCompat.Callback() {
                /**
                 * 开始播放逻辑
                 */
                override fun onPlay() {
                    super.onPlay()
                    mediaOnPlay()
                }

                /**
                 * 暂停逻辑
                 */
                override fun onPause() {
                    super.onPause()
                    mediaOnPause()
                }

                override fun onStop() {
                    super.onStop()
                    mediaOnStop()
                }
            })
            // 激活 Session
            isActive = true
            // 设置支持的媒体按键（告诉系统能响应哪些按钮）
            val capabilities = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE
            // 设置初始状态（必须是 STOPPED 或 NONE，不能省略）
            val initialState = PlaybackStateCompat.Builder()
                .setActions(capabilities)
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                .build()
            setPlaybackState(initialState)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 生命周期绑定
        media.addObserver(this)
        // 媒体监听
        media.setOnPreparedListener {
            mediaOnPlay()
        }
        media.setOnErrorListener { _, _, _ ->
            mediaOnStop()
        }
        media.setOnCompletionListener {
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
            updateMediaNotification()
        }
        // 创建/更新通知
        updateMediaNotification()
    }

    private fun mediaOnPlay() {
        media.start()
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    private fun mediaOnPause() {
        media.pause()
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
    }

    private fun mediaOnStop() {
        media.stop()
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
    }

    /**
     * 更新播放器状态
     */
    private fun updatePlaybackState(@PlaybackStateCompat.State state: Int) {
//        val capabilities = PlaybackStateCompat.ACTION_PLAY or
//                PlaybackStateCompat.ACTION_PAUSE or
//                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        val capabilities = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(capabilities)
            .setState(state, media.getCurrentPosition().toSafeLong(), 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    /**
     * 当播放状态/歌曲变化时调用此方法更新通知
     * 更新:
     *  仅在媒体信息变化或需要刷新通知UI时调用（重量，低频调用）
     *  onPlay/onPause → 只调 updatePlaybackState()
     *  onCompletion / 切歌 / 首次创建 → updatePlaybackState() + updateMediaNotification()
     */
    private fun updateMediaNotification() {
        val title = "歌曲/视频标题"
        val artist = "艺术家/频道名"
        val albumArt = decodeResource(R.mipmap.ic_launcher)
        val notification = buildMediaNotification(
            token = mediaSession.sessionToken,
            title = title,
            artist = artist,
            albumArt = albumArt,
            actions = createMediaActions(),
            compactActionIndices = intArrayOf(0) // 折叠态只显示播放/暂停
        )
        // 启动前台服务（Android 15要求必须在启动服务后5秒内调用）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFY_ID_AUDIO_MEDIA, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFY_ID_AUDIO_MEDIA, notification)
        }
    }

    /**
     * 创建媒体播放控制按钮（通过广播触发）
     */
    private fun createMediaActions(): List<NotificationCompat.Action> {
        return listOf(
//            createMediaAction(MusicPlaybackReceiver.ACTION_PREVIOUS, android.R.drawable.ic_media_previous, "上一首"),
            createMediaAction(MusicPlaybackReceiver.ACTION_PLAY_PAUSE, android.R.drawable.ic_media_pause, "播放/暂停", mediaSession.sessionToken),
//            createMediaAction(MusicPlaybackReceiver.ACTION_NEXT, android.R.drawable.ic_media_next, "下一首")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 设置资源
        val fileUri = intent.intentString(Extra.SOURCE)
        media.setDataSource(fileUri, false)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        media.release()
        mediaSession.release()
    }

}