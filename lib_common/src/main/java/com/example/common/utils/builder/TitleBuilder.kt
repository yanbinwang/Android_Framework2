package com.example.common.utils.builder

import android.app.Activity
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.ScreenUtil.statusBarPadding
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.color
import com.example.framework.utils.function.view.*

/**
 * 顶部标题默认不具备任何颜色和显示的按钮
 */
class TitleBuilder(private val activity: Activity, private val binding: ViewTitleBarBinding) {

    init {
        binding.clContainer.statusBarPadding()
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * shade->标题底部是否带阴影
     * transparent->是否是透明
     */
    @JvmOverloads
    fun setTitle(title: String = "", titleColor: Int = R.color.grey_333333, bgColor: Int = R.color.white, shade: Boolean = false): TitleBuilder {
        binding.clContainer.setBackgroundColor(activity.color(bgColor))
        binding.tvTitle.setArguments(title, titleColor)
        binding.viewShade.apply { if (shade) visible() else gone() }
        return this
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * onClick->点击事件
     */
    @JvmOverloads
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivLeft.apply {
            visible()
//            size(88.pt, 88.pt)
//            paddingAll(20.pt)
            imageResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.apply {
            visible()
            imageResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        return this
    }

    /**
     * 设置左/右侧文字
     * label->文案
     * labelColor->文案颜色
     * onClick->点击事件
     */
    @JvmOverloads
    fun setLeft(label: String, labelColor: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvLeft.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRight(label: String, labelColor: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvRight.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    /**
     * 整体隐藏
     */
    fun hideTitle() {
        binding.clContainer.gone()
    }

    /**
     * 默认配置返回样式
     */
    fun getDefault(): TitleBuilder {
        binding.ivLeft.apply {
            imageResource(R.mipmap.ic_btn_back)
            visible()
            click { activity.finish() }
        }
        return this
    }

}