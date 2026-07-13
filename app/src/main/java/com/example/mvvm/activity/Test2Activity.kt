package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.common.R
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.function.applyTextStyle
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.applyConstraints
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.bottomToBottomOf
import com.example.framework.utils.function.view.centerVertically
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToEndOf
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.topToTopOf
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityTest2Binding
import com.google.android.material.appbar.AppBarLayout
import com.therouter.router.Route
import kotlin.math.abs

@Route(path = RouterPath.TestActivity2)
class Test2Activity : BaseActivity<ActivityTest2Binding>() {
    private val rootView by lazy { ConstraintLayout(this) }
    private val ivAvatarId by lazy { View.generateViewId() }
    private val tvNickId by lazy { View.generateViewId() }
    // 折叠后的小头像
    private val ivAvatar by lazy {
        ImageView(this).apply {
            id = ivAvatarId
            setResource(R.drawable.shape_glide_circular)
            size(44.pt, 44.pt)
            padding(10.pt, 10.pt, 10.pt, 10.pt)
            gone()
        }
    }
    // 折叠后的标题
    private val tvNick by lazy {
        TextView(this).apply {
            id = tvNickId
            applyTextStyle("老王", R.color.textPrimary)
            textSize(R.dimen.textSize12)
            bold(true)
            size(WRAP_CONTENT, WRAP_CONTENT)
            gone()
        }
    }

    companion object {
        private const val MIN_SCALE = 0.3f
        private const val MAX_SCALE = 1.0f
        private const val TRANSLATE_X_FACTOR = 0.09f  // 水平终点系数
        private const val TRANSLATE_Y_FACTOR = 0.03f  // 垂直终点系数
    }

    override fun isImmersionBarEnabled() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 初始化顶部菜单
        mBinding?.toolbar.size(height = 44.pt + getStatusBarHeight())
        mBinding?.toolbar?.clearToolbarCompletely()
        rootView.size(MATCH_PARENT, MATCH_PARENT)
        rootView.padding(top = getStatusBarHeight())
        mBinding?.toolbar?.addView(rootView)
        rootView.addView(ivAvatar)
        rootView.applyConstraints {
            startToStartOf(ivAvatarId)
            centerVertically(ivAvatarId)
        }
        rootView.addView(tvNick)
        rootView.applyConstraints {
            startToEndOf(tvNickId, ivAvatarId)
            topToTopOf(tvNickId, ivAvatarId)
            bottomToBottomOf(tvNickId, ivAvatarId)
        }
        ivAvatar.margin(start = 5.pt)
    }

    override fun initEvent() {
        super.initEvent()
        /**
         * setScrimVisibleHeightTrigger(int height) 是 CollapsingToolbarLayout 提供的一个关键 API，用来精确控制 内容遮罩（Content Scrim）何时开始显示/隐藏
         * 当背景图被折叠到看不见时，提供一个纯色背景，保证 Toolbar 上的文字/图标依然清晰可读
         */
        mBinding?.collapsingToolbar?.scrimVisibleHeightTrigger = getStatusBarHeight() + 44.pt
        // 设置监听
        mBinding?.appbar?.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            private var isCollapsed = false  // true=已折叠, false=已展开

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                val totalRange = appBarLayout?.totalScrollRange ?: return
                val offset = abs(verticalOffset).toSafeFloat()
                val percentage = (offset / totalRange).coerceIn(0f, 1f)
                // ========== 缩放 + 平移（连续变化，每帧都执行）==========
                val scaleOffset = (1 - percentage).coerceIn(MIN_SCALE, MAX_SCALE)
                mBinding?.llInfo?.apply {
                    scaleX = scaleOffset
                    scaleY = scaleOffset
                    // 将缩放比例映射为平移进度
                    // 当 scale=1.0 时，progress=0，translationX=0
                    // 当 scale=minScale(0.3) 时，progress=1.0，translationX 精确到达你原来调好的终点
                    val translateProgress = ((MAX_SCALE - scaleOffset) / (MAX_SCALE - MIN_SCALE)).coerceIn(0f, 1f)
                    translationX = -(translateProgress * screenWidth * TRANSLATE_X_FACTOR)
                    translationY = percentage * screenHeight * TRANSLATE_Y_FACTOR
                }
                // ========== 大头像(llInfo)的显隐（只在临界点切换一次）==========
                val currentlyCollapsed = abs(verticalOffset) < totalRange
                if (currentlyCollapsed != isCollapsed) {
                    isCollapsed = currentlyCollapsed
                    if (isCollapsed) {
                        "执行展开".logWTF("wyb")
                        initImmersionBar(false)
                        // 展开 → 大头像淡入
                        mBinding?.llInfo.appear(100)
                        ivAvatar.invisible()
                        tvNick.invisible()
                        mBinding?.toolbar.background(R.color.bgTransparent)
                        mBinding?.toolbar.fade()
                    } else {
                        "执行折叠".logWTF("wyb")
                        mBinding?.llInfo.fade(100)
                        ivAvatar.visible()
                        tvNick.visible()
                        mBinding?.toolbar.background(R.color.bgDefault)
                        mBinding?.toolbar.appear()
                        initImmersionBar(true)
                    }
                }
            }
        })
    }

}

fun Toolbar.clearToolbarCompletely() {
    removeAllViews()
    setLogo(null)
    setTitle(null)
    getMenu().clear()
    // 如果有导航图标，也移除
    navigationIcon = null
    // 某些情况下可能需要强制设置padding
    setPadding(0, 0, 0, 0)
}