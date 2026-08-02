package com.example.mvvm.service

import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.common.utils.function.decodeResource
import com.example.framework.utils.function.TrackableLifecycleService
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logWTF
import com.example.mvvm.R
import com.example.mvvm.service.receiver.MediaPlaybackReceiver
import com.example.mvvm.service.receiver.MediaPlaybackReceiver.Companion.createMediaAction
import com.example.thirdparty.media.utils.MediaHelper
import com.example.thirdparty.utils.NotificationUtil.buildMediaNotification

class MusicService : TrackableLifecycleService() {

    companion object {
        private const val NOTIFY_ID_MEDIA = 1001
        var media: MediaHelper? = null
        var mediaSession: MediaSessionCompat? = null
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 MediaSession（实际项目中应在 PlaybackManager 中管理）
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                /**
                 * 开始播放逻辑
                 */
                override fun onPlay() {
                    super.onPlay()
                    media?.start()
                    // 播放时必须更新状态为 STATE_PLAYING
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                /**
                 * 暂停逻辑
                 */
                override fun onPause() {
                    super.onPause()
                    media?.pause()
                    // 暂停时必须更新状态为 STATE_PAUSED
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                /**
                 * 下一首逻辑
                 */
                override fun onSkipToNext() {
                    super.onSkipToNext()
                    "下一首".logWTF("wyb")
                }

                /**
                 * 上一首逻辑
                 */
                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    "上一首".logWTF("wyb")
                }
            })
            // 激活 Session
            isActive = true
            // 设置支持的媒体按键（告诉系统能响应哪些按钮）
            val capabilities = PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            // 设置初始状态（必须是 STOPPED 或 NONE，不能省略）
            val initialState = PlaybackStateCompat.Builder()
                .setActions(capabilities)
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                .build()
            setPlaybackState(initialState)
        }
        // 创建通知
        updateMediaNotification("歌曲/视频标题", "艺术家/频道名", decodeResource(R.mipmap.ic_launcher))
    }

    /**
     * 更新播放器状态
     */
    private fun updatePlaybackState(@PlaybackStateCompat.State state: Int) {
        val capabilities = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(capabilities)
            .setState(state, media?.getCurrentPosition().toSafeLong(), 1.0f)
            .build()
        mediaSession?.setPlaybackState(playbackState)
    }

    /**
     * 当播放状态/歌曲变化时调用此方法更新通知
     */
    private fun updateMediaNotification(title: String, artist: String?, albumArt: Bitmap?) {
        val token = mediaSession?.sessionToken ?: throw IllegalStateException("MediaSession 未初始化")
        val notification = buildMediaNotification(
            token = token,
            title = title,
            artist = artist,
            albumArt = albumArt,
            actions = createMediaActions(),
            compactActionIndices = intArrayOf(1) // 折叠态只显示播放/暂停
        )
        // 启动前台服务（Android 15要求必须在启动服务后5秒内调用）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFY_ID_MEDIA, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFY_ID_MEDIA, notification)
        }
    }

    /**
     * 创建媒体播放控制按钮（通过广播触发）
     */
    private fun createMediaActions(): List<NotificationCompat.Action> {
        return listOf(
            createMediaAction(MediaPlaybackReceiver.ACTION_PREVIOUS, android.R.drawable.ic_media_previous, "上一首"),
            createMediaAction(MediaPlaybackReceiver.ACTION_PLAY_PAUSE, android.R.drawable.ic_media_pause, "播放/暂停"),
            createMediaAction(MediaPlaybackReceiver.ACTION_NEXT, android.R.drawable.ic_media_next, "下一首")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
        media?.release()
        media = null
    }

}