package com.example.multimedia.utils.helper

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.example.multimedia.utils.MediaType.AUDIO
import com.example.multimedia.utils.MultimediaUtil

/**
 * @description 录音帮助类（熄屏后无声音，并可能会导致后续声音也录制不了）
 * @author yan
 */
class RecorderHelper {
    private val player by lazy { MediaPlayer() }
    private var recorder: MediaRecorder? = null

    companion object {
        internal var onRecorderListener: OnRecorderListener? = null

        fun setOnRecorderListener(onRecorderListener: OnRecorderListener) {
            this.onRecorderListener = onRecorderListener
        }
    }

    /**
     * 开始录音
     */
    fun startRecord(context: Context) {
        val recordFile = MultimediaUtil.getOutputFile(AUDIO)
        val sourcePath = recordFile?.absolutePath
        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)//设置麦克风
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setOutputFile(sourcePath) else setOutputFile(recordFile)
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
        onRecorderListener?.onStart(sourcePath)
    }

    /**
     * 停止录音
     */
    fun stopRecord() {
        onRecorderListener?.onShutter()
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        }
        onRecorderListener?.onStop()
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
     * 当前音频是否正在播放
     */
    fun isPlaying() = player.isPlaying

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

}

interface OnRecorderListener {

    fun onStart(sourcePath: String?)

    fun onShutter()

    fun onStop()

}