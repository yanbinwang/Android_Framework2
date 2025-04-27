package com.example.common.utils.builder

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.setI18nTheme
import com.example.common.utils.function.setTheme
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
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToEndOf
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.topToTopOf
import java.util.concurrent.ConcurrentHashMap

/**
 * 顶部标题默认不具备任何颜色和显示的按钮
 * 格式->左右侧图片/文本，中间大标题，左右侧间距默认文本大小都应固定，图片可定制
 */
//class TitleBuilder(private val mActivity: AppCompatActivity, private val mBinding: ViewTitleBarBinding?) {
//    /**
//     * 实际项目组右侧按钮可能是刷新，需要延后显示
//     */
//    val ivRight get() = mBinding?.ivRight
//
//    init {
//        mActivity.doOnDestroy { mBinding?.unbind() }
//        mBinding?.clRoot.padding(top = getStatusBarHeight())
//    }
//
//    /**
//     * 默认二级页面标题配置
//     * resTitle->标题res路径
//     * titleColor->标题颜色
//     * bgColor->背景颜色
//     * isShade->标题底部是否带阴影
//     */
//    fun setTitle(resTitle: Int = -1, titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
//        mBinding?.tvTitle?.setI18nTheme(resTitle, titleColor)
//        return setRoot(bgColor, isShade)
//    }
//
//    fun setTitle(title: String, titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
//        mBinding?.tvTitle?.setTheme(title, titleColor)
//        return setRoot(bgColor, isShade)
//    }
//
//    private fun setRoot(bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
//        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
//        mBinding?.viewShade?.apply { if (isShade) visible() else gone() }
//        setLeft()
//        return this
//    }
//
//    /**
//     * 部分页面不需要标题，只需要一个定制的返回按钮和特定背景，故而使用此方法
//     */
//    fun setTitleSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }, bgColor: Int = R.color.bgToolbar): TitleBuilder {
//        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
//        setLeft(resId, tintColor, onClick = onClick)
//        return this
//    }
//
//    /**
//     * 1.继承BaseActivity，在xml中include对应标题布局
//     * 2.把布局bind传入工具类，实现绑定后，调取对应方法（private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }）
//     */
//    fun setTransparent(resTitle: Int = -1, titleColor: Int = R.color.textPrimary): TitleBuilder {
//        return setTitle(resTitle, titleColor, 0)
//    }
//
//    fun setTransparent(title: String, titleColor: Int = R.color.textPrimary): TitleBuilder {
//        return setTitle(title, titleColor, 0)
//    }
//
//    fun setTransparentSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
//        return setTitleSecondary(resId, tintColor, onClick, 0)
//    }
//
//    /**
//     * 设置左/右侧按钮图片资源
//     * resId->图片
//     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
//     * onClick->点击事件
//     */
//    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int? = null, height: Int? = null, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
//        mBinding?.ivLeft?.apply {
//            visible()
//            setResource(resId)
//            if (0 != tintColor) tint(tintColor)
//            if (null != width && null != height) size(width, height)
//            click { onClick.invoke() }
//        }
//        mBinding?.tvLeft.gone()
//        return this
//    }
//
//    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int? = null, height: Int? = null, onClick: () -> Unit = {}): TitleBuilder {
//        mBinding?.ivRight?.apply {
//            visible()
//            setResource(resId)
//            if (0 != tintColor) tint(tintColor)
//            if (null != width && null != height) size(width, height)
//            click { onClick.invoke() }
//        }
//        mBinding?.tvRight.gone()
//        return this
//    }
//
//    /**
//     * 设置左/右侧文字
//     * resLabel->文案res
//     * labelColor->文案颜色
//     * onClick->点击事件
//     */
//    fun setI18nLeft(resLabel: Int, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
//        mBinding?.tvLeft?.apply {
//            visible()
//            setI18nTheme(resLabel, labelColor)
//            click { onClick.invoke() }
//        }
//        mBinding?.ivLeft.gone()
//        return this
//    }
//
//    fun setI18nRight(resLabel: Int, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = {}): TitleBuilder {
//        mBinding?.tvRight?.apply {
//            visible()
//            setI18nTheme(resLabel, labelColor)
//            click { onClick.invoke() }
//        }
//        mBinding?.ivRight.gone()
//        return this
//    }
//
//    /**
//     * 整体隐藏
//     */
//    fun hideTitle() {
//        mBinding?.clRoot.gone()
//    }
//
//}
class TitleBuilder(private val mActivity: AppCompatActivity, val mBinding: ViewTitleBarBinding?) {
    val idsMap by lazy { ConcurrentHashMap<String, Int>() }

    init {
        mActivity.doOnDestroy {
            idsMap.clear()
            mBinding?.unbind()
        }
        mBinding?.clRoot.padding(5.pt, getStatusBarHeight(), 5.pt, 0)
    }

    companion object {
        // 标题
        const val TITLE_TEXT = "title_text"      // 标题文本
        const val TITLE_SHADOW = "title_shadow"  // 标题阴影线
        // 左侧按钮
        const val LEFT_ICON = "left_icon"        // 左侧图标按钮
        const val LEFT_TEXT = "left_text"        // 左侧文本按钮
        const val LEFT_CUSTOM_VIEW = "left_custom_view" // 左侧自定义视图（任意 View 类型）
        // 右侧按钮
        const val RIGHT_ICON = "right_icon"      // 右侧图标按钮
        const val RIGHT_TEXT = "right_text"      // 右侧文本按钮
        const val RIGHT_CUSTOM_VIEW = "right_custom_view" // 右侧自定义视图（任意 View 类型）
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * hasShade->标题底部是否带阴影
     */
    fun setTitle(title: Any? = null, titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, hasShade: Boolean = false): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
        if (null != title) {
            handleView<I18nTextView>(TITLE_TEXT, {
                I18nTextView(mActivity).also {
                    it.textSize(R.dimen.textSize18)
                    it.bold(true)
                    it.size(WRAP_CONTENT, 44.pt)
                    it.gravity = Gravity.CENTER
                    it.filters = arrayOf(InputFilter.LengthFilter(10))
                    it.ellipsize = TextUtils.TruncateAt.END
                }
            }, {
                center(it)
            }).apply {
                when (title) {
                    is Int -> setI18nTheme(title, titleColor)
                    is String -> setTheme(title, titleColor)
                }
            }
        }
        if (hasShade) {
            handleView<View>(TITLE_SHADOW, {
                View(mActivity).also {
                    it.background(R.color.bgLine)
                    it.size(MATCH_PARENT, 1.pt)
                }
            }, {
                startToEndOf(it)
                endToEndOf(it)
                topToTopOf(it)
            }).margin(top = 44.pt)
        }
        setLeft()
        return this
    }

    /**
     * 页面不需要标题，只需要定制的返回按钮及特定背景
     */
    fun setTitleSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }, bgColor: Int = R.color.bgToolbar): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
        setLeft(resId, tintColor, onClick = onClick)
        return this
    }

    /**
     * 1.继承BaseActivity，在xml中include对应标题布局
     * 2.把布局bind传入工具类，实现绑定后，调取对应方法（private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }）
     */
    fun setTransparent(title: Any? = null, titleColor: Int = R.color.textPrimary): TitleBuilder {
        return setTitle(title, titleColor, 0)
    }

    fun setTransparentSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        return setTitleSecondary(resId, tintColor, onClick, 0)
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * width/height->本身宽高
     * onClick->点击事件
     */
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        createImageView(LEFT_ICON, resId, tintColor, onClick) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 设置左/右侧文字
     * label->文案
     * labelColor->文案颜色
     * drawable->是否包含图片（默认就是左侧的）
     *  1.drawable(res, width, height)获取图片，设置宽高
     *  3.view.setCompoundDrawables(startDrawable, topDrawable, endDrawable, bottomDrawable)调取绘制
     *  4.drawablePadding?.let { view.compoundDrawablePadding = it }文字间距
     * onClick->点击事件
     */
    fun setLeft(label: Any, labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        createTextView(LEFT_TEXT, label, labelColor, drawable, onClick) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 1.创建视图的函数
     * 2.配置视图的回调
     * titleBuilder.setLeft({ ImageView(mActivity) }) { img ->
     *     img.setImageResource(R.drawable.ic_back)
     *     img.setOnClickListener { mActivity.finish() }
     *     img.setPadding(10.pt, 10.pt, 10.pt, 10.pt)
     * }
     */
    inline fun <reified T : View> setLeft(crossinline creator: () -> T, rsp: (T) -> Unit = {}): TitleBuilder {
        //margin属性是插入后才可以设置的
        handleView(LEFT_CUSTOM_VIEW, creator) {
            startToStartOf(it)
            centerVertically(it)
        }.also(rsp)
        return this
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        createImageView(RIGHT_ICON, resId, tintColor, onClick) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    fun setRight(label: Any, labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onClick: () -> Unit = {}): TitleBuilder {
        createTextView(RIGHT_TEXT, label, labelColor, drawable, onClick) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    inline fun <reified T : View> setRight(crossinline creator: () -> T, rsp: (T) -> Unit = {}): TitleBuilder {
        handleView(RIGHT_CUSTOM_VIEW, creator) {
            endToEndOf(it)
            centerVertically(it)
        }.also(rsp)
        return this
    }

    /**
     * 创建左右侧按钮方法
     */
    private fun createImageView(key: String, resId: Int, tintColor: Int, onClick: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        handleView<ImageView>(key, {
            ImageView(mActivity).also {
                it.setResource(resId)
                if (tintColor != 0) it.tint(tintColor)
                it.size(44.pt, 44.pt)
                it.padding(10.pt, 10.pt, 10.pt, 10.pt)
                it.click {
                    onClick.invoke()
                }
            }
        }, block)
    }

    private fun createTextView(key: String, label: Any, labelColor: Int, drawable: Drawable? = null, onClick: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        handleView<I18nTextView>(key, {
            I18nTextView(mActivity).also {
                when (label) {
                    is Int -> it.setI18nTheme(label, labelColor)
                    is String -> it.setTheme(label, labelColor)
                }
                it.padding(start = 15.pt, end = 15.pt)
                it.textSize(R.dimen.textSize14)
                it.gravity = Gravity.CENTER
                if (drawable != null) {
                    it.clearBackground()
                    it.clearHighlightColor()
                    drawable.setTint(color(labelColor))
                    it.setCompoundDrawables(drawable, null, null, null)
                    it.compoundDrawablePadding = 2.pt
                }
                it.click {
                    onClick.invoke()
                }
            }
        }, block)
    }

    /**
     * 外层创建view
     */
    inline fun <reified T : View> handleView(key: String, crossinline creator: () -> T, noinline block: ConstraintSet.(Int) -> Unit = {}): T {
        val parent = mBinding?.clRoot
        // 移除上一次的视图
        val lastId = idsMap[key]
        if (lastId != null && lastId != View.NO_ID) {
            parent?.findViewById<T>(lastId)?.let {
                parent.removeView(it)
            }
        }
        // 生成新的唯一 id
        val newViewId = View.generateViewId()
        idsMap[key] = newViewId
        val newView = creator.invoke()
        newView.id = newViewId
        parent?.addView(newView)
        parent?.applyConstraints {
            block(newViewId)
        }
        return newView
    }

    /**
     * 检测是否创建
     */
    fun nonNull(vararg keys: String): Boolean {
        return keys.all { idsMap[it] != null }
    }

    /**
     * 整体隐藏
     */
    fun hideTitle() {
        mBinding?.clRoot.gone()
    }

}