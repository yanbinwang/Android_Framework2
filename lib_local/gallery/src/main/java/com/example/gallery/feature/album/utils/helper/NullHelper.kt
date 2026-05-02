package com.example.gallery.feature.album.utils.helper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.transition.Fade
import android.transition.Visibility
import android.view.View
import android.view.ViewTreeObserver
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor
import com.example.common.utils.function.color
import com.example.common.utils.function.ptFloat
import com.example.common.utils.function.string
import com.example.common.utils.function.tintWithMutate
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.getHighLightColor
import com.example.framework.utils.function.value.getNormalColor
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.selectorRoundBackground
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible
import com.example.gallery.R
import com.example.gallery.databinding.AlbumActivityNullBinding
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.api.callback.Action
import com.example.gallery.feature.album.app.album.NullActivity.Companion.KEY_OUTPUT_IMAGE_PATH
import com.example.gallery.feature.album.bean.Widget

/**
 * 空页面UI管控
 */
class NullHelper(private val mActivity: AppCompatActivity, private val mBinding: AlbumActivityNullBinding?) {
    /**
     * 相机拍摄完成的回调：返回路径
     */
    private val mCameraAction = object : Action<String> {
        override fun onAction(result: String) {
            val intent = Intent()
            intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result)
            mActivity.setResult(RESULT_OK, intent)
            mActivity.finish()
        }
    }

    init {
        mActivity.doOnDestroy {
            mBinding?.unbind()
        }
    }

    /**
     * 页面设置UI之前
     */
    fun initBefore() {
        // 覆盖基类动画
        setActivityAnimations()
        mActivity.overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
        // 禁止侧滑拖动动画
        val decorView = mActivity.window.decorView
        decorView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                decorView.viewTreeObserver.removeOnPreDrawListener(this)
                mActivity.window.decorView.isScrollContainer = false
                mActivity.window.decorView.overScrollMode = View.OVER_SCROLL_NEVER
                // Android 13+ 预测性侧滑返回 → 直接关闭，不拖动页面
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val dispatcher = mActivity.window.onBackInvokedDispatcher
                    dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) { mActivity.finish() }
                }
                // Android 10+ 手势排除
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        mActivity.window.systemGestureExclusionRects = mutableListOf<Rect>()
                    } catch (_: Exception) {
                    }
                }
                // 允许继续绘制
                return true
            }
        })
    }

    /**
     * 设置页面动画
     */
    fun setActivityAnimations() {
        val fadeEnter = Fade(Visibility.MODE_IN)
        fadeEnter.duration = 300
        mActivity.window.exitTransition = fadeEnter
        val fadeExit = Fade(Visibility.MODE_OUT)
        fadeEnter.duration = 300
        mActivity.window.returnTransition = fadeExit
    }

    /**
     * 设置UI
     */
    fun setViews(mWidget: Widget, mFunction: Int, mHasCamera: Boolean) {
        // 设置按钮样式
        val buttonSelector = mWidget.buttonSelector
        val normalColor = buttonSelector.getNormalColor()
        val pressedColor = buttonSelector.getHighLightColor()
        mBinding?.llImage.selectorRoundBackground(normalColor, pressedColor, normalColor, 2.ptFloat)
        mBinding?.llVideo.selectorRoundBackground(normalColor, pressedColor, normalColor, 2.ptFloat)
        // 获取按钮主题色 , 如果需要深色主题则提取出绘制的图标并渲染成深色 -> 此处拿取的是normal
        if (!shouldUseWhiteSystemBarsForColor(buttonSelector.defaultColor)) {
            // 拍照片
            val takeImageIcon = mBinding?.tvImage?.compoundDrawablesRelative[0].also { it.tintWithMutate(color(R.color.bgBlack)) }
            mBinding?.tvImage?.setCompoundDrawables(takeImageIcon, null, null, null)
            mBinding?.tvImage.textColor(R.color.textPrimary)
            // 录视频
            val takeVideoIcon = mBinding?.tvVideo?.compoundDrawablesRelative[0].also { it.tintWithMutate(color(R.color.bgBlack)) }
            mBinding?.tvVideo?.setCompoundDrawables(takeVideoIcon, null, null, null)
            mBinding?.tvVideo.textColor(R.color.textPrimary)
        } else {
            mBinding?.tvImage.textColor(R.color.textWhite)
            mBinding?.tvVideo.textColor(R.color.textWhite)
        }
        // 根据当前功能类型，显示不同提示文案
        when (mFunction) {
            Album.FUNCTION_CHOICE_IMAGE -> {
                mBinding?.tvMessage?.text = string(R.string.album_not_found_image)
                mBinding?.llImage.visible()
                mBinding?.llVideo.gone()
            }
            Album.FUNCTION_CHOICE_VIDEO -> {
                mBinding?.tvMessage?.text = string(R.string.album_not_found_video)
                mBinding?.llImage.gone()
                mBinding?.llVideo.visible()
            }
            Album.FUNCTION_CHOICE_ALBUM -> {
                mBinding?.tvMessage?.text = string(R.string.album_not_found_album)
                mBinding?.llImage.visible()
                mBinding?.llVideo.visible()
            }
        }
        // 如果不允许使用相机，隐藏两个按钮
        if (!mHasCamera) {
            mBinding?.llImage.gone()
            mBinding?.llVideo.gone()
        }
    }

    /**
     * 拍照
     */
    fun takePicture() {
        Album.camera(this)
            .image()
            .onResult(mCameraAction)
            .start()
    }

    /**
     * 录像
     */
    fun takeVideo(mQuality: Int, mLimitDuration: Long, mLimitBytes: Long) {
        Album.camera(this)
            .video()
            .quality(mQuality)
            .limitDuration(mLimitDuration)
            .limitBytes(mLimitBytes)
            .onResult(mCameraAction)
            .start()
    }

}