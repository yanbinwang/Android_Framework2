package com.example.live.config

import android.content.pm.ActivityInfo

object Config {
    const val DEBUG_MODE = false
    const val FILTER_ENABLED = false
    const val SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    const val HINT_ENCODING_ORIENTATION_CHANGED = "Encoding orientation had been changed. Stop streaming first and restart streaming will take effect"
    // Streaming config constant name
    const val NAME_ENCODING_CONFIG = "EncodingConfig"
    const val NAME_CAMERA_CONFIG = "CameraConfig"
    const val PUBLISH_URL = "PUBLISH_URL"
    const val TRANSFER_MODE_QUIC = "TRANSFER_MODE_QUIC"
    const val TRANSFER_MODE_SRT = "TRANSFER_MODE_SRT"
    const val AUDIO_CHANNEL_STEREO = "AUDIO_CHANNEL_STEREO"
    const val AUDIO_VOIP_RECORD = "AUDIO_VOIP_RECORD"
    const val AUDIO_SCO_ON = "AUDIO_SCO_ON"
    const val SP_NAME = "PLDroidMediaStreamingDemo"
    const val KEY_USER_ID = "userId"
}