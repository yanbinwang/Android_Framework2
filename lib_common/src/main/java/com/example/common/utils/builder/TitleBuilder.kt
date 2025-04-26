package com.example.common.utils.builder

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.color
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.setTheme
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.clearBackground
import com.example.framework.utils.function.view.clearHighlightColor
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.visible

/**
 * 顶部标题默认不具备任何颜色和显示的按钮
 * 格式：
 * 1.左右侧图片/文本
 * 2.中间大标题
 * 3.左右侧间距默认，文本大小固定，图片可定制
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
//     * title->标题
//     * titleColor->标题颜色
//     * bgColor->背景颜色
//     * isShade->标题底部是否带阴影
//     */
//    fun setTitle(title: String = "", titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
//        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
//        mBinding?.tvTitle?.setTheme(title, titleColor)
//        mBinding?.viewShade?.apply { if (isShade) visible() else gone() }
//        setLeft()
//        return this
//    }
//
//    /**
//     * 页面不需要标题，只需要定制的返回按钮及特定背景
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
//    fun setTransparent(title: String = "", titleColor: Int = R.color.textPrimary): TitleBuilder {
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
//    /**
//     * 设置左/右侧文字
//     * label->文案
//     * labelColor->文案颜色
//     * onClick->点击事件
//     */
//    fun setLeft(label: String, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
//        mBinding?.tvLeft?.apply {
//            visible()
//            setTheme(label, labelColor)
//            click { onClick.invoke() }
//        }
//        mBinding?.ivLeft.gone()
//        return this
//    }
//
//    /**
//     * 部分页面UI需要更深度的定制
//     */
//    fun setLeftSecondary(label: String, labelColor: Int = R.color.textPrimary, resId: Int = R.mipmap.ic_btn_back, width: Int? = null, height: Int? = null, onClick: (textView: TextView) -> Unit = { mActivity.finish() }): TitleBuilder {
//        mBinding?.tvLeft?.apply {
//            visible()
//            setTheme(label, labelColor, resId)
//            if (null != width && null != height) size(width, height)
//            click { onClick.invoke(this) }
//        }
//        mBinding?.ivLeft.gone()
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
//    fun setRight(label: String, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = {}): TitleBuilder {
//        mBinding?.tvRight?.apply {
//            visible()
//            setTheme(label, labelColor)
//            click { onClick.invoke() }
//        }
//        mBinding?.ivRight.gone()
//        return this
//    }
//
//    fun setRightSecondary(label: String, labelColor: Int = R.color.textPrimary, resId: Int = R.mipmap.ic_btn_back, width: Int? = null, height: Int? = null, onClick: (textView: TextView) -> Unit = {}): TitleBuilder {
//        mBinding?.tvRight?.apply {
//            visible()
//            setTheme(label, labelColor, resId)
//            if (null != width && null != height) size(width, height)
//            click { onClick.invoke(this) }
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
class TitleBuilder(private val mActivity: AppCompatActivity, private val mBinding: ViewTitleBarBinding?) {

    init {
        mActivity.doOnDestroy {
            mBinding?.unbind()
        }
        mBinding?.clRoot.padding(5.pt, getStatusBarHeight(), 5.pt, 0)
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * isShade->标题底部是否带阴影
     */
    fun setTitle(title: String = "", titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else color(bgColor))
        mBinding?.tvTitle?.setTheme(title, titleColor)
        mBinding?.viewShade?.apply { if (isShade) visible() else gone() }
        setLeft()
        return this
    }

    /**
     * 页面不需要标题，只需要定制的返回按钮及特定背景
     */
    fun setTitleSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }, bgColor: Int = R.color.bgToolbar): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else color(bgColor))
        setLeft(resId, tintColor, onClick = onClick)
        return this
    }

    /**
     * 1.继承BaseActivity，在xml中include对应标题布局
     * 2.把布局bind传入工具类，实现绑定后，调取对应方法（private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }）
     */
    fun setTransparent(title: String = "", titleColor: Int = R.color.textPrimary): TitleBuilder {
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
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int = 44.pt, height: Int = 44.pt, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        val ivLeft = createImageView(resId, tintColor, width, height, onClick)
        setLeft(ivLeft)
        return this
    }

    /**
     * 设置左/右侧文字
     * label->文案
     * labelColor->文案颜色
     * drawablePair->是否包含图片（默认就是左侧的）
     *  1.drawable(it)获取图片
     *  2.drawable?.setBounds(0, 0, width, height)设置宽高
     *  3.view.setCompoundDrawables(startDrawable, topDrawable, endDrawable, bottomDrawable)调取绘制
     *  4.drawablePadding?.let { view.compoundDrawablePadding = it }文字间距
     * onClick->点击事件
     */
    fun setLeft(label: String, labelColor: Int = R.color.textPrimary, drawablePair: Pair<Drawable, Int>? = null, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        val tvLeft = createTextView(label, labelColor, drawablePair, onClick)
        setLeft(tvLeft)
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
    fun <T : View> setLeft(viewCreator: () -> T, setup: (T) -> Unit = {}): TitleBuilder {
        val view = viewCreator()
        setup(view) // 先配置视图（如设置点击事件、属性）
        setLeft(view) // 最后添加到布局
        return this
    }

    /**
     * 插入一个/多个自定义view
     */
    fun setLeft(view: View, isRemove: Boolean = true) {
        mBinding?.llLeft?.apply {
            if (isRemove) removeAllViews()
            addView(view)
        }
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int = 44, height: Int = 44, onClick: () -> Unit = {}): TitleBuilder {
        val ivRight = createImageView(resId, tintColor, width, height, onClick)
        setRight(ivRight)
        return this
    }

    fun setRight(label: String, labelColor: Int = R.color.textPrimary, drawablePair: Pair<Drawable, Int>? = null, onClick: () -> Unit = {}): TitleBuilder {
        val tvRight = createTextView(label, labelColor, drawablePair, onClick)
        setRight(tvRight)
        return this
    }

    fun <T : View> setRight(viewCreator: () -> T, setup: (T) -> Unit = {}): TitleBuilder {
        val view = viewCreator()
        setup(view)
        setRight(view)
        return this
    }

    fun setRight(view: View, isRemove: Boolean = true) {
        mBinding?.llRight?.apply {
            if (isRemove) removeAllViews()
            addView(view)
        }
    }

    /**
     * 创建左右侧按钮方法
     */
    private fun createImageView(resId: Int, tintColor: Int, width: Int, height: Int, onClick: () -> Unit): ImageView {
        return ImageView(mActivity).apply {
            setResource(resId)
            if (tintColor != 0) tint(tintColor)
            size(width, height)
            padding(10.pt, 10.pt, 10.pt, 10.pt)
            click {
                onClick.invoke()
            }
        }
    }

    private fun createTextView(label: String, labelColor: Int, drawablePair: Pair<Drawable, Int>? = null, onClick: () -> Unit): TextView {
        return TextView(mActivity).apply {
            setTheme(label, labelColor)
            padding(start = 15.pt, end = 15.pt)
            textSize(R.dimen.textSize14)
            gravity = Gravity.CENTER
            if (drawablePair != null) {
                clearBackground()
                clearHighlightColor()
                setCompoundDrawables(drawablePair.first, null, null, null)
                compoundDrawablePadding = drawablePair.second
            }
            click {
                onClick.invoke()
            }
        }
    }

    /**
     * 整体隐藏
     */
    fun hideTitle() {
        mBinding?.clRoot.gone()
    }

}