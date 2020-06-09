package com.example.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat

import com.example.common.R
import com.example.framework.utils.StatusBarUtil

import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
class TitleBuilder(activity: Activity) {
    private val mActivity: WeakReference<Activity> = WeakReference(activity)
    private var view: View? = null
    private var mainLine: View? = null //标题线
    private var mainLeftLin: LinearLayout? = null
    private var mainRightLin: LinearLayout? = null //左右侧按钮
    private var mainLeftImg: ImageView? = null
    private var mainRightImg: ImageView? = null //左右侧按钮图片
    private var mainTitleTxt: TextView? = null
    private var mainLeftTxt: TextView? = null
    private var mainRightTxt: TextView? = null //页面标题,左侧文字,右侧文字
    private val statusBarUtil: StatusBarUtil

    init {
        statusBarUtil = StatusBarUtil(mActivity.get()!!)
        statusBarUtil.setStatusBarColor(ContextCompat.getColor(mActivity.get()!!, R.color.white))
        instanceObjects()
    }

    private fun instanceObjects() {
        view = mActivity.get()?.findViewById(R.id.rl_main)
        mainTitleTxt = view?.findViewById(R.id.tv_main_title)
        mainLeftTxt = view?.findViewById(R.id.tv_main_left)
        mainRightTxt = view?.findViewById(R.id.tv_main_right)
        mainLeftLin = view?.findViewById(R.id.ll_main_left)
        mainLeftImg = view?.findViewById(R.id.iv_main_left)
        mainRightLin = view?.findViewById(R.id.ll_main_right)
        mainRightImg = view?.findViewById(R.id.iv_main_right)
        mainLine = view?.findViewById(R.id.v_main_line)
    }

    fun getDefault(): TitleBuilder {
        mainLeftLin?.visibility = View.VISIBLE
        mainLeftLin?.setOnClickListener { mActivity.get()?.finish() }
        return this
    }

    fun hideBack(): TitleBuilder {
        mainLeftLin?.visibility = View.GONE
        mainLeftLin?.setOnClickListener(null)
        return this
    }

    fun hideTitle(): TitleBuilder {
        hideTitle(true)
        return this
    }

    fun hideTitle(isDark: Boolean): TitleBuilder {
        view?.visibility = View.GONE
        mainLine?.visibility = View.GONE
        statusBarUtil.setStatusBarLightMode(isDark)
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
        setTitle(titleStr, ContextCompat.getColor(mActivity.get()!!, R.color.black), isShade, isDark)
        return this
    }

    fun setTitle(titleStr: String, color: Int, isShade: Boolean, isDark: Boolean): TitleBuilder {
        if (mainTitleTxt != null) {
            mainTitleTxt?.text = titleStr
            mainTitleTxt?.setTextColor(color)
        }
        if (isShade) {
            mainLine?.visibility = View.VISIBLE
        } else {
            mainLine?.visibility = View.GONE
        }
        statusBarUtil.setStatusBarLightMode(isDark)
        return this
    }

    fun setTitleTextColor(color: Int): TitleBuilder {
        mainTitleTxt?.setTextColor(color)
        return this
    }

    fun setTitleBackgroundColor(color: Int): TitleBuilder {
        statusBarUtil.setStatusBarColor(color)
        view?.setBackgroundColor(color)
        return this
    }

    fun setLeftImageResource(resId: Int): TitleBuilder {
        mainLeftTxt?.visibility = View.GONE
        mainLeftImg?.visibility = View.VISIBLE
        mainLeftImg?.setImageResource(resId)
        return this
    }

    fun setLeftText(text: String): TitleBuilder {
        mainLeftTxt?.visibility = View.VISIBLE
        mainLeftImg?.visibility = View.GONE
        mainLeftTxt?.text = text
        return this
    }

    fun setLeftTextColor(color: Int): TitleBuilder {
        mainLeftTxt?.setTextColor(color)
        return this
    }

    fun setLeftOnclick(onclick: View.OnClickListener): TitleBuilder {
        mainLeftLin?.visibility = View.VISIBLE
        mainLeftLin?.setOnClickListener(onclick)
        return this
    }

    fun setRightImageResource(resId: Int): TitleBuilder {
        mainRightTxt?.visibility = View.GONE
        mainRightImg?.visibility = View.VISIBLE
        mainRightImg?.setImageResource(resId)
        return this
    }

    fun setRightText(text: String): TitleBuilder {
        mainRightTxt?.visibility = View.VISIBLE
        mainRightImg?.visibility = View.GONE
        mainRightTxt?.text = text
        return this
    }

    fun setRightTextColor(color: Int): TitleBuilder {
        mainRightTxt?.setTextColor(color)
        return this
    }

    fun setRightOnclick(onclick: View.OnClickListener): TitleBuilder {
        mainRightLin?.visibility = View.VISIBLE
        mainRightLin?.setOnClickListener(onclick)
        return this
    }

    //透明标题头(自己实现导航栏，默认白色)
    fun setTransparentStatus(): TitleBuilder {
        hideTitle(false)
        statusBarUtil.setTransparentStatus()
        return this
    }

    //透明标题头(自己实现导航栏，默认黑色)
    fun setTransparentDarkStatus(): TitleBuilder {
        hideTitle()
        statusBarUtil.setTransparentDarkStatus()
        return this
    }

}