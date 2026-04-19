package com.yanzhenjie.album.app.album

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.yanzhenjie.album.app.Contract
import com.yanzhenjie.album.model.Widget
import com.yanzhenjie.album.utils.AlbumUtil.setDrawableTint

/**
 * 空页面 View 层
 * 功能：当手机里没有图片/视频时，显示这个页面
 * 提供：拍照、录像按钮，纯 UI 展示
 */
class NullView(activity: Activity, presenter: Contract.NullPresenter) : Contract.NullView(activity, presenter), View.OnClickListener {
    // 标题栏
    private var mToolbar: Toolbar? = null
    // 标题文字
    private var mTitle: TextView? = null
    // 空页面提示文字
    private var mTvMessage: TextView? = null
    // 拍照按钮
    private var mBtnTakeImage: Button? = null
    // 录像按钮
    private var mBtnTakeVideo: Button? = null

    /**
     * 构造方法：绑定控件
     */
    init {
        // 绑定所有控件
        mToolbar = activity.findViewById(R.id.toolbar)
        mTitle = activity.findViewById(R.id.tv_title)
        mTvMessage = activity.findViewById(R.id.tv_message)
        mBtnTakeImage = activity.findViewById(R.id.btn_camera_image)
        mBtnTakeVideo = activity.findViewById(R.id.btn_camera_video)
        // 按钮点击事件
        clicks(mBtnTakeImage, mBtnTakeVideo)
    }

    /**
     * 初始化页面样式（颜色、主题、图标）
     * 按钮样式：颜色、背景
     */
    override fun setupViews(widget: Widget) {
        // 设置返回箭头
        val navigationIcon = getDrawable(R.mipmap.gallery_ic_back)
        // 浅色 / 深色 主题切换
        if (widget.uiStyle == Widget.STYLE_LIGHT) {
            setDrawableTint(navigationIcon, getColor(R.color.galleryIconDark))
            mTitle?.setTextColor(getColor(R.color.galleryFontDark))
        } else {
            mTitle?.setTextColor(getColor(R.color.galleryFontLight))
        }
        // 设置返回按钮
        setHomeAsUpIndicator(navigationIcon)
        // 标题同步状态栏颜色
        mToolbar?.setBackgroundColor(getColor(widget.statusBarColor))
        mTitle?.text = widget.title
        // 设置按钮颜色
        val buttonSelector = widget.buttonSelector
        mBtnTakeImage?.setBackgroundTintList(buttonSelector)
        mBtnTakeVideo?.setBackgroundTintList(buttonSelector)
        // 获取按钮主题色
        val isLight = shouldUseWhiteSystemBarsForColor(buttonSelector.defaultColor)
        // 如果需要深色主题,提取出绘制的图标并渲染成深色
        if (!isLight) {
            val takeImageDraws = mBtnTakeImage?.getCompoundDrawablesRelative()
            takeImageDraws?.get(0)?.let { takeImageIcon ->
                setDrawableTint(takeImageIcon, getColor(R.color.galleryIconDark))
                mBtnTakeImage?.setCompoundDrawables(takeImageIcon, null, null, null)
                mBtnTakeImage?.setTextColor(getColor(R.color.galleryFontDark))
            }
            val takeVideoDraws = mBtnTakeVideo?.getCompoundDrawablesRelative()
            takeVideoDraws?.get(0)?.let { takeVideoIcon ->
                setDrawableTint(takeVideoIcon, getColor(R.color.galleryIconDark))
                mBtnTakeVideo?.setCompoundDrawables(takeVideoIcon, null, null, null)
                mBtnTakeVideo?.setTextColor(getColor(R.color.galleryFontDark))
            }
        } else {
            mBtnTakeImage?.setTextColor(getColor(R.color.galleryFontLight))
            mBtnTakeVideo?.setTextColor(getColor(R.color.galleryFontLight))
        }
    }

    /**
     * 设置空页面提示文字
     */
    override fun setMessage(message: Int) {
        mTvMessage?.setText(message)
    }

    /**
     * 显示 / 隐藏 拍照按钮
     */
    override fun setMakeImageDisplay(display: Boolean) {
        mBtnTakeImage?.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 显示 / 隐藏 录像按钮
     */
    override fun setMakeVideoDisplay(display: Boolean) {
        mBtnTakeVideo?.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 点击拍照 / 录像，交给 Presenter 处理逻辑
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_camera_image -> getPresenter().takePicture()
            R.id.btn_camera_video -> getPresenter().takeVideo()
        }
    }

}