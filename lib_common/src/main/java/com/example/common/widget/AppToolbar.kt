package com.example.common.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.utils.function.applyI18nTextStyle
import com.example.common.utils.function.applyTextStyle
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.tintWithMutate
import com.example.common.widget.i18n.I18nTextView
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.applyConstraints
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.center
import com.example.framework.utils.function.view.centerVertically
import com.example.framework.utils.function.view.clearBackground
import com.example.framework.utils.function.view.clearHighlightColor
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.endToEndOf
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToEndOf
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.topToTopOf
import com.example.framework.widget.BaseViewGroup
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 仿系统Toolbar自定义头
 */
class AppToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var activity: WeakReference<FragmentActivity>? = null
    val rootView by lazy { ConstraintLayout(context) }
    val idsMap by lazy { ConcurrentHashMap<String, Int>() }

    companion object {
        // 标题
        const val KEY_TITLE_TEXT = "title_text" // 标题文本
        const val KEY_TITLE_SHADOW = "title_shadow" // 标题阴影线
        // 左侧按钮
        const val KEY_LEFT_ICON = "left_icon" // 左侧图标按钮
        const val KEY_LEFT_TEXT = "left_text" // 左侧文本按钮
        const val KEY_LEFT_CUSTOM_VIEW = "left_custom_view" // 左侧自定义视图（任意 View 类型）
        // 右侧按钮
        const val KEY_RIGHT_ICON = "right_icon" // 右侧图标按钮
        const val KEY_RIGHT_TEXT = "right_text" // 右侧文本按钮
        const val KEY_RIGHT_CUSTOM_VIEW = "right_custom_view" // 右侧自定义视图（任意 View 类型）
        // 标题位置
        const val TITLE_ALIGNMENT_START = 0 // 靠左
        const val TITLE_ALIGNMENT_CENTER = 1 // 居中（默认）
        // 编译时注解：限制标题类型
        @IntDef(TITLE_ALIGNMENT_START, TITLE_ALIGNMENT_CENTER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TitleAlignment
    }

    /**
     * 初始化及添加当前View至容器
     */
    init {
        rootView.size(MATCH_PARENT, WRAP_CONTENT)
        rootView.padding(5.pt, getStatusBarHeight(), 5.pt, 0)
    }

    override fun onInflate() {
        if (shouldInflate) addView(rootView)
    }

    /**
     * 建立页面视图绑定关系
     */
    fun bind(host: Any): AppToolbar {
        activity = WeakReference(when (host) {
            // Activity（兼容所有现代 Activity）
            is FragmentActivity -> host
            // AndroidX Fragment
            is Fragment -> host.requireActivity()
            // 旧系统Fragment
            is android.app.Fragment -> throw RuntimeException("android.app.Fragment is deprecated and not supported!")
            // 不认识的类型
            else -> throw IllegalArgumentException("Unsupported host type: ${host::class.java.name}")
        })
        host.doOnDestroy {
            idsMap.clear()
        }
        return this
    }

    /**
     * 控件移除页面时持有引用清空
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        runCatching { activity?.clear() }.onFailure { e -> e.printStackTrace() }
    }

    /**
     * 关闭引用的页面
     */
    private fun finishHost() {
        activity?.get()?.takeIf { !it.isFinishing }?.finish()
    }

    /**
     * 默认二级页面标题配置
     * @title -> 标题
     * @titleColor -> 标题字体颜色
     * @resId -> 返回按钮样式
     * @tintColor -> 返回按钮覆盖色
     * @bgColor ->背景颜色
     * @hasShade -> 标题底部是否带阴影
     * @showBackButton -> 是否显示左侧返回按钮，默认为 true
     * @titleAlignment -> 默认标题位置
     * @onBack -> 默认左侧返回
     */
    fun setCustomTitle(title: Any? = null, @ColorRes titleColor: Int = R.color.textPrimary, @DrawableRes resId: Int = R.mipmap.ic_btn_back, @ColorRes tintColor: Int = -1, @ColorRes bgColor: Int = R.color.bgToolbar, hasShade: Boolean = false, showBackButton: Boolean = true, @TitleAlignment titleAlignment: Int = TITLE_ALIGNMENT_CENTER, onBack: () -> Unit = { finishHost() }): AppToolbar {
        // 设置返回按钮
        if (showBackButton) setLeftButton(resId, tintColor, onBack = onBack)
        // 设置标题
        if (null != title) {
            createOrUpdateView<I18nTextView>(KEY_TITLE_TEXT, {
                I18nTextView(context).also {
                    it.textSize(R.dimen.textSize16)
                    it.bold(true)
                    it.size(WRAP_CONTENT, 44.pt)
                    it.gravity = when (titleAlignment) {
                        TITLE_ALIGNMENT_START -> Gravity.CENTER_VERTICAL or Gravity.START
                        else -> Gravity.CENTER
                    }
                    it.filters = arrayOf(InputFilter.LengthFilter(10))
                    it.ellipsize = TextUtils.TruncateAt.END
                }
            }, {
                if (titleAlignment == TITLE_ALIGNMENT_START) {
                    if (showBackButton) {
                        findIdByKey(KEY_LEFT_ICON)?.let { leftViewId ->
                            startToEndOf(it, leftViewId)
                        }
                    } else {
                        startToStartOf(it)
                    }
                    centerVertically(it)
                } else {
                    center(it)
                }
            }).apply {
                when (title) {
                    is Int -> applyI18nTextStyle(title, titleColor)
                    is String -> applyTextStyle(title, titleColor)
                }
            }
        }
        // 设置背景色
        rootView.setBackgroundColor(context.color(bgColor))
        // 设置阴影
        if (hasShade) createShade()
        return this
    }

    fun setTitle(title: Any? = null, @ColorRes titleColor: Int = R.color.textPrimary, @ColorRes bgColor: Int = R.color.bgToolbar, hasShade: Boolean = false, @TitleAlignment titleAlignment: Int = TITLE_ALIGNMENT_CENTER, onBack: () -> Unit = { finishHost() }): AppToolbar {
        setCustomTitle(title, titleColor, bgColor = bgColor, hasShade = hasShade, titleAlignment = titleAlignment, onBack = onBack)
        return this
    }

    /**
     * 页面不需要标题，只需要定制的返回按钮及特定背景
     */
    fun setSecondaryTitle(@DrawableRes resId: Int = R.mipmap.ic_btn_back, @ColorRes tintColor: Int = -1, @ColorRes bgColor: Int = R.color.bgToolbar, hasShade: Boolean = false, onBack: () -> Unit = { finishHost() }): AppToolbar {
        setCustomTitle(resId = resId, tintColor = tintColor, bgColor = bgColor, hasShade = hasShade, onBack = onBack)
        return this
    }

    /**
     * 1) 在xml中绘制AppToolbar
     * 2) 页面调用bind()方法传入Activity实现绑定
     */
    fun setTransparent(title: Any? = null, @ColorRes titleColor: Int = R.color.textPrimary, @TitleAlignment titleAlignment: Int = TITLE_ALIGNMENT_CENTER, onBack: () -> Unit = { finishHost() }): AppToolbar {
        return setTitle(title, titleColor, R.color.bgTransparent, titleAlignment = titleAlignment, onBack = onBack)
    }

    fun setSecondaryTransparent(@DrawableRes resId: Int = R.mipmap.ic_btn_back, @ColorRes tintColor: Int = -1, onBack: () -> Unit = { finishHost() }): AppToolbar {
        return setSecondaryTitle(resId, tintColor, R.color.bgTransparent, onBack = onBack)
    }

    /**
     * 设置左/右侧按钮图片资源
     * @resId -> 图片
     * @tintColor -> 图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * @onBack -> 点击事件
     */
    fun setLeftButton(@DrawableRes resId: Int = R.mipmap.ic_btn_back, @ColorRes tintColor: Int = -1, onBack: () -> Unit = { finishHost() }): AppToolbar {
        createImageView(KEY_LEFT_ICON, resId, tintColor, onBack) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 设置左/右侧文字
     * @label -> 文案
     * @labelColor -> 文案颜色
     * @drawable -> 是否包含图片（默认就是左侧的）
     *  1) drawable(res, width, height)获取图片，设置宽高
     *  2) view.setCompoundDrawables(startDrawable, topDrawable, endDrawable, bottomDrawable)调取绘制
     *  3) drawablePadding?.let { view.compoundDrawablePadding = it }文字间距
     * @onBack->点击事件
     */
    fun setLeftText(label: Any, @ColorRes labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onBack: () -> Unit = { finishHost() }): AppToolbar {
        createTextView(KEY_LEFT_TEXT, label, labelColor, drawable, onBack) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 1) 创建视图的函数
     * 2) 配置视图的回调
     * AppToolbar.setLeft({ ImageView(mActivity) }) { img ->
     *     img.setImageResource(R.drawable.ic_back)
     *     img.setOnClickListener { mActivity.finish() }
     *     img.setPadding(10.pt, 10.pt, 10.pt, 10.pt)
     * }
     */
    inline fun <reified T : View> setLeft(crossinline creator: () -> T, block: (T) -> Unit = {}): AppToolbar {
        //margin属性是插入后才可以设置的
        createOrUpdateView(KEY_LEFT_CUSTOM_VIEW, creator) {
            startToStartOf(it)
            centerVertically(it)
        }.also(block)
        return this
    }

    fun setRightButton(@DrawableRes resId: Int = R.mipmap.ic_btn_back, @ColorRes tintColor: Int = -1, onBack: () -> Unit = {}): AppToolbar {
        createImageView(KEY_RIGHT_ICON, resId, tintColor, onBack) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    fun setRightText(label: Any, @ColorRes labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onBack: () -> Unit = {}): AppToolbar {
        createTextView(KEY_RIGHT_TEXT, label, labelColor, drawable, onBack) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    inline fun <reified T : View> setRight(crossinline creator: () -> T, block: (T) -> Unit = {}): AppToolbar {
        createOrUpdateView(KEY_RIGHT_CUSTOM_VIEW, creator) {
            endToEndOf(it)
            centerVertically(it)
        }.also(block)
        return this
    }

    /**
     * 外层创建view
     */
    inline fun <reified T : View> createOrUpdateView(key: String, crossinline creator: () -> T, noinline block: ConstraintSet.(Int) -> Unit = {}): T {
        // 移除上一次的视图
        val lastId = idsMap[key]
        if (lastId != null && lastId != NO_ID) {
            rootView.findViewById<T>(lastId)?.let {
                rootView.removeView(it)
            }
        }
        // 生成新的唯一 id
        val newViewId = generateViewId()
        idsMap[key] = newViewId
        val newView = creator.invoke()
        newView.id = newViewId
        rootView.addView(newView)
        rootView.applyConstraints {
            block(newViewId)
        }
        return newView
    }

    /**
     * 获取某个特定的view
     */
    inline fun <reified T : View> findViewByKey(key: String): T? {
        val id = idsMap[key]
        return if (id != null && id != NO_ID) {
            rootView.findViewById(id)
        } else {
            null
        }
    }

    /**
     * 获取某个特定的view的id
     */
    fun findIdByKey(key: String): Int? {
        return idsMap[key]
    }

    /**
     * 检测是否创建
     */
    fun nonNull(vararg keys: String): Boolean {
        return keys.all { idsMap[it] != null }
    }

    /**
     * 底部是否需要阴影
     */
    private fun createShade() {
        createOrUpdateView<View>(KEY_TITLE_SHADOW, {
            View(context).also {
                it.background(R.color.bgLine)
                it.size(MATCH_PARENT, 1.pt)
            }
        }, {
            startToStartOf(it)
            endToEndOf(it)
            topToTopOf(it)
        }).margin(top = 44.pt)
    }

    private fun createTextView(key: String, label: Any, @ColorRes labelColor: Int, drawable: Drawable? = null, onBack: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        createOrUpdateView<I18nTextView>(key, {
            I18nTextView(context).also {
                when (label) {
                    is Int -> it.applyI18nTextStyle(label, labelColor)
                    is String -> it.applyTextStyle(label, labelColor)
                }
                it.padding(start = 15.pt, end = 15.pt)
                it.textSize(R.dimen.textSize14)
                it.gravity = Gravity.CENTER
                if (drawable != null) {
                    it.clearBackground()
                    it.clearHighlightColor()
                    drawable.tintWithMutate(color(labelColor))
                    it.setCompoundDrawables(drawable, null, null, null)
                    it.compoundDrawablePadding = 2.pt
                }
                it.click {
                    onBack.invoke()
                }
            }
        }, block)
    }

    /**
     * 创建左右侧按钮方法
     */
    private fun createImageView(key: String, @DrawableRes resId: Int, @ColorRes tintColor: Int, onBack: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        createOrUpdateView<ImageView>(key, {
            ImageView(context).also {
                it.setResource(resId)
                if (tintColor != -1) it.tint(tintColor)
                it.size(44.pt, 44.pt)
                it.padding(10.pt, 10.pt, 10.pt, 10.pt)
                it.click {
                    onBack.invoke()
                }
            }
        }, block)
    }

}