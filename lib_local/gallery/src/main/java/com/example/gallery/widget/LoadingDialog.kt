package com.example.gallery.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.gallery.R

/**
 * 相册专用加载对话框
 * 用途：扫描图片、加载视频时显示的等待弹窗
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.Gallery_Dialog) {
    // 加载进度条
    private var mProgressBar: ColorProgressBar? = null
    // 加载提示文字
    private var mTvMessage: TextView? = null

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { _: DialogInterface?, _: Int, _: KeyEvent? -> true }
        setContentView(R.layout.gallery_dialog_loading)
        mProgressBar = findViewById(R.id.progress_bar)
        mTvMessage = findViewById(R.id.tv_message)
    }

    /**
     * 根据主题配置 加载条颜色（亮色/暗色模式）
     */
    fun setupViews(@ColorInt color: Int, @StringRes message: Int) {
        mProgressBar?.setColorFilter(color)
        setMessage(message)
    }

    /**
     * 设置提示文字（资源ID）
     */
    fun setMessage(@StringRes message: Int) {
        mTvMessage?.setText(message)
    }

    /**
     * 设置提示文字（字符串）
     */
    fun setMessage(message: String) {
        mTvMessage?.text = message
    }

    /**
     * 转圈开启/停止
     */
    override fun show() {
        super.show()
        mProgressBar?.isIndeterminate = true
    }

    override fun dismiss() {
        super.dismiss()
        mProgressBar?.isIndeterminate = false
    }

}