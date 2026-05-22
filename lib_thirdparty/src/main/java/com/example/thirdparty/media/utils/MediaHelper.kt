package com.example.thirdparty.media.utils

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.logWTF
import java.io.IOException

/**
 * @description 音频播放帮助类
 * @author yan
 * @autoResume 是否允许生命周期自动恢复播放（默认关闭，可开启）
 * @autoPause 是否允许生命周期自动暂停播放（默认开启，可关闭）
 */
class MediaHelper(mActivity: FragmentActivity, private val autoResume: Boolean = false, private val autoPause: Boolean = true) : LifecycleEventObserver {
    // 当前 MediaPlayer 状态（辅助判断，避免依赖 isPlaying() 单一状态）
    private var currentState = State.IDLE
    // 暴露外部回调
    private var onPreparedListener: ((mp: MediaPlayer) -> Unit)? = null
    private var onErrorListener: ((mp: MediaPlayer, what: Int, extra: Int) -> Unit)? = null
    private var onCompletionListener: ((mp: MediaPlayer) -> Unit)? = null
    // 懒加载 MediaPlayer，初始化时设置基础监听器
    private val player by lazy {
        MediaPlayer().also {
            // 监听准备完成（异步准备后触发）
            it.setOnPreparedListener { mp ->
                currentState = State.PREPARED
                "准备完成，当前状态：${currentState}".logWTF(TAG)
                onPreparedListener?.invoke(mp)
            }
            // 监听播放错误（包括状态违规、数据源错误等）
            it.setOnErrorListener { mp, what, extra ->
                currentState = State.ERROR
                "播放错误：what=$what, extra=$extra，当前状态：${currentState}".logWTF(TAG)
                onErrorListener?.invoke(mp, what, extra)
                // 返回 true 表示已处理错误，避免系统默认弹窗
                true
            }
            // 监听播放完成（非循环播放时触发）
            it.setOnCompletionListener { mp ->
                currentState = State.COMPLETED
                "播放完成，当前状态：${currentState}".logWTF(TAG)
                onCompletionListener?.invoke(mp)
            }
        }
    }
    // 音频焦点管理器
    private val isOreoOrHigher get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    private val audioManager by lazy { mActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var audioFocusRequest: AudioFocusRequest? = null
    // 低版本焦点监听器
    private val lowVersionFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        handleAudioFocusChange(focusChange)
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
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 设置播放数据源（支持重复调用切换音频）
     * @param sourcePath 音频路径（本地文件/网络地址）
     * @param looping 是否循环播放
     */
    fun setDataSource(sourcePath: String, looping: Boolean = true) {
        try {
            // 申请音频焦点
            val focusGranted = requestAudioFocus()
            if (!focusGranted) {
                "音频焦点申请失败，可能无法播放".logWTF(TAG)
                currentState = State.ERROR
                onErrorListener?.invoke(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
                return
            }
            // 切换数据源前，先重置状态（避免状态违规）
            resetPlayer()
            // 设置数据源并异步准备
            player.apply {
                setDataSource(sourcePath)
                isLooping = looping
                currentState = State.PREPARING
                // 异步准备（适合网络流/大文件，避免阻塞主线程）
                prepareAsync()
            }
            "开始异步准备，数据源：${sourcePath}，当前状态：${currentState}".logWTF(TAG)
        } catch (e: IOException) {
            "设置数据源失败（IO异常）：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(player, MediaPlayer.MEDIA_ERROR_IO, 0)
        } catch (e: Exception) {
            "设置数据源异常（未知错误）：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }

    /**
     * 申请音频焦点（适配23+）
     * @return 是否申请成功
     */
    private fun requestAudioFocus(): Boolean {
        return if (isOreoOrHigher) {
            // 26+ 使用新API
            if (audioFocusRequest == null) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }.build()
            }
            val focusRequest = audioFocusRequest
            if (null == focusRequest) {
                "AudioFocusRequest 初始化失败，无法申请音频焦点".logWTF(TAG)
                false
            } else {
                val result = audioManager.requestAudioFocus(focusRequest)
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } else {
            // 23-25 使用旧API
            val result = audioManager.requestAudioFocus(lowVersionFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * 处理音频焦点变化（统一逻辑，兼容高低版本）
     */
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // 永久失去焦点，停止播放
                stop()
                "永久失去音频焦点，停止播放".logWTF(TAG)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 临时失去焦点，暂停播放
                pause()
                "临时失去音频焦点，暂停播放".logWTF(TAG)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 恢复焦点，自动播放（根据autoResume）
                if (autoResume) {
                    start()
                    "恢复音频焦点，自动播放".logWTF(TAG)
                }
            }
        }
    }

    /**
     * 开始播放（仅在 PREPARED/PAUSED/COMPLETED 状态有效）
     */
    fun start() {
        try {
            // 切回前台时，如果currentState标记是STARTED，但底层MediaPlayer已经被系统暂停了，把状态校准为PAUSED，确保autoResume=true能正常触发播放
            if (currentState == State.STARTED && !player.isPlaying) {
                currentState = State.PAUSED
            }
            when (currentState) {
                State.PREPARED, State.PAUSED, State.COMPLETED -> {
                    player.start()
                    currentState = State.STARTED
                    "开始播放，当前进度：${player.currentPosition}ms，状态：${currentState}".logWTF(TAG)
                }
                State.STARTED -> "已在播放中，无需重复调用（状态：${currentState}）".logWTF(TAG)
                else -> "当前状态不允许播放：${currentState}".logWTF(TAG)
            }
        } catch (e: Exception) {
            "播放失败：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
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
                "暂停播放，当前进度：${player.currentPosition}ms，状态：${currentState}".logWTF(TAG)
            } else {
                "当前状态不允许暂停：${currentState}".logWTF(TAG)
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
                    "停止播放，进度重置为 0ms，状态：${currentState}".logWTF(TAG)
                }
                State.STOPPED -> "已停止播放，无需重复调用（状态：${currentState}）".logWTF(TAG)
                else -> "当前状态不允许停止：${currentState}".logWTF(TAG)
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
            // 释放音频焦点
            releaseAudioFocus()
            // 移除所有监听器，避免内存泄漏
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
            "资源已释放，当前状态：${currentState}".logWTF(TAG)
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
                // 限制在合法范围
                val validPosition = position.coerceIn(0, player.duration)
                player.seekTo(validPosition)
                "跳转到 $validPosition ms（总时长：${player.duration}ms），状态：${currentState}".logWTF(TAG)
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
            "播放器已重置，当前状态：${currentState}".logWTF(TAG)
        } catch (e: Exception) {
            "重置播放器失败：${e.message}".logWTF(TAG)
            currentState = State.ERROR
            onErrorListener?.invoke(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }

    /**
     * 释放音频焦点
     */
    private fun releaseAudioFocus() {
        if (isOreoOrHigher) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
            }
        } else {
            audioManager.abandonAudioFocus(lowVersionFocusListener)
        }
    }

    /**
     * 是否在播放
     */
    fun isPlaying(): Boolean {
        return currentState == State.STARTED
    }

    /**
     * 当前播放进度 (毫秒（ms）)
     */
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

    /**
     * 音频总时长（毫秒（ms））
     */
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

    /**
     * 准备/出错/完成 回调监听
     */
    fun setOnPreparedListener(listener: ((mp: MediaPlayer) -> Unit)) {
        this.onPreparedListener = listener
    }

    fun setOnErrorListener(listener: ((mp: MediaPlayer, what: Int, extra: Int) -> Unit)) {
        this.onErrorListener = listener
    }

    fun setOnCompletionListener(listener: ((mp: MediaPlayer) -> Unit)) {
        this.onCompletionListener = listener
    }

    /**
     * 页面生命周期变化回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            // 初次加载页面/切回页面时自动恢复播放，开始播放
            Lifecycle.Event.ON_RESUME -> {
                if (autoResume) {
                    start()
                }
                "生命周期 ON_RESUME，自动恢复播放（autoResume：$autoResume）".logWTF(TAG)
            }
            // 页面退到后台（如按Home键），暂停播放
            Lifecycle.Event.ON_PAUSE -> {
                if (autoPause) {
                    pause()
                }
                "生命周期 ON_PAUSE，自动暂停播放（autoPause：${autoPause}）".logWTF(TAG)
            }
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