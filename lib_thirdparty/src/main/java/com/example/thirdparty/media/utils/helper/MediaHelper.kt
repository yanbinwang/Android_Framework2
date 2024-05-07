package com.example.thirdparty.media.utils.helper

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.thirdparty.media.utils.MediaUtil
import com.example.thirdparty.media.utils.MediaUtil.MediaType.AUDIO

/**
 * @description 录音帮助类（熄屏后无声音，并可能会导致后续声音也录制不了）
 * @author yan
 */
class MediaHelper : LifecycleEventObserver {
    private var isDestroy = false
    private var recorder: MediaRecorder? = null
    private var listener: OnRecorderListener? = null
    private val player by lazy { MediaPlayer() }

    // <editor-fold defaultstate="collapsed" desc="初始化相关">
    constructor(mActivity: FragmentActivity?) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && null != mActivity) MediaRecorder(mActivity) else MediaRecorder()
        mActivity?.lifecycle?.addObserver(this)
    }

    constructor(observer: LifecycleOwner?) {
        recorder = MediaRecorder()
        observer?.lifecycle?.addObserver(this)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放相关">
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
        } catch (_: Exception) {
        }
    }

    /**
     * 开始播放
     */
    fun start() {
        try {
            if (isPlaying()) return
            player.start()
        } catch (_: Exception) {
        }
    }

    /**
     * 停止播放
     */
    fun pause() {
        try {
            if (!isPlaying()) return
            player.pause()
        } catch (_: Exception) {
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
     * 销毁-释放资源
     */
    fun release() {
        try {
            player.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="录音相关">
    /**
     * 开始录音
     */
    fun startRecord() {
        isDestroy = false
        val recordFile = MediaUtil.getOutputFile(AUDIO)
        val sourcePath = recordFile?.absolutePath
        listener?.onStart(sourcePath)
        try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)//设置麦克风
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setOutputFile(sourcePath)
                } else {
                    setOutputFile(recordFile)
                }
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 停止录音
     */
    fun stopRecord() {
        if (!isDestroy) listener?.onShutter()
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        }
        if (!isDestroy) listener?.onStop()
    }

    /**
     * 录音监听
     */
    fun setOnRecorderListener(listener: OnRecorderListener) {
        this.listener = listener
    }

    /**
     * 回调监听
     */
    interface OnRecorderListener {
        fun onStart(sourcePath: String?)

        fun onShutter()

        fun onStop()
    }
    // </editor-fold>

    /**
     * 生命周期管控
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                isDestroy = true
                stopRecord()
                release()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}