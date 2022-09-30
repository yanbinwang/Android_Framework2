package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.app.Activity
import com.example.base.utils.function.color
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.setParam
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
class TitleBuilder(activity: Activity, private val binding: ViewTitleBarBinding) {
    private val weakActivity by lazy { WeakReference(activity).get() }
    private val statusBarBuilder by lazy { StatusBarBuilder(activity.window) }

    @JvmOverloads
    fun setTitle(titleStr: String = "", txtColor: Int = R.color.grey_333333, bgColor: Int = R.color.white, light: Boolean = true, shade: Boolean = false): TitleBuilder {
        statusBarBuilder.apply {
            statusBarLightMode(light)
            statusBarColor(weakActivity!!.color(bgColor))
        }
        binding.rlContainer.setBackgroundColor(weakActivity!!.color(bgColor))
        binding.tvTitle.setParam(titleStr, txtColor)
        binding.vShade.apply { if (shade) visible() else gone() }
        return this
    }

    /**
     * 继承baseactivity，用include把布局引入后调用
     */
    fun setTransparentTitle(titleStr: String = "", txtColor: Int = R.color.grey_333333, light: Boolean = true, enable: Boolean = false): TitleBuilder {
        statusBarBuilder.transparent(light, enable)
        binding.rlContainer.apply {
            setBackgroundColor(0)
            statusBarMargin()
        }
        binding.tvTitle.setParam(titleStr, txtColor)
        return this
    }

    @JvmOverloads
    fun setLeftResource(resId: Int, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivLeft.apply {
            visible()
            setImageResource(resId)
            setOnClickListener { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setLeftText(textStr: String, color: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvLeft.apply {
            visible()
            setParam(textStr, color)
            setOnClickListener { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRightResource(resId: Int, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.apply {
            visible()
            setImageResource(resId)
            setOnClickListener { onClick.invoke() }
        }
        return this
    }

    @JvmOverloads
    fun setRightText(textStr: String, color: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.tvRight.apply {
            visible()
            setParam(textStr, color)
            setOnClickListener { onClick.invoke() }
        }
        return this
    }

    fun hideBack(): TitleBuilder {
        binding.ivLeft.gone()
        return this
    }

    fun getDefault(): TitleBuilder {
        binding.ivLeft.setOnClickListener { weakActivity?.finish() }
        return this
    }

}