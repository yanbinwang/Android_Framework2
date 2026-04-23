package com.example.gallery.feature.album.app.gallery

import android.os.Bundle
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentBoolean
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentStringArrayList
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.app.Contract
import com.example.gallery.feature.album.callback.Action
import com.example.gallery.feature.album.callback.ItemAction
import com.example.gallery.feature.album.model.Widget

/**
 * 纯图片路径预览
 * 功能：只预览图片路径（String），不处理 AlbumFile 与 GalleryAlbumActivity 逻辑一致，仅数据类型不同
 */
internal class GalleryActivity : BaseActivity(), Contract.GalleryPresenter {
    // 当前预览位置
    private var mCurrentPosition = 0
    // 是否可选中
    private val mCheckable by lazy { intentBoolean(Album.KEY_INPUT_GALLERY_CHECKABLE) }
    // 图片路径列表
    private val mPathList by lazy { intentStringArrayList(Album.KEY_INPUT_CHECKED_LIST) }
    // 主题样式
    private val mWidget by lazy { intentParcelable<Widget>(Album.KEY_INPUT_WIDGET) ?: Widget.getDefaultWidget(this) }
    // 记录选中状态（路径 -> 是否选中）
    private val mCheckedMap by lazy { HashMap<String, Boolean>() }
    // MVP View 层
    private val mView by lazy { GalleryView<String>(this, this) }

    companion object {
        // 外部回调监听
        var sClick: ItemAction<String>? = null
        var sLongClick: ItemAction<String>? = null
        var sCancel: Action<String>? = null
        var sResult: Action<ArrayList<String>>? = null
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
        // 初始化 MVP
        mView.setupViews(mWidget, mCheckable)
        // 初始化选中状态：全部默认选中
        for (path in mPathList) {
            mCheckedMap[path] = true
        }
        // 不可选时隐藏底部栏
        if (!mCheckable) {
            mView.setMenuDisplay(false)
        }
        mView.setLayerDisplay(false)
        mView.setDurationDisplay(false)
        // 绑定数据
        mView.bindData(mPathList)
        // 定位到当前位置
        if (mCurrentPosition == 0) {
            onCurrentChanged(mCurrentPosition)
        } else {
            mView.setCurrentItem(mCurrentPosition)
        }
        // 更新完成按钮文字
        setCheckedCount()
        // 返回按钮监听
        setOnBackPressedListener {
            sCancel?.onAction("User canceled.")
            finish()
        }
    }

    /**
     * 点击图片
     */
    override fun clickItem(position: Int) {
        sClick?.onAction(this, mPathList[mCurrentPosition])
    }

    /**
     * 长按图片
     */
    override fun longClickItem(position: Int) {
        sLongClick?.onAction(this, mPathList[mCurrentPosition])
    }

    /**
     * 滑动切换图片
     */
    override fun onCurrentChanged(position: Int) {
        mCurrentPosition = position
        if (mCheckable) {
            mView.setChecked(mCheckedMap[mPathList[position]] == true)
        }
    }

    /**
     * 切换选中状态
     */
    override fun onCheckedChanged() {
        val path = mPathList[mCurrentPosition]
        mCheckedMap[path] = (mCheckedMap[path] == false)
        setCheckedCount()
    }

    /**
     * 完成选择，返回结果
     */
    override fun complete() {
        val checkedList = ArrayList<String>()
        for (entry in mCheckedMap.entries) {
            if (entry.value) {
                checkedList.add(entry.key)
            }
        }
        sResult?.onAction(checkedList)
        finish()
    }

    /**
     * 计算选中数量，更新按钮文字
     */
    private fun setCheckedCount() {
        var checkedCount = 0
        for (entry in mCheckedMap.entries) {
            if (entry.value) {
                checkedCount += 1
            }
        }
        var completeText = getString(R.string.album_menu_finish)
        completeText += "(" + checkedCount + " / " + mPathList.size + ")"
        mView.setCompleteText(completeText)
    }

    /**
     * 页面销毁，清空监听防止内存泄漏
     */
    override fun finish() {
        sResult = null
        sCancel = null
        sClick = null
        sLongClick = null
        super.finish()
    }

}