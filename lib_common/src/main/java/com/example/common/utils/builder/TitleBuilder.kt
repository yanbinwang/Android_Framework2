package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.app.Activity
import com.example.base.utils.function.color
import com.example.base.utils.function.view.click
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.setParam
import com.example.common.utils.tint

@SuppressLint("InflateParams")
class TitleBuilder(private val activity: Activity, private val binding: ViewTitleBarBinding) {
    private val statusBarBuilder by lazy { StatusBarBuilder(activity.window) }

    /**
     * 由于5.0响应系统导航栏颜色样式，但是不响应电池样式，故而全局设定的颜色是黑色
     * 初始化导航栏时，如果app是白色主题或者其余主题，在初始化时用代码修改导航栏颜色和黑白电池样式
     */
    init {
        statusBarBuilder.apply {
            statusBarLightMode(true)
            statusBarColor(activity.color(R.color.white))
        }
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景色（状态栏同步变为该颜色）
     * light->黑白电池
     * shade->标题底部是否带阴影
     */
    @JvmOverloads
    fun setTitle(title: String = "", titleColor: Int = R.color.grey_333333, bgColor: Int = R.color.white, light: Boolean = true, shade: Boolean = false): TitleBuilder {
        statusBarBuilder.apply {
            //调取样式切换耗费资源，故而做一个检测
            if(!light) statusBarLightMode(false)
            if(bgColor!= R.color.white) statusBarColor(activity.color(bgColor))
        }
        binding.clContainer.setBackgroundColor(activity.color(bgColor))
        binding.tvTitle.setParam(title, titleColor)
        binding.viewShade.apply { if (shade) visible() else gone() }
        return this
    }

    /**
     * 继承baseactivity，用include把布局引入后调用
     */
    fun setTransparentTitle(title: String = "", titleColor: Int = R.color.grey_333333, light: Boolean = true): TitleBuilder {
        statusBarBuilder.transparent(light)
        binding.clContainer.apply {
            statusBarPadding()
            setBackgroundColor(0)
        }
        binding.tvTitle.setParam(title, titleColor)
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
            setImageResource(resId)
            if (0 != tintColor) tint(tintColor)
            click { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRightResource(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.apply {
            visible()
            setImageResource(resId)
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
            setParam(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRightText(label: String, labelColor: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvRight.apply {
            visible()
            setParam(label, labelColor)
            click { onClick.invoke() }
        }
        return this
    }

    /**
     * 隐藏左侧返回
     */
    fun hideBack(): TitleBuilder {
        binding.ivLeft.gone()
        binding.tvLeft.gone()
        return this
    }

    /**
     * 默认配置返回样式
     */
    fun getDefault(): TitleBuilder {
        binding.ivLeft.apply {
            visible()
            click { activity.finish() }
        }
        return this
    }

}