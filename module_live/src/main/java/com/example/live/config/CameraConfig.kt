package com.example.live.config

import com.qiniu.pili.droid.streaming.CameraStreamingSetting.PREVIEW_SIZE_LEVEL
import com.qiniu.pili.droid.streaming.CameraStreamingSetting.PREVIEW_SIZE_RATIO
import java.io.Serializable

/**
 * 保存所选择的相机配置信息，仅在 demo 上用来保存配置信息
 * 此类为非必须的，您可以根据您的产品定义自行决定配置信息的保存方式
 */
class CameraConfig : Serializable {
    var mFrontFacing: Boolean? = null
    var mSizeLevel: PREVIEW_SIZE_LEVEL? = null
    var mSizeRatio: PREVIEW_SIZE_RATIO? = null
    var mFocusMode: String? = null
    var mIsFaceBeautyEnabled: Boolean? = null
    var mIsCustomFaceBeauty: Boolean? = null
    var mContinuousAutoFocus: Boolean? = null
    var mPreviewMirror: Boolean? = null
    var mEncodingMirror: Boolean? = null
}