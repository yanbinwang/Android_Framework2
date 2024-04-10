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
class RecorderHelper(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var isDestroy = false
    private var listener: OnRecorderListener? = null
    private val player by lazy { MediaPlayer() }
    private val recorder by lazy { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(mActivity) else MediaRecorder() }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 开始录音
     */
    fun startRecord() {
        isDestroy = false
        val recordFile = MediaUtil.getOutputFile(AUDIO)
        val sourcePath = recordFile?.absolutePath
        listener?.onStart(sourcePath)
        try {
            recorder.apply {
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
            recorder.apply {
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

    interface OnRecorderListener {
        fun onStart(sourcePath: String?)

        fun onShutter()

        fun onStop()
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
     * 销毁-释放资源
     */
    private fun release() {
        try {
            player.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                isDestroy = true
                stopRecord()
                release()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}