package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.app.Activity
import com.example.base.utils.function.color
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.setParameter
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
class TitleBuilder(activity: Activity, private val binding: ViewTitleBarBinding) {
    private var dark = true
    private val weakActivity by lazy { WeakReference(activity).get() }
    private val statusBarBuilder by lazy { StatusBarBuilder(activity.window) }

    init {
        statusBarBuilder.setStatusBarColor(weakActivity!!.color(R.color.white))
    }

    @JvmOverloads
    fun setTitle(titleStr: String, dark: Boolean = true, shade: Boolean = false): TitleBuilder {
        this.dark = dark
        statusBarBuilder.setStatusBarLightMode(dark)
        binding.tvTitle.setParameter(titleStr, if (dark) R.color.black else R.color.white)
        binding.vShade.apply { if (shade) visible() else gone() }
        return this
    }

    fun setTitleParameter(txtColor: Int = R.color.grey_333333, bgColor: Int = R.color.white): TitleBuilder {
        binding.tvTitle.setTextColor(weakActivity!!.color(txtColor))
        statusBarBuilder.setStatusBarColor(weakActivity!!.color(bgColor))
        binding.rlContainer.setBackgroundColor(weakActivity!!.color(bgColor))
        return this
    }

    fun setLeftResource(resId: Int, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivLeft.apply {
            visible()
            setImageResource(resId)
        }
        binding.tvLeft.gone()
        binding.llLeft.setOnClickListener { onClick.invoke() }
        return this
    }

    fun setLeftText(textStr: String, color: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivLeft.gone()
        binding.tvLeft.apply {
            visible()
            setParameter(textStr, color)
        }
        binding.llLeft.setOnClickListener { onClick.invoke() }
        return this
    }

    fun setRightResource(resId: Int, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.apply {
            visible()
            setImageResource(resId)
        }
        binding.tvRight.gone()
        binding.llRight.setOnClickListener { onClick.invoke() }
        return this
    }

    fun setRightText(textStr: String, color: Int = R.color.grey_333333, onClick: () -> Unit = {}): TitleBuilder {
        binding.ivRight.gone()
        binding.tvRight.apply {
            visible()
            setParameter(textStr, color)
        }
        binding.llRight.setOnClickListener { onClick.invoke() }
        return this
    }

    fun hideBack(): TitleBuilder {
        binding.llLeft.apply {
            gone()
            setOnClickListener(null)
        }
        return this
    }

    fun getDefault(): TitleBuilder {
        binding.llLeft.setOnClickListener { weakActivity?.finish() }
        return this
    }

}