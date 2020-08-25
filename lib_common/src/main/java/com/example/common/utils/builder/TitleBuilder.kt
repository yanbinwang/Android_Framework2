package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
class TitleBuilder(activity: Activity, private val binding: ViewTitleBarBinding) {
    private val weakActivity = WeakReference(activity)
    private val statusBarBuilder = StatusBarBuilder(weakActivity.get()!!)

    init {
        statusBarBuilder.setStatusBarColor(ContextCompat.getColor(weakActivity.get()!!, R.color.white))
    }

    fun getDefault(): TitleBuilder {
        binding.llMainLeft.visibility = View.VISIBLE
        binding.llMainLeft.setOnClickListener { weakActivity.get()?.finish() }
        return this
    }

    fun hideBack(): TitleBuilder {
        binding.llMainLeft.visibility = View.GONE
        binding.llMainLeft.setOnClickListener(null)
        return this
    }

    fun hideTitle(): TitleBuilder {
        hideTitle(true)
        return this
    }

    fun hideTitle(isDark: Boolean): TitleBuilder {
        binding.rlMain.visibility = View.GONE
        binding.vMainLine.visibility = View.GONE
        statusBarBuilder.setStatusBarLightMode(isDark)
        return this
    }

    fun setTitle(titleStr: String): TitleBuilder {
        setTitle(titleStr, true)
        return this
    }

    fun setTitle(titleStr: String, isDark: Boolean): TitleBuilder {
        setTitle(titleStr, false, isDark)
        return this
    }

    fun setTitle(titleStr: String, isShade: Boolean, isDark: Boolean): TitleBuilder {
        setTitle(titleStr, ContextCompat.getColor(weakActivity.get()!!, R.color.black), isShade, isDark)
        return this
    }

    fun setTitle(titleStr: String, color: Int, isShade: Boolean, isDark: Boolean): TitleBuilder {
        binding.tvMainTitle.text = titleStr
        binding.tvMainTitle.setTextColor(color)
        binding.vMainLine.visibility = if (isShade) View.VISIBLE else View.GONE
        statusBarBuilder.setStatusBarLightMode(isDark)
        return this
    }

    fun setTitleTextColor(color: Int): TitleBuilder {
        binding.tvMainTitle.setTextColor(color)
        return this
    }

    fun setTitleBackgroundColor(color: Int): TitleBuilder {
        statusBarBuilder.setStatusBarColor(color)
        binding.rlMain.setBackgroundColor(color)
        return this
    }

    fun setLeftImageResource(resId: Int): TitleBuilder {
        binding.tvMainLeft.visibility = View.GONE
        binding.ivMainLeft.visibility = View.VISIBLE
        binding.ivMainLeft.setImageResource(resId)
        return this
    }

    fun setLeftText(text: String): TitleBuilder {
        binding.tvMainLeft.visibility = View.VISIBLE
        binding.ivMainLeft.visibility = View.GONE
        binding.tvMainLeft.text = text
        return this
    }

    fun setLeftTextColor(color: Int): TitleBuilder {
        binding.tvMainLeft.setTextColor(color)
        return this
    }

    fun setLeftOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llMainLeft.visibility = View.VISIBLE
        binding.llMainLeft.setOnClickListener(onClick)
        return this
    }

    fun setRightImageResource(resId: Int): TitleBuilder {
        binding.tvMainRight.visibility = View.GONE
        binding.ivMainRight.visibility = View.VISIBLE
        binding.ivMainRight.setImageResource(resId)
        return this
    }

    fun setRightText(text: String): TitleBuilder {
        binding.tvMainRight.visibility = View.VISIBLE
        binding.ivMainRight.visibility = View.GONE
        binding.tvMainRight.text = text
        return this
    }

    fun setRightTextColor(color: Int): TitleBuilder {
        binding.tvMainRight.setTextColor(color)
        return this
    }

    fun setRightOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llMainRight.visibility = View.VISIBLE
        binding.llMainRight.setOnClickListener(onClick)
        return this
    }

    //透明标题头(自己实现导航栏，默认白色)
    fun setTransparentStatus(): TitleBuilder {
        hideTitle(false)
        statusBarBuilder.setTransparentStatus()
        return this
    }

    //透明标题头(自己实现导航栏，默认黑色)
    fun setTransparentDarkStatus(): TitleBuilder {
        hideTitle()
        statusBarBuilder.setTransparentDarkStatus()
        return this
    }

}