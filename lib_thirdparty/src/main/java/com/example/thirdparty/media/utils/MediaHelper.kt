package com.example.thirdparty.media.utils

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @description 音频播放帮助类
 * @author yan
 */
class MediaHelper(owner: LifecycleOwner) : LifecycleEventObserver {
    private var onCompletionListener: ((MediaPlayer) -> Unit)? = null
    private var onErrorListener: ((MediaPlayer, Int, Int) -> Boolean)? = null
    private val player by lazy { MediaPlayer() }

    init {
        owner.lifecycle.addObserver(this)
    }

    /**
     * 设置播放的音频地址
     */
    fun setDataSource(sourcePath: String, looping: Boolean = true) {
        try {
            player.apply {
                setDataSource(sourcePath)
                isLooping = looping //设置是否循环播放
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开始播放
     */
    fun start() {
        try {
            if (isPlaying()) return
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止播放
     */
    fun pause() {
        try {
            if (!isPlaying()) return
            player.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 当前音频是否正在播放
     */
    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    /**
     * 获取多媒体
     */
    fun getMediaPlayer(): MediaPlayer {
        return player
    }

    /**
     * 获取当前播放进度
     */
    fun getCurrentPosition(): Int {
        return try {
            player.currentPosition
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Int) {
        try {
            player.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置播放完成监听器
     */
    fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
        onCompletionListener = listener
        player.setOnCompletionListener {
            onCompletionListener?.invoke(it)
        }
    }

    /**
     * 设置错误监听器
     */
    fun setOnErrorListener(listener: (MediaPlayer, Int, Int) -> Boolean) {
        onErrorListener = listener
        player.setOnErrorListener { mp, what, extra ->
            onErrorListener?.invoke(mp, what, extra) ?: false
        }
    }

    /**
     * 销毁-释放资源
     */
    fun release() {
        try {
            player.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 生命周期管控
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                release()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}