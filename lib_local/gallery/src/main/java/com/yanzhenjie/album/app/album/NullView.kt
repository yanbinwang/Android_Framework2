package com.yanzhenjie.album.app.album

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.example.gallery.base.BaseActivity.Companion.setSupportToolbar
import com.yanzhenjie.album.api.widget.Widget
import com.yanzhenjie.album.app.Contract
import com.example.gallery.R

/**
 * 空页面 View 层
 * 功能：当手机里没有图片/视频时，显示这个页面
 * 提供：拍照、录像按钮，纯 UI 展示
 */
class NullView(activity: Activity, presenter: Contract.NullPresenter) :
    Contract.NullView(activity, presenter), View.OnClickListener {
    // 标题栏
    private var mToolbar: Toolbar? = null
    // 标题文字
    private var mTitle: TextView? = null
    // 空页面提示文字
    private var mTvMessage: TextView? = null
    // 拍照按钮
    private var mBtnTakeImage: AppCompatButton? = null
    // 录像按钮
    private var mBtnTakeVideo: AppCompatButton? = null

    /**
     * 构造方法：绑定控件
     */
    init {
        // 设置Toolbar
        mToolbar = activity.findViewById(R.id.toolbar)
        setSupportToolbar(mToolbar)
        mTitle = activity.findViewById(R.id.tv_title)
        mTvMessage = activity.findViewById(R.id.tv_message)
        mBtnTakeImage = activity.findViewById(R.id.btn_camera_image)
        mBtnTakeVideo = activity.findViewById(R.id.btn_camera_video)
        // 按钮点击事件
        mBtnTakeImage?.setOnClickListener(this)
        mBtnTakeVideo?.setOnClickListener(this)
    }

    /**
     * 初始化页面样式（颜色、主题、图标）
     */
    override fun setupViews(widget: Widget) {
        TODO("Not yet implemented")
    }

    override fun setMessage(message: Int) {
        TODO("Not yet implemented")
    }

    override fun setMakeImageDisplay(display: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setMakeVideoDisplay(display: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

}