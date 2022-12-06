package com.example.common.utils.builder

import android.app.Activity
import com.example.base.utils.function.color
import com.example.base.utils.function.view.*
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.function.imageResource
import com.example.common.utils.function.pt
import com.example.common.utils.function.setArguments

class TitleBuilder(private val activity: Activity, private val binding: ViewTitleBarBinding) {
    private val statusBarBuilder by lazy { StatusBarBuilder(activity.window) }

    init {
        //防背刺，初始化时再检测一下特定编号的版本深浅主题
        statusBarBuilder.statusBarCheckDomestic()
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * shade->标题底部是否带阴影
     */
    @JvmOverloads
    fun setTitle(title: String = "", titleColor: Int = R.color.grey_333333, shade: Boolean = false): TitleBuilder {
        binding.clContainer.setBackgroundColor(activity.color(R.color.white))
        binding.tvTitle.setArguments(title, titleColor)
        binding.viewShade.apply { if (shade) visible() else gone() }
        return this
    }

    /**
     * 继承baseactivity，用include把布局引入后调用
     */
    @JvmOverloads
    fun setTransparentTitle(title: String = "", titleColor: Int = R.color.grey_333333, light: Boolean = true): TitleBuilder {
        statusBarBuilder.transparent(light)
        binding.clContainer.statusBarPadding()
        binding.tvTitle.setArguments(title, titleColor)
        return this
    }

    /**
     * 调取系统状态栏颜色样式修改是比较费资源的操作，默认样式配置了若不是特定页面不做修改，如果需要再调取当前代码
     */
    @JvmOverloads
    fun setBackgroundColor(bgColor: Int = R.color.black, light: Boolean = false): TitleBuilder {
        statusBarBuilder.apply {
            statusBarLightMode(light)
            statusBarColor(activity.color(bgColor))
        }
        binding.clContainer.setBackgroundColor(activity.color(bgColor))
        return this
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * onClick->点击事件
     */
    @JvmOverloads
    fun setLeftResource(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
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
    fun setRightResource(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
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
    fun setLeftText(label: String, labelColor: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvLeft.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRightText(label: String, labelColor: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvRight.apply {
            visible()
            setArguments(label, labelColor)
            click { onClick.invoke() }
        }
        return this
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