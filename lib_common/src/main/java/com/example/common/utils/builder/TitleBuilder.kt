package com.example.common.utils.builder

import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.visible

/**
 * 顶部标题默认不具备任何颜色和显示的按钮
 * 格式->左右侧图片/文本，中间是大标题
 */
class TitleBuilder(private val mActivity: AppCompatActivity, private val mBinding: ViewTitleBarBinding?) {
    val layout: ConstraintLayout?
        get() = mBinding?.clRoot
    val ivLeft: ImageView?
        get() = mBinding?.ivLeft
    val tvLeft: TextView?
        get() = mBinding?.tvLeft
    val ivRight: ImageView?
        get() = mBinding?.ivRight
    val tvRight: TextView?
        get() = mBinding?.tvRight

    init {
        mActivity.doOnDestroy { mBinding?.unbind() }
        mBinding?.clRoot.padding(top = getStatusBarHeight())
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * shade->标题底部是否带阴影
     */
    fun setTitle(title: String = "", titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, isShade: Boolean = false): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
        mBinding?.tvTitle?.setArguments(title, titleColor)
        mBinding?.viewShade?.apply { if (isShade) visible() else gone() }
        setLeft()
        return this
    }

    /**
     * 部分页面不需要标题，只需要一个定制的返回按钮和特定背景，故而使用此方法
     */
    fun setTitleSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }, bgColor: Int = R.color.bgToolbar): TitleBuilder {
        mBinding?.clRoot?.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else mActivity.color(bgColor))
        setLeft(resId, tintColor, onClick)
        return this
    }

    /**
     * 继承BaseActivity，在xml中include对应标题布局
     * 把布局bind传入工具类，实现绑定后，调取对应方法
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
     * onClick->点击事件
     */
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.ivLeft?.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        mBinding?.tvLeft.gone()
        return this
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.ivRight?.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        mBinding?.tvRight.gone()
        return this
    }

    /**
     * 设置左/右侧文字
     * label->文案
     * labelColor->文案颜色
     * onClick->点击事件
     */
    fun setLeft(label: String, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.tvLeft?.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        mBinding?.ivLeft.gone()
        return this
    }

    fun setRight(label: String, labelColor: Int = R.color.textPrimary, onClick: () -> Unit = { mActivity.finish() }): TitleBuilder {
        mBinding?.tvRight?.apply {
            visible()
            setArguments(label, labelColor)
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