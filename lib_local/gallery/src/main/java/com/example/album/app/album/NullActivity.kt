package com.example.album.app.album

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.Visibility
import android.view.View
import android.view.ViewTreeObserver
import android.window.OnBackInvokedDispatcher
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.intentString
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentBoolean
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentLong
import com.example.framework.utils.function.intentParcelable
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.example.album.Album
import com.example.album.app.Contract
import com.example.album.callback.Action
import com.example.album.model.Widget

/**
 * 空页面
 * 功能：当手机里没有图片/视频时显示 提供拍照、录像入口
 */
internal class NullActivity : BaseActivity(), Contract.NullPresenter {
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
    // MVP & UI
    private val mView by lazy { NullView(this, this) }

    /**
     * 相机拍摄完成的回调：返回路径
     */
    private val mCameraAction = object : Action<String> {
        override fun onAction(result: String) {
            val intent = Intent()
            intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    companion object {
        // 拍照/录像返回的路径 Key
        private const val KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH"

        /**
         * 外部解析返回路径
         */
        @JvmStatic
        fun parsePath(intent: Intent?): String {
            return intent.intentString(KEY_OUTPUT_IMAGE_PATH)
        }
    }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasExtras()) return finish()
        // 覆盖基类动画
        setActivityAnimations()
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
        // 禁止侧滑拖动动画
        val decorView = window.decorView
        decorView.getViewTreeObserver().addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                decorView.getViewTreeObserver().removeOnPreDrawListener(this)
                window.decorView.isScrollContainer = false
                window.decorView.setOverScrollMode(View.OVER_SCROLL_NEVER)
                // Android 13+ 预测性侧滑返回 → 直接关闭，不拖动页面
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val dispatcher = window.onBackInvokedDispatcher
                    dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) { finish() }
                }
                // Android 10+ 手势排除
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        window.systemGestureExclusionRects = mutableListOf<Rect>()
                    } catch (_: Exception) {
                    }
                }
                // 允许继续绘制
                return true
            }
        })
        setContentView(R.layout.album_activity_null)
        // 初始化状态栏/导航栏颜色（黑白字体自适应）
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.statusBarColor)
        val navigationBarBattery = shouldUseWhiteSystemBarsForRes(mWidget.navigationBarColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mWidget.navigationBarColor)
        // 绑定 MVP
        mView.setupViews(mWidget)
        // 根据当前功能类型，显示不同提示文案
        when (mFunction) {
            Album.FUNCTION_CHOICE_IMAGE -> {
                mView.setMessage(R.string.album_not_found_image)
                mView.setMakeVideoDisplay(false)
            }
            Album.FUNCTION_CHOICE_VIDEO -> {
                mView.setMessage(R.string.album_not_found_video)
                mView.setMakeImageDisplay(false)
            }
            Album.FUNCTION_CHOICE_ALBUM -> {
                mView.setMessage(R.string.album_not_found_album)
            }
            else -> {
                throw AssertionError("This should not be the case.")
            }
        }
        // 如果不允许使用相机，隐藏两个按钮
        if (!mHasCamera) {
            mView.setMakeImageDisplay(false)
            mView.setMakeVideoDisplay(false)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setActivityAnimations()
    }

    private fun setActivityAnimations() {
        val fadeEnter = Fade(Visibility.MODE_IN)
        fadeEnter.setDuration(300)
        window.setExitTransition(fadeEnter)
        val fadeExit = Fade(Visibility.MODE_OUT)
        fadeEnter.setDuration(300)
        window.setReturnTransition(fadeExit)
    }

    /**
     * 点击拍照
     */
    override fun takePicture() {
        Album.camera(this)
            .image()
            .onResult(mCameraAction)
            .start()
    }

    /**
     * 点击录像
     */
    override fun takeVideo() {
        Album.camera(this)
            .video()
            .quality(mQuality)
            .limitDuration(mLimitDuration)
            .limitBytes(mLimitBytes)
            .onResult(mCameraAction)
            .start()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_none)
    }

}