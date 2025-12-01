package com.example.thirdparty.media.utils

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.logWTF
import java.io.IOException

/**
 * @description 音频播放帮助类
 * @author yan
 */
class MediaHelper(owner: LifecycleOwner) : LifecycleEventObserver {
    // 当前 MediaPlayer 状态（辅助判断，避免依赖 isPlaying() 单一状态）
    private var currentState = State.IDLE
    // 暴露外部回调
    private var onPreparedListener: (() -> Unit)? = null
    private var onErrorListener: ((what: Int, extra: Int) -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    // 懒加载 MediaPlayer，初始化时设置基础监听器
    private val player by lazy {
        MediaPlayer().also {
            // 监听准备完成（异步准备后触发）
            it.setOnPreparedListener {
                "准备完成，当前状态：PREPARED".logWTF(TAG)
                onPreparedListener?.invoke()
            }
            // 监听播放错误（包括状态违规、数据源错误等）
            it.setOnErrorListener { _, what, extra ->
                "播放错误：what=$what, extra=$extra".logWTF(TAG)
                onErrorListener?.invoke(what, extra)
                // 返回 true 表示已处理错误，避免系统默认弹窗
                true
            }
            // 监听播放完成（非循环播放时触发）
            it.setOnCompletionListener {
                "播放完成".logWTF(TAG)
                onCompletionListener?.invoke()
            }
        }
    }

    companion object {
        // 日志tag
        private const val TAG = "MediaHelper"

        // MediaPlayer 核心状态枚举（对应官方状态机）
        private enum class State {
            IDLE,       // 初始状态
            PREPARING,  // 准备中（异步准备）
            PREPARED,   // 准备完成
            STARTED,    // 播放中
            PAUSED,     // 暂停中
            STOPPED,    // 已停止
            COMPLETED,  // 播放完成
            ERROR       // 错误状态
        }
    }

    init {
        owner.lifecycle.addObserver(this)
    }

    /**
     * 设置播放数据源（支持重复调用切换音频）
     * @param sourcePath 音频路径（本地文件/网络地址）
     * @param looping 是否循环播放
     */
    fun setDataSource(sourcePath: String, looping: Boolean = true) {
        try {
            // 切换数据源前，先重置状态（避免状态违规）
            resetPlayer()
            player.apply {
                setDataSource(sourcePath)
                isLooping = looping
                currentState = State.PREPARING
                // 异步准备（适合网络流/大文件，避免阻塞主线程）
                prepareAsync()
            }
            "开始异步准备，数据源：$sourcePath".logWTF(TAG)
        } catch (e: IOException) {
            "设置数据源失败：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(MediaPlayer.MEDIA_ERROR_IO, 0)
        } catch (e: Exception) {
            "设置数据源异常：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }

    /**
     * 开始播放（仅在 PREPARED/PAUSED/COMPLETED 状态有效）
     */
    fun start() {
        try {
            when (currentState) {
                State.PREPARED, State.PAUSED, State.COMPLETED -> {
                    player.start()
                    currentState = State.STARTED
                    "开始播放，当前进度：${player.currentPosition}ms".logWTF(TAG)
                }
                State.STARTED -> "已在播放中，无需重复调用".logWTF(TAG)
                else -> "当前状态不允许播放：$currentState".logWTF(TAG)
            }
        } catch (e: Exception) {
            "播放失败：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }

    /**
     * 暂停播放（仅在 STARTED 状态有效）
     */
    fun pause() {
        try {
            if (currentState == State.STARTED) {
                player.pause()
                currentState = State.PAUSED
                "暂停播放，当前进度：${player.currentPosition}ms".logWTF(TAG)
            } else {
                "当前状态不允许暂停：$currentState".logWTF(TAG)
            }
        } catch (e: Exception) {
            "暂停失败：${e.message}".logWTF(TAG)
        }
    }

    /**
     * 停止播放（仅在 STARTED/PAUSED/COMPLETED 状态有效）
     * 停止后需重新 prepare 才能播放
     */
    fun stop() {
        try {
            when (currentState) {
                State.STARTED, State.PAUSED, State.COMPLETED -> {
                    player.stop()
                    currentState = State.STOPPED
                    "停止播放，进度重置为 0ms".logWTF(TAG)
                }
                State.STOPPED -> "已停止播放，无需重复调用".logWTF(TAG)
                else -> "当前状态不允许停止：$currentState".logWTF(TAG)
            }
        } catch (e: Exception) {
            "停止失败：${e.message}".logWTF(TAG)
        }
    }

    /**
     * 释放资源（生命周期 ON_DESTROY 时调用，彻底销毁）
     */
    fun release() {
        try {
            // 先移除所有监听器，避免内存泄漏
            player.setOnPreparedListener(null)
            player.setOnErrorListener(null)
            player.setOnCompletionListener(null)
            // 根据当前状态安全停止/释放
            when (currentState) {
                State.STARTED, State.PAUSED, State.COMPLETED -> player.stop()
                else -> {}
            }
            player.reset()
            player.release()
            currentState = State.IDLE
            "资源已释放".logWTF(TAG)
        } catch (e: Exception) {
            "释放资源失败：${e.message}".logWTF(TAG)
        }
    }

    /**
     * 跳转到指定位置（仅在 PREPARED/STARTED/PAUSED/COMPLETED 状态有效）
     * @param position 目标位置（毫秒）
     */
    fun seekTo(position: Int) {
        try {
            if (currentState in listOf(State.PREPARED, State.STARTED, State.PAUSED, State.COMPLETED)) {
                val validPosition = position.coerceIn(0, player.duration) // 限制在合法范围
                player.seekTo(validPosition)
                "跳转到 $validPosition ms（总时长：${player.duration}ms）".logWTF(TAG)
            } else {
                "当前状态不允许跳转：$currentState".logWTF(TAG)
            }
        } catch (e: Exception) {
            "跳转失败：${e.message}".logWTF(TAG)
        }
    }

    /**
     * 重置播放器（清空数据源、状态，用于切换音频）
     */
    private fun resetPlayer() {
        try {
            // 先停止播放（若处于播放/暂停状态）
            stop()
            player.reset()
            currentState = State.IDLE
            "播放器已重置".logWTF(TAG)
        } catch (e: Exception) {
            "重置播放器失败：${e.message}".logWTF(TAG)
        }
    }

    fun isPlaying(): Boolean {
        return currentState == State.STARTED
    }

    fun getCurrentPosition(): Int {
        return try {
            if (currentState in listOf(State.PREPARED, State.STARTED, State.PAUSED, State.COMPLETED)) {
                player.currentPosition
            } else {
                0
            }
        } catch (e: Exception) {
            "获取当前进度失败：${e.message}".logWTF(TAG)
            0
        }
    }

    fun getDuration(): Int {
        return try {
            if (currentState in listOf(State.PREPARED, State.STARTED, State.PAUSED, State.COMPLETED)) {
                player.duration
            } else {
                0
            }
        } catch (e: Exception) {
            "获取总时长失败：${e.message}".logWTF(TAG)
            0
        }
    }

    fun setOnPreparedListener(listener: (() -> Unit)?) {
        this.onPreparedListener = listener
    }

    fun setOnErrorListener(listener: ((what: Int, extra: Int) -> Unit)?) {
        this.onErrorListener = listener
    }

    fun setOnCompletionListener(listener: (() -> Unit)?) {
        this.onCompletionListener = listener
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
//            // 初次加载页面/切回页面时自动恢复播放，开始播放
//            Lifecycle.Event.ON_RESUME -> {
//                start()
//                "生命周期 ON_RESUME，开始播放".logWTF(TAG)
//            }
//            // 页面退到后台（如按Home键），暂停播放
//            Lifecycle.Event.ON_PAUSE -> {
//                pause()
//                "生命周期 ON_PAUSE，暂停播放".logWTF(TAG)
//            }
            // 页面销毁，释放所有资源
            Lifecycle.Event.ON_DESTROY -> {
                release()
                source.lifecycle.removeObserver(this)
                "生命周期 ON_DESTROY，释放资源".logWTF(TAG)
            }
            else -> {}
        }
    }

}