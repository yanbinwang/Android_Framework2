package com.example.common.utils.builder

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.setI18nTheme
import com.example.common.utils.function.setTheme
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.visible

/**
 * 顶部标题默认不具备任何颜色和显示的按钮
 * 格式->左右侧图片/文本，中间大标题，左右侧间距默认文本大小都应固定，图片可定制
 */
class TitleBuilder(private val mActivity: AppCompatActivity, private val mBinding: ViewTitleBarBinding?) {

    init {
        mActivity.doOnDestroy { mBinding?.unbind() }
        mBinding?.clRoot.padding(top = getStatusBarHeight())
    }

    /**
     * 默认二级页面标题配置
     * resTitle->标题res路径
     * titleColor->标题颜色
     * bgColor->背景颜色
     * isShade->标题底部是否带阴影
     */
    fun setTitle(resTitle: Int = -1, titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
        mBinding?.tvTitle?.setI18nTheme(resTitle, titleColor)
        return setRoot(bgColor, isShade)
    }

    fun setTitle(title: String, titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
        mBinding?.tvTitle?.setTheme(title, titleColor)
        return setRoot(bgColor, isShade)
    }

    private fun setRoot(bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
        mBinding?.viewShade?.apply { if (isShade) visible() else gone() }
        setLeft()
        return this
    }

    /**
     * 部分页面不需要标题，只需要一个定制的返回按钮和特定背景，故而使用此方法
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
    fun setTransparent(resTitle: Int = -1, titleColor: Int = R.color.textPrimary): TitleBuilder {
        return setTitle(resTitle, titleColor, 0)
    }

    fun setTransparent(title: String, titleColor: Int = R.color.textPrimary): TitleBuilder {
        return setTitle(title, titleColor, 0)
    }

    fun setTransparentSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        return setTitleSecondary(resId, tintColor, onClick, 0)
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * onClick->点击事件
     */
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int? = null, height: Int? = null, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.ivLeft?.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            if (null != width && null != height) size(width, height)
            click { onClick.invoke() }
        }
        mBinding?.tvLeft.gone()
        return this
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int? = null, height: Int? = null, onClick: () -> Unit = { }): TitleBuilder {
        mBinding?.ivRight?.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            if (null != width && null != height) size(width, height)
            click { onClick.invoke() }
        }
        mBinding?.tvRight.gone()
        return this
    }

    /**
     * 设置左/右侧文字
     * resLabel->文案res
     * labelColor->文案颜色
     * onClick->点击事件
     */
    fun setI18nLeft(resLabel: Int, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.tvLeft?.apply {
            visible()
            setI18nTheme(resLabel, labelColor)
            click { onClick.invoke() }
        }
        mBinding?.ivLeft.gone()
        return this
    }

    fun setI18nRight(resLabel: Int, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { }): TitleBuilder {
        mBinding?.tvRight?.apply {
            visible()
            setI18nTheme(resLabel, labelColor)
            click { onClick.invoke() }
        }
        mBinding?.ivRight.gone()
        return this
    }

    /**
     * 整体隐藏
     */
    fun hideTitle() {
        mBinding?.clRoot.gone()
    }

}