package com.example.gallery.feature.album.app.album

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.getHighLightColor
import com.example.framework.utils.function.value.getNormalColor
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.selectorRoundBackground
import com.example.gallery.R
import com.example.gallery.feature.album.app.Contract
import com.example.gallery.feature.album.bean.Widget
import com.example.gallery.utils.MediaUtil.setDrawableTint

/**
 * 空页面 View 层
 * 功能：当手机里没有图片/视频时，显示这个页面，提供：拍照、录像按钮，纯 UI 展示
 */
class NullView(activity: FragmentActivity, presenter: Contract.NullPresenter) : Contract.NullView(activity, presenter), View.OnClickListener {
    // 标题栏
    private val mToolbar = activity.findViewById<Toolbar>(R.id.toolbar)
    // 标题文字
    private val mTitle = activity.findViewById<TextView>(R.id.tv_title)
    // 空页面提示文字
    private val mMessage = activity.findViewById<TextView>(R.id.tv_message)
    // 拍照按钮
    private val mTakeImage = activity.findViewById<LinearLayout>(R.id.layout_camera_image)
    private val mTvTakeImage = activity.findViewById<TextView>(R.id.tv_camera_image)
    // 录像按钮
    private val mTakeVideo = activity.findViewById<LinearLayout>(R.id.layout_camera_video)
    private val mTvTakeVideo = activity.findViewById<TextView>(R.id.tv_camera_video)

    /**
     * 构造方法：绑定控件
     */
    init {
        // 按钮点击事件
        clicks(mTakeImage, mTakeVideo)
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
            mTitle.setTextColor(getColor(R.color.galleryFontDark))
        } else {
            mTitle.setTextColor(getColor(R.color.galleryFontLight))
        }
        // 设置返回按钮
        setHomeAsUpIndicator(navigationIcon)
        // 标题同步状态栏颜色
        mToolbar.setBackgroundColor(getColor(widget.statusBarColor))
        mTitle.text = widget.title
        // 设置按钮样式
        val buttonSelector = widget.buttonSelector
        val normalColor = buttonSelector.getNormalColor()
        val pressedColor = buttonSelector.getHighLightColor()
        mTakeImage.selectorRoundBackground(normalColor, pressedColor, normalColor, 2.ptFloat)
        mTakeVideo.selectorRoundBackground(normalColor, pressedColor, normalColor, 2.ptFloat)
        // 获取按钮主题色 , 如果需要深色主题则提取出绘制的图标并渲染成深色 -> 此处拿取的是normal
        if (!shouldUseWhiteSystemBarsForColor(buttonSelector.defaultColor)) {
            // 拍照片
            val takeImageIcon = mTvTakeImage.compoundDrawablesRelative[0]
            setDrawableTint(takeImageIcon, getColor(R.color.galleryIconDark))
            mTvTakeImage.setCompoundDrawables(takeImageIcon, null, null, null)
            mTvTakeImage.setTextColor(getColor(R.color.galleryFontDark))
            // 录视频
            val takeVideoIcon = mTvTakeVideo.compoundDrawablesRelative[0]
            setDrawableTint(takeVideoIcon, getColor(R.color.galleryIconDark))
            mTvTakeVideo.setCompoundDrawables(takeVideoIcon, null, null, null)
            mTvTakeVideo.setTextColor(getColor(R.color.galleryFontDark))
        } else {
            mTvTakeImage.setTextColor(getColor(R.color.galleryFontLight))
            mTvTakeVideo.setTextColor(getColor(R.color.galleryFontLight))
        }
    }

    /**
     * 设置空页面提示文字
     */
    override fun setMessage(message: Int) {
        mMessage.setText(message)
    }

    /**
     * 显示 / 隐藏 拍照按钮
     */
    override fun setMakeImageDisplay(display: Boolean) {
        mTakeImage.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 显示 / 隐藏 录像按钮
     */
    override fun setMakeVideoDisplay(display: Boolean) {
        mTakeVideo.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 点击拍照 / 录像，交给 Presenter 处理逻辑
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.layout_camera_image -> getPresenter()?.takePicture()
            R.id.layout_camera_video -> getPresenter()?.takeVideo()
        }
    }

}