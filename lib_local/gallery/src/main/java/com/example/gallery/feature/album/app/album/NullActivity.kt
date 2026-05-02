package com.example.gallery.feature.album.app.album

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.common.base.BaseTitleActivity
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.intentString
import com.example.framework.utils.function.intentBoolean
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.example.gallery.databinding.AlbumActivityNullBinding
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.bean.Widget
import com.example.gallery.feature.album.utils.helper.NullHelper

/**
 * 空页面
 * 功能：当手机里没有图片/视频时显示 提供拍照、录像入口
 */
class NullActivity : BaseTitleActivity<AlbumActivityNullBinding>(), View.OnClickListener {
    // 功能：图片/视频/全部
    private val mFunction by lazy { intentInt(Album.KEY_INPUT_FUNCTION) }
    // 视频质量
    private val mQuality by lazy { intentInt(Album.KEY_INPUT_CAMERA_QUALITY, 1) }
    // 视频最大时长
    private val mLimitDuration by lazy { intentLong(Album.KEY_INPUT_CAMERA_DURATION) }
    // 视频最大大小
    private val mLimitBytes by lazy { intentLong(Album.KEY_INPUT_CAMERA_BYTES) }
    // 是否显示拍照按钮
    private val mHasCamera by lazy { intentBoolean(Album.KEY_INPUT_ALLOW_CAMERA) }
    // 主题样式
    private val mWidget by lazy { intentParcelable<Widget>(Album.KEY_INPUT_WIDGET) ?: Widget.getDefaultWidget(this) }
    // UI帮助类
    private val mHelper by lazy { NullHelper(this, mBinding) }

    companion object {
        // 拍照/录像返回的路径 Key
        const val KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH"

        /**
         * 外部解析返回路径
         */
        @JvmStatic
        fun parsePath(intent: Intent?): String {
            return intent.intentString(KEY_OUTPUT_IMAGE_PATH)
        }
    }

    override fun isImmersionBarEnabled() = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mHelper.setActivityAnimations()
    }

    override fun initBefore() {
        super.initBefore()
        mHelper.initBefore()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 初始化状态栏/导航栏颜色（黑白字体自适应）
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.statusBarColor)
        val navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.navigationBarColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.navigationBarColor)
        // 浅色 / 深色 主题切换
        if (mWidget.uiStyle == Widget.STYLE_LIGHT) {
            titleRoot
                .setTitle(mWidget.title)
        } else {
            titleRoot
                .setTitle(mWidget.title, R.color.textWhite, R.color.bgBlack)
                .setLeftButton(tintColor = R.color.bgBlack)
        }
        // 设置基础UI
        mHelper.setViews(mWidget, mFunction, mHasCamera)
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.llImage, mBinding?.llVideo)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 点击拍照
            R.id.ll_image -> mHelper.takePicture()
            // 点击录像
            R.id.ll_video -> mHelper.takeVideo(mQuality, mLimitDuration, mLimitBytes)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

}