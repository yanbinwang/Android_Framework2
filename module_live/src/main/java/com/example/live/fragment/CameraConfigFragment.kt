package com.example.live.fragment

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.framework.utils.function.value.orZero
import com.example.live.config.CameraConfig
import com.example.live.config.Config
import com.example.live.databinding.FragmentCameraConfigBinding
import com.qiniu.pili.droid.streaming.CameraStreamingSetting.PREVIEW_SIZE_LEVEL
import com.qiniu.pili.droid.streaming.CameraStreamingSetting.PREVIEW_SIZE_RATIO

/**
 * 相机采集配置项 Fragment，仅用作 demo 中获取配置信息，后续在推流初始化时传递给 SDK 内部使用
 * 此 Fragment 为非必须的，您可以根据您的产品定义自行决定配置信息的配置方式
 */
class CameraConfigFragment : ConfigFragment<FragmentCameraConfigBinding>() {
    private val PREVIEW_SIZE_LEVEL_PRESETS = arrayOf("SMALL", "MEDIUM", "LARGE")
    private val PREVIEW_SIZE_LEVEL_PRESETS_MAPPING = arrayOf(PREVIEW_SIZE_LEVEL.SMALL, PREVIEW_SIZE_LEVEL.MEDIUM, PREVIEW_SIZE_LEVEL.LARGE)
    private val PREVIEW_SIZE_RATIO_PRESETS = arrayOf("4:3", "16:9")
    private val PREVIEW_SIZE_RATIO_PRESETS_MAPPING = arrayOf(PREVIEW_SIZE_RATIO.RATIO_4_3, PREVIEW_SIZE_RATIO.RATIO_16_9)
    private val FOCUS_MODE_PRESETS = arrayOf("AUTO", "CONTINUOUS PICTURE", "CONTINUOUS VIDEO")
    private val FOCUS_MODE_PRESETS_MAPPING = arrayOf(Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPreviewSizeLevelSpinner()
        initPreviewSizeRatioSpinner()
        initFocusModeSpinner()
    }

    private fun initPreviewSizeLevelSpinner() {
        val data = ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, PREVIEW_SIZE_LEVEL_PRESETS)
        mBinding?.previewSizeLevelSpinner?.adapter = data
        mBinding?.previewSizeLevelSpinner?.setSelection(1)
    }

    private fun initPreviewSizeRatioSpinner() {
        val data = ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, PREVIEW_SIZE_RATIO_PRESETS)
        mBinding?.previewSizeRatioSpinner?.adapter = data
        mBinding?.previewSizeRatioSpinner?.setSelection(1)
    }

    private fun initFocusModeSpinner() {
        val data = ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, FOCUS_MODE_PRESETS)
        mBinding?.focusModeSpinner?.adapter = data
        mBinding?.focusModeSpinner?.setSelection(1)
    }

    override fun getIntent(): Intent {
        val data = Intent()
        data.putExtra(Config.NAME_CAMERA_CONFIG, buildCameraConfig())
        return data
    }

    private fun buildCameraConfig(): CameraConfig {
        val cameraConfig = CameraConfig()
        cameraConfig.mFrontFacing = mBinding?.facingFront?.isChecked
        cameraConfig.mSizeLevel = PREVIEW_SIZE_LEVEL_PRESETS_MAPPING[mBinding?.previewSizeLevelSpinner?.selectedItemPosition.orZero]
        cameraConfig.mSizeRatio = PREVIEW_SIZE_RATIO_PRESETS_MAPPING[mBinding?.previewSizeRatioSpinner?.selectedItemPosition.orZero]
        cameraConfig.mFocusMode = FOCUS_MODE_PRESETS_MAPPING[mBinding?.focusModeSpinner?.selectedItemPosition.orZero]
        cameraConfig.mIsFaceBeautyEnabled = mBinding?.faceBeauty?.isChecked
        cameraConfig.mIsCustomFaceBeauty = mBinding?.externalFaceBeauty?.isChecked
        cameraConfig.mContinuousAutoFocus = mBinding?.continuousAutoFocus?.isChecked
        cameraConfig.mPreviewMirror = mBinding?.previewMirror?.isChecked
        cameraConfig.mEncodingMirror = mBinding?.encodingMirror?.isChecked
        return cameraConfig
    }

}