package com.example.gallery.feature.album.app.gallery

import android.os.Bundle
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentBoolean
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentParcelableArrayList
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.Contract
import com.example.gallery.feature.album.callback.Action
import com.example.gallery.feature.album.callback.ItemAction
import com.example.gallery.feature.album.model.AlbumFile
import com.example.gallery.feature.album.model.Widget
import com.example.gallery.feature.album.utils.AlbumUtil

/**
 * 外部媒体预览
 * 功能：处理所有预览页的业务逻辑（选中、切换、完成、回调）
 */
internal class GalleryAlbumActivity : BaseActivity(), Contract.GalleryPresenter {
    // 当前预览位置
    private var mCurrentPosition = 0
    // 是否可以选中（勾选）
    private val mCheckable by lazy { intentBoolean(Album.KEY_INPUT_GALLERY_CHECKABLE) }
    // 要预览的图片/视频列表
    private val mAlbumFiles by lazy { intentParcelableArrayList<AlbumFile>(Album.KEY_INPUT_CHECKED_LIST) ?: arrayListOf() }
    // 主题样式
    private val mWidget by lazy { intentParcelable<Widget>(Album.KEY_INPUT_WIDGET) ?: Widget.getDefaultWidget(this) }
    // MVP 的 View 层（负责UI）
    private val mView by lazy { GalleryView<AlbumFile>(this, this) }

    companion object {
        // 外部设置的静态监听：点击、长按、取消、选择结果
        var sClick: ItemAction<AlbumFile>? = null
        var sLongClick: ItemAction<AlbumFile>? = null
        var sCancel: Action<String>? = null
        var sResult: Action<ArrayList<AlbumFile>>? = null
    }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 校验参数
        if (!hasExtras()) return finish()
        setContentView(R.layout.album_activity_gallery)
        // 导航栏
        initImmersionBar(false, false, R.color.albumPrimary)
        // 获取一次默认选中状态
        mCurrentPosition = intentInt(Album.KEY_INPUT_CURRENT_POSITION)
        // 绑定 MVP
        mView.setupViews(mWidget, mCheckable)
        mView.bindData(mAlbumFiles)
        // 显示当前预览位置
        if (mCurrentPosition == 0) {
            onCurrentChanged(mCurrentPosition)
        } else {
            mView.setCurrentItem(mCurrentPosition)
        }
        // 设置右上角完成按钮的文字（显示选中数量）
        setCheckedCount()
        // 返回按钮逻辑
        setOnBackPressedListener {
            sCancel?.onAction("User canceled.")
            finish()
        }
    }

    /**
     * 点击图片
     */
    override fun clickItem(position: Int) {
        sClick?.onAction(this, mAlbumFiles[mCurrentPosition])
    }

    /**
     * 长按图片
     */
    override fun longClickItem(position: Int) {
        sLongClick?.onAction(this, mAlbumFiles[mCurrentPosition])
    }

    /**
     * 滑动切换图片时回调（核心逻辑）
     */
    override fun onCurrentChanged(position: Int) {
        mCurrentPosition = position
        val albumFile = mAlbumFiles[position]
        // 同步勾选状态
        if (mCheckable) {
            mView.setChecked(albumFile.isChecked)
        }
        // 如果是不可用的文件，显示遮罩
        mView.setLayerDisplay(albumFile.isDisable)
        // 如果是视频 → 显示时长
        if (albumFile.mediaType == AlbumFile.TYPE_VIDEO) {
            if (!mCheckable) {
                mView.setBottomDisplay(true)
            }
            mView.setDuration(AlbumUtil.convertDuration(albumFile.duration))
            mView.setDurationDisplay(true)
            // 图片 → 隐藏视频时长
        } else {
            if (!mCheckable) {
                mView.setBottomDisplay(false)
            }
            mView.setDurationDisplay(false)
        }
    }

    /**
     * 点击勾选框，切换选中状态
     */
    override fun onCheckedChanged() {
        val albumFile = mAlbumFiles[mCurrentPosition]
        // 切换状态
        albumFile.isChecked = !albumFile.isChecked
        // 更新按钮文字
        setCheckedCount()
    }

    /**
     * 点击完成，返回选中结果
     */
    override fun complete() {
        val checkedList = ArrayList<AlbumFile>()
        for (albumFile in mAlbumFiles) {
            if (albumFile.isChecked) {
                checkedList.add(albumFile)
            }
        }
        sResult?.onAction(checkedList)
        finish()
    }

    /**
     * 计算已选中数量，更新完成按钮文字
     */
    private fun setCheckedCount() {
        var checkedCount = 0
        for (albumFile in mAlbumFiles) {
            if (albumFile.isChecked) {
                checkedCount += 1
            }
        }
        var completeText = getString(R.string.album_menu_finish)
        completeText += "(" + checkedCount + " / " + mAlbumFiles.size + ")"
        mView.setCompleteText(completeText)
    }

    /**
     * 页面销毁，清空所有静态监听（防止内存泄漏）
     */
    override fun finish() {
        sResult = null
        sCancel = null
        sClick = null
        sLongClick = null
        super.finish()
    }

}