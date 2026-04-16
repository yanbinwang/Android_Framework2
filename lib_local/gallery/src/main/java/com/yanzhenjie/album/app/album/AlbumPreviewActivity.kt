package com.yanzhenjie.album.app.album

import android.os.Bundle
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.app.Contract.GalleryPresenter
import com.yanzhenjie.album.app.gallery.GalleryView
import com.yanzhenjie.album.model.AlbumFile
import com.yanzhenjie.album.model.Widget
import com.yanzhenjie.album.utils.AlbumUtil

/**
 * 相册内部选择预览（带勾选）
 * 功能：选择控制、数量限制、预览切换、完成返回
 */
class AlbumPreviewActivity : BaseActivity(), GalleryPresenter {
    // 功能类型：图片 / 视频 / 全部
    private val mFunction by lazy { intentInt(Album.KEY_INPUT_FUNCTION) }
    // 最大可选数量
    private val mAllowSelectCount by lazy { intentInt(Album.KEY_INPUT_LIMIT_COUNT) }
    // 主题样式
    private val mWidget by lazy { intentParcelable<Widget>(Album.KEY_INPUT_WIDGET) }
    // MVP View 层
    private val mView by lazy { GalleryView<AlbumFile>(this, this) }

    companion object {
        // 静态全局数据（跨页面传递）
        @JvmField
        var sCheckedCount = 0 // 已选数量
        @JvmField
        var sCurrentPosition = 0 // 当前预览位置
        @JvmField
        var sAlbumFiles: ArrayList<AlbumFile>? = null // 预览列表
        @JvmField
        var sCallback: Callback? = null // 预览回调
    }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasExtras()) return finish()
        setContentView(R.layout.album_activity_gallery)
        // 导航栏
        initImmersionBar(false, false, R.color.albumGalleryPrimary)
        // 绑定 MVP
        mView.setupViews(mWidget, true)
        mView.bindData(sAlbumFiles)
        // 定位到当前预览位置
        if (sCurrentPosition == 0) {
            onCurrentChanged(sCurrentPosition)
        } else {
            mView.setCurrentItem(sCurrentPosition)
        }
        // 设置右上角完成按钮文字
        setCheckedCount()
        // 返回逻辑
        setOnBackPressedListener {
            finish()
        }
    }

    /**
     * 点击图片（这里空实现，没有使用）
     */
    override fun clickItem(position: Int) {
    }

    /**
     * 长按图片（这里空实现，没有使用）
     */
    override fun longClickItem(position: Int) {
    }

    /**
     * 滑动切换图片时更新 UI
     */
    override fun onCurrentChanged(position: Int) {
        sCurrentPosition = position
        val albumFile = sAlbumFiles?.get(position) ?: return
        // 同步勾选状态
        mView.setChecked(albumFile.isChecked)
        // 不可用文件显示遮罩
        mView.setLayerDisplay(albumFile.isDisable)
        // 视频 → 显示时长
        if (albumFile.mediaType == AlbumFile.TYPE_VIDEO) {
            mView.setDuration(AlbumUtil.convertDuration(albumFile.duration))
            mView.setDurationDisplay(true)
        } else {
            mView.setDurationDisplay(false)
        }
    }

    /**
     * 点击勾选框：切换选中状态（带数量限制）
     */
    override fun onCheckedChanged() {
        val albumFile = sAlbumFiles?.get(sCurrentPosition) ?: return
        // 取消选中
        if (albumFile.isChecked) {
            albumFile.isChecked = false
            sCallback?.onPreviewChanged(albumFile)
            sCheckedCount--
            // 选中
        } else {
            // 超过最大数量 → 提示
            if (sCheckedCount >= mAllowSelectCount) {
                val messageRes = when (mFunction) {
                    Album.FUNCTION_CHOICE_IMAGE -> R.string.album_check_image_limit
                    Album.FUNCTION_CHOICE_VIDEO -> R.string.album_check_video_limit
                    Album.FUNCTION_CHOICE_ALBUM -> R.string.album_check_album_limit
                    else -> throw java.lang.AssertionError("This should not be the case.")
                }
                mView.toast(getString(messageRes, mAllowSelectCount))
                mView.setChecked(false)
                // 没超数量 → 选中
            } else {
                albumFile.isChecked = true
                sCallback?.onPreviewChanged(albumFile)
                sCheckedCount++
            }
        }
        // 更新按钮文字
        setCheckedCount()
    }

    /**
     * 点击完成按钮（必须选至少一个）
     */
    override fun complete() {
        if (sCheckedCount == 0) {
            val messageRes = when (mFunction) {
                Album.FUNCTION_CHOICE_IMAGE -> R.string.album_check_image_little
                Album.FUNCTION_CHOICE_VIDEO -> R.string.album_check_video_little
                Album.FUNCTION_CHOICE_ALBUM -> R.string.album_check_album_little
                else -> throw AssertionError("This should not be the case.")
            }
            mView.toast(messageRes)
        } else {
            sCallback?.onPreviewComplete()
            finish()
        }
    }

    /**
     * 更新完成按钮文字：已选 / 最大数量
     */
    private fun setCheckedCount() {
        var completeText = getString(R.string.album_menu_finish)
        completeText += "($sCheckedCount / $mAllowSelectCount)"
        mView.setCompleteText(completeText)
    }

    /**
     * 页面销毁：清空静态变量 → 防止内存泄漏
     */
    override fun finish() {
        sCheckedCount = 0
        sCurrentPosition = 0
        sAlbumFiles = null
        sCallback = null
        super.finish()
    }

    /**
     * 预览回调接口
     */
    interface Callback {
        /**
         * 完成选择
         */
        fun onPreviewComplete()

        /**
         * 选中/取消
         */
        fun onPreviewChanged(albumFile: AlbumFile)
    }

}