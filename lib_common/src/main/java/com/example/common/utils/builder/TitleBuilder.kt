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
    private val weakActivity by lazy { WeakReference(activity) }
    private val statusBarBuilder by lazy { StatusBarBuilder(weakActivity.get()!!) }

    init {
        statusBarBuilder.setStatusBarColor(ContextCompat.getColor(weakActivity.get()!!, R.color.white))
    }

    fun getDefault(): TitleBuilder {
        binding.llMainLeft.apply {
            visibility = View.VISIBLE
            setOnClickListener { weakActivity.get()?.finish() }

        }
        return this
    }

    fun hideBack(): TitleBuilder {
        binding.llMainLeft.apply {
            visibility = View.GONE
            setOnClickListener(null)
        }
        return this
    }

    fun hideTitle(isDark: Boolean = true): TitleBuilder {
        statusBarBuilder.setStatusBarLightMode(isDark)
        binding.rlMain.visibility = View.GONE
        binding.vMainLine.visibility = View.GONE
        return this
    }

    fun setTitle(titleStr: String, color: Int = ContextCompat.getColor(weakActivity.get()!!, R.color.black), isShade: Boolean = false, isDark: Boolean = true): TitleBuilder {
        statusBarBuilder.setStatusBarLightMode(isDark)
        binding.tvMainTitle.apply {
            text = titleStr
            setTextColor(color)
        }
        binding.vMainLine.visibility = if (isShade) View.VISIBLE else View.GONE
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
        binding.ivMainLeft.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
        binding.tvMainLeft.visibility = View.GONE
        return this
    }

    fun setLeftText(textStr: String): TitleBuilder {
        binding.ivMainLeft.visibility = View.GONE
        binding.tvMainLeft.apply {
            visibility = View.VISIBLE
            text = textStr
        }
        return this
    }

    fun setLeftTextColor(color: Int): TitleBuilder {
        binding.tvMainLeft.setTextColor(color)
        return this
    }

    fun setLeftOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llMainLeft.apply {
            visibility = View.VISIBLE
            setOnClickListener(onClick)
        }
        return this
    }

    fun setRightImageResource(resId: Int): TitleBuilder {
        binding.ivMainRight.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
        binding.tvMainRight.visibility = View.GONE
        return this
    }

    fun setRightText(textStr: String): TitleBuilder {
        binding.ivMainRight.visibility = View.GONE
        binding.tvMainRight.apply {
            visibility = View.VISIBLE
            text = textStr
        }
        return this
    }

    fun setRightTextColor(color: Int): TitleBuilder {
        binding.tvMainRight.setTextColor(color)
        return this
    }

    fun setRightOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llMainRight.apply {
            visibility = View.VISIBLE
            setOnClickListener(onClick)
        }
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