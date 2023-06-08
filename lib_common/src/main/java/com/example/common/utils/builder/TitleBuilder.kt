package com.example.common.utils.builder

import android.app.Activity
import android.graphics.Color
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.color
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
class TitleBuilder(private val activity: Activity, private val binding: ViewTitleBarBinding) {

    init {
        binding.clContainer.padding(top = getStatusBarHeight())
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * shade->标题底部是否带阴影
     */
    fun setTitle(title: String = "", titleColor: Int = R.color.black, bgColor: Int = R.color.white, shade: Boolean = false): TitleBuilder {
        binding.clContainer.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else activity.color(bgColor))
        binding.tvTitle.setArguments(title, titleColor)
        binding.viewShade.apply { if (shade) visible() else gone() }
        return this
    }

    /**
     * 部分页面不需要标题，只需要一个定制的返回按钮和特定背景，故而使用此方法
     */
    fun setTitle(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}, bgColor: Int = R.color.white): TitleBuilder {
        binding.ivLeft.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
            binding.clContainer.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else activity.color(bgColor))
        }
        return this
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * onClick->点击事件
     */
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivLeft.apply {
            visible()
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        return this
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.apply {
            visible()
            setResource(resId)
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
    fun setLeft(label: String, labelColor: Int = R.color.black, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvLeft.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    fun setRight(label: String, labelColor: Int = R.color.black, onClick: () -> Unit = {}): TitleBuilder {
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
            setResource(R.mipmap.ic_btn_back)
            visible()
            click { activity.finish() }
        }
        return this
    }

}