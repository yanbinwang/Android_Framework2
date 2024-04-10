package com.example.qiniu.plain

import com.qiniu.pili.droid.streaming.AVCodecType
import com.qiniu.pili.droid.streaming.PLVideoEncodeType
import com.qiniu.pili.droid.streaming.StreamingProfile.BitrateAdjustMode
import com.qiniu.pili.droid.streaming.StreamingProfile.H264Profile
import com.qiniu.pili.droid.streaming.StreamingProfile.YuvFilterMode
import com.qiniu.pili.droid.streaming.WatermarkSetting.WATERMARK_LOCATION
import com.qiniu.pili.droid.streaming.WatermarkSetting.WATERMARK_SIZE
import java.io.Serializable

/**
 * 保存所选择的推流配置信息，仅在 demo 上用来保存配置信息
 * 此类为非必须的，您可以根据您的产品定义自行决定配置信息的保存方式
 */
class EncodingConfig : Serializable {
    var mCodecType: AVCodecType? = null
    var mVideoEncodeType: PLVideoEncodeType? = null
    var mIsAudioOnly = false
    var mIsVideoQualityPreset = false
    var mVideoQualityPreset = 0
    var mVideoQualityCustomFPS = 0
    var mVideoQualityCustomBitrate = 0
    var mVideoQualityCustomMaxKeyFrameInterval = 0
    var mVideoQualityCustomProfile: H264Profile? = null
    var mIsVideoSizePreset = false
    var mVideoSizePreset = 0
    var mVideoSizeCustomWidth = 0
    var mVideoSizeCustomHeight = 0
    var mVideoOrientationPortrait = false
    var mVideoRateControlQuality = false
    var mBitrateAdjustMode: BitrateAdjustMode? = null
    var mAdaptiveBitrateMin = -1
    var mAdaptiveBitrateMax = -1
    var mVideoFPSControl = false
    var mIsWatermarkEnabled = false
    var mWatermarkAlpha = 0
    var mWatermarkSize: WATERMARK_SIZE? = null
    var mWatermarkCustomWidth = 0
    var mWatermarkCustomHeight = 0
    var mIsWatermarkLocationPreset = false
    var mWatermarkLocationPreset: WATERMARK_LOCATION? = null
    var mWatermarkLocationCustomX = 0f
    var mWatermarkLocationCustomY = 0f
    var mIsPictureStreamingEnabled = false
    var mPictureStreamingFilePath: String? = null
    var mIsAudioQualityPreset = false
    var mAudioQualityPreset = 0
    var mAudioQualityCustomSampleRate = 0
    var mAudioQualityCustomBitrate = 0
    var mYuvFilterMode: YuvFilterMode? = null
}