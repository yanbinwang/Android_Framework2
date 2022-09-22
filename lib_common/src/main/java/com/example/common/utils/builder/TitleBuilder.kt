package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.example.base.utils.function.color
import com.example.common.R
import com.example.common.databinding.ViewTitleBarBinding
import com.example.common.utils.setData
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
class TitleBuilder(activity: Activity, private val binding: ViewTitleBarBinding) {
    private val weakActivity by lazy { WeakReference(activity).get() }
    private val statusBarBuilder by lazy { StatusBarBuilder(activity.window) }

    init {
        statusBarBuilder.setStatusBarColor(weakActivity!!.color(R.color.white))
    }

    @JvmOverloads
    fun setTitle(titleStr: String, dark: Boolean = true, shade: Boolean = false): TitleBuilder {
        statusBarBuilder.setStatusBarLightMode(dark)
        binding.tvTitle.setData(titleStr, if (dark) R.color.black else R.color.white)
        binding.vShade.visibility = if (shade) View.VISIBLE else View.GONE
        return this
    }

    fun setTitleTextColor(color: Int): TitleBuilder {
        binding.tvTitle.setTextColor(color)
        return this
    }

    fun setTitleBackgroundColor(color: Int): TitleBuilder {
        statusBarBuilder.setStatusBarColor(color)
        binding.rlContainer.setBackgroundColor(color)
        return this
    }

    fun setLeftResource(resId: Int): TitleBuilder {
        binding.ivLeft.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
        binding.tvLeft.visibility = View.GONE
        return this
    }

    fun setLeftText(textStr: String): TitleBuilder {
        binding.ivLeft.visibility = View.GONE
        binding.tvLeft.apply {
            visibility = View.VISIBLE
            text = textStr
        }
        return this
    }

    fun setLeftTextColor(color: Int): TitleBuilder {
        binding.tvLeft.setTextColor(color)
        return this
    }

    fun setLeftOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llLeft.apply {
            visibility = View.VISIBLE
            setOnClickListener(onClick)
        }
        return this
    }

    fun setRightResource(resId: Int): TitleBuilder {
        binding.ivRight.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
        binding.tvRight.visibility = View.GONE
        return this
    }

    fun setRightText(textStr: String): TitleBuilder {
        binding.ivRight.visibility = View.GONE
        binding.tvRight.apply {
            visibility = View.VISIBLE
            text = textStr
        }
        return this
    }

    fun setRightTextColor(color: Int): TitleBuilder {
        binding.tvRight.setTextColor(color)
        return this
    }

    fun setRightOnClick(onClick: View.OnClickListener): TitleBuilder {
        binding.llRight.apply {
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

    fun hideBack(): TitleBuilder {
        binding.llLeft.apply {
            visibility = View.GONE
            setOnClickListener(null)
        }
        return this
    }

    @JvmOverloads
    fun hideTitle(dark: Boolean = true): TitleBuilder {
        statusBarBuilder.setStatusBarLightMode(dark)
        binding.rlContainer.visibility = View.GONE
        binding.vShade.visibility = View.GONE
        return this
    }

    fun getDefault(): TitleBuilder {
        binding.llLeft.apply {
            visibility = View.VISIBLE
            setOnClickListener { weakActivity?.finish() }
        }
        return this
    }

}