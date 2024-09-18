package com.example.live.config

import com.qiniu.pili.droid.streaming.AVCodecType
import com.qiniu.pili.droid.streaming.PLVideoEncodeType
import com.qiniu.pili.droid.streaming.StreamingProfile.BitrateAdjustMode
import com.qiniu.pili.droid.streaming.StreamingProfile.H264Profile
import com.qiniu.pili.droid.streaming.StreamingProfile.YuvFilterMode
import com.qiniu.pili.droid.streaming.WatermarkSetting.WATERMARK_LOCATION
import com.qiniu.pili.droid.streaming.WatermarkSetting.WATERMARK_SIZE
import java.io.Serializable

class EncodingConfig : Serializable {
    var mCodecType: AVCodecType? = null
    var mVideoEncodeType: PLVideoEncodeType? = null
    var mIsAudioOnly: Boolean? = null
    var mIsVideoQualityPreset: Boolean? = null
    var mVideoQualityPreset: Int? = null
    var mVideoQualityCustomFPS: Int? = null
    var mVideoQualityCustomBitrate: Int? = null
    var mVideoQualityCustomMaxKeyFrameInterval: Int? = null
    var mVideoQualityCustomProfile: H264Profile? = null
    var mIsVideoSizePreset: Boolean? = null
    var mVideoSizePreset: Int? = null
    var mVideoSizeCustomWidth: Int? = null
    var mVideoSizeCustomHeight: Int? = null
    var mVideoOrientationPortrait: Boolean? = null
    var mVideoRateControlQuality: Boolean? = null
    var mBitrateAdjustMode: BitrateAdjustMode? = null
    var mAdaptiveBitrateMin = -1
    var mAdaptiveBitrateMax = -1
    var mVideoFPSControl: Boolean? = null
    var mIsWatermarkEnabled: Boolean? = null
    var mWatermarkAlpha: Int? = null
    var mWatermarkSize: WATERMARK_SIZE? = null
    var mWatermarkCustomWidth: Int? = null
    var mWatermarkCustomHeight: Int? = null
    var mIsWatermarkLocationPreset: Boolean? = null
    var mWatermarkLocationPreset: WATERMARK_LOCATION? = null
    var mWatermarkLocationCustomX: Float? = null
    var mWatermarkLocationCustomY: Float? = null
    var mIsPictureStreamingEnabled: Boolean? = null
    var mPictureStreamingFilePath: String? = null
    var mIsAudioQualityPreset: Boolean? = null
    var mAudioQualityPreset: Int? = null
    var mAudioQualityCustomSampleRate: Int? = null
    var mAudioQualityCustomBitrate: Int? = null
    var mYuvFilterMode: YuvFilterMode? = null
}