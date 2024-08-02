package com.example.live.fragment

import com.example.common.base.BaseFragment
import com.example.live.databinding.FragmentEncodingConfigBinding
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.WatermarkSetting

class EncodingConfigFragment : BaseFragment<FragmentEncodingConfigBinding>() {
    private val DEFAULT_VIDEO_QUALITY_POS = 6
    private val DEFAULT_AUDIO_QUALITY_POS = 3
    private val DEFAULT_H264_PROFILE_POS = 2
    private val DEFAULT_VIDEO_ENCODE_SIZE_POS = 1
    private val DEFAULT_WATERMARK_SIZE_POS = 1
    private val DEFAULT_WATERMARK_LOCATION_POS = 2
    private val DEFAULT_YUV_FILTER_MODE_POS = 1

    private val VIDEO_SIZE_PRESETS = arrayOf(
        "240p(320x240 (4:3), 424x240 (16:9))",
        "480p(640x480 (4:3), 848x480 (16:9))",
        "544p(720x544 (4:3), 960x544 (16:9))",
        "720p(960x720 (4:3), 1280x720 (16:9))",
        "1080p(1440x1080 (4:3), 1920x1080 (16:9))"
    )

    private val VIDEO_QUALITY_PRESETS = arrayOf(
        "LOW1(FPS:12, Bitrate:150kbps)",
        "LOW2(FPS:15, Bitrate:264kbps)",
        "LOW3(FPS:15, Bitrate:350kbps)",
        "MEDIUM1(FPS:30, Bitrate:512kbps)",
        "MEDIUM2(FPS:30, Bitrate:800kbps)",
        "MEDIUM3(FPS:30, Bitrate:1000kbps)",
        "HIGH1(FPS:30, Bitrate:1200kbps)",
        "HIGH2(FPS:30, Bitrate:1500kbps)",
        "HIGH3(FPS:30, Bitrate:2000kbps)"
    )

    private val VIDEO_QUALITY_PRESETS_MAPPING = intArrayOf(
        StreamingProfile.VIDEO_QUALITY_LOW1,
        StreamingProfile.VIDEO_QUALITY_LOW2,
        StreamingProfile.VIDEO_QUALITY_LOW3,
        StreamingProfile.VIDEO_QUALITY_MEDIUM1,
        StreamingProfile.VIDEO_QUALITY_MEDIUM2,
        StreamingProfile.VIDEO_QUALITY_MEDIUM3,
        StreamingProfile.VIDEO_QUALITY_HIGH1,
        StreamingProfile.VIDEO_QUALITY_HIGH2,
        StreamingProfile.VIDEO_QUALITY_HIGH3
    )

    private val VIDEO_QUALITY_PROFILES = arrayOf(
        "HIGH",
        "MAIN",
        "BASELINE"
    )

    private val VIDEO_QUALITY_PROFILES_MAPPING = arrayOf(
        StreamingProfile.H264Profile.HIGH,
        StreamingProfile.H264Profile.MAIN,
        StreamingProfile.H264Profile.BASELINE
    )

    private val AUDIO_QUALITY_PRESETS = arrayOf(
        "LOW1(SampleRate:44.1kHZ, Bitrate:18kbps)",
        "LOW2(SampleRate:44.1kHZ, Bitrate:24kbps)",
        "MEDIUM1(SampleRate:44.1kHZ, Bitrate:32kbps)",
        "MEDIUM2(SampleRate:44.1kHZ, Bitrate:48kbps)",
        "HIGH1(SampleRate:44.1kHZ, Bitrate:96kbps)",
        "HIGH2(SampleRate:44.1kHZ, Bitrate:128kbps)"
    )

    private val AUDIO_QUALITY_PRESETS_MAPPING = intArrayOf(
        StreamingProfile.AUDIO_QUALITY_LOW1,
        StreamingProfile.AUDIO_QUALITY_LOW2,
        StreamingProfile.AUDIO_QUALITY_MEDIUM1,
        StreamingProfile.AUDIO_QUALITY_MEDIUM2,
        StreamingProfile.AUDIO_QUALITY_HIGH1,
        StreamingProfile.AUDIO_QUALITY_HIGH2
    )

    private val WATERMARK_SIZE_PRESETS = arrayOf(
        "SMALL",
        "MEDIUM",
        "LARGE",
    )

    private val WATERMARK_SIZE_PRESETS_MAPPING = arrayOf(
        WatermarkSetting.WATERMARK_SIZE.SMALL,
        WatermarkSetting.WATERMARK_SIZE.MEDIUM,
        WatermarkSetting.WATERMARK_SIZE.LARGE
    )

    private val WATERMARK_LOCATION_PRESETS = arrayOf(
        "NORTH-WEST",
        "NORTH-EAST",
        "SOUTH-EAST",
        "SOUTH-WEST",
    )

    private val WATERMARK_LOCATION_PRESETS_MAPPING = arrayOf(
        WatermarkSetting.WATERMARK_LOCATION.NORTH_WEST,
        WatermarkSetting.WATERMARK_LOCATION.NORTH_EAST,
        WatermarkSetting.WATERMARK_LOCATION.SOUTH_EAST,
        WatermarkSetting.WATERMARK_LOCATION.SOUTH_WEST
    )

    private val YUV_FILTER_MODE = arrayOf(
        "NONE",
        "Linear",
        "Bilinear",
        "Box",
    )

    private fun initVideoQualityPanel() {

    }

    private fun initVideoSizePanel() {

    }

    private fun initAudioQualityPanel() {

    }

    private fun initWatermarkPanel() {

    }

    private fun initPicturePanel() {

    }

    private fun initBitrateCtrlPanel() {

    }

    private fun initYuvFilterModePanel() {

    }
}