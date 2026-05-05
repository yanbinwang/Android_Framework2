package com.example.gallery.feature.album.app.album.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.common.utils.function.pt
import com.example.common.widget.AppToolbar
import com.example.common.widget.AppToolbar.Companion.KEY_RIGHT_ICON
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.paddingAll
import com.example.framework.utils.function.view.visible
import com.example.gallery.R
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.adapter.AlbumAdapter
import com.example.gallery.feature.album.app.Contract
import com.example.gallery.feature.album.bean.AlbumFolder
import com.example.gallery.feature.album.bean.Widget
import com.example.gallery.feature.album.widget.recyclerview.OnCheckedClickListener
import com.example.gallery.feature.album.widget.recyclerview.OnItemClickListener
import com.example.gallery.feature.album.widget.recyclerview.divider.ItemDivider
import com.example.gallery.widget.ColorProgressBar

/**
 * 相册主页面 View 层
 * 功能：负责所有 UI 展示、事件点击、列表刷新、主题切换
 */
@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class AlbumView(activity: AppCompatActivity, presenter: Contract.AlbumPresenter) : Contract.AlbumView(activity, presenter), View.OnClickListener {
    // 相册列表适配器
    private var mAdapter: AlbumAdapter? = null
    // 右上角完成菜单
    private var mCompleteMenu: ImageView? = null
    // 标题栏
    private val mToolbar = activity.findViewById<AppToolbar>(R.id.toolbar)
    // 相册列表
    private val mRecyclerView = activity.findViewById<RecyclerView>(R.id.recycler_view)
    // 切换文件夹按钮
    private val mSwitchFolder = activity.findViewById<LinearLayout>(R.id.layout_switch_dir)
    private val mTvSwitchFolder = activity.findViewById<TextView>(R.id.tv_switch_dir)
    // 预览按钮
    private val mPreview = activity.findViewById<LinearLayout>(R.id.layout_preview)
    private val mTvPreview = activity.findViewById<TextView>(R.id.tv_preview)
    // 加载中布局
    private val mLayoutLoading = activity.findViewById<LinearLayout>(R.id.layout_loading)
    // 加载进度条
    private val mProgressBar = activity.findViewById<ColorProgressBar>(R.id.progress_bar)

    /**
     * 构造方法：绑定控件 + 设置点击事件
     */
    init {
        // 点击标题栏 → 回到顶部 / 切换文件夹 / 预览已选图片
        clicks(mToolbar, mSwitchFolder, mPreview)
    }

    /**
     * 初始化页面样式：主题、颜色、列表、适配器
     */
    override fun setupViews(widget: Widget, column: Int, hasCamera: Boolean, choiceMode: Int) {
        // 浅色 / 深色主题 -> 影响图标
        if (widget.uiStyle == Widget.STYLE_LIGHT) {
            mProgressBar.setColorFilter(getColor(R.color.albumLoading))
        } else {
            mProgressBar.setColorFilter(getColor(widget.statusBarColor))
        }
        // 标题同步状态栏颜色
        mToolbar
            .setTitle(widget.title, widget.getTextTintColor(), widget.statusBarColor)
            .setLeftButton(tintColor = widget.getIconTintColor()) {
                getPresenter().navigateBack()
            }
        // 单选模式隐藏预览按钮
        if (choiceMode == Album.MODE_SINGLE) {
            mPreview.gone()
        } else {
            mPreview.visible()
            // 创建右上角菜单（完成按钮）
            mToolbar.setRightButton(R.mipmap.gallery_ic_done, widget.getIconTintColor()) {
                getPresenter().complete()
            }
            mCompleteMenu = mToolbar.findViewByKey<ImageView>(KEY_RIGHT_ICON)
        }
        // 配置网格布局（横竖屏切换）
        val mLayoutManager = GridLayoutManager(getContext(), column, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.setLayoutManager(mLayoutManager)
        // 设置列表间隔
        val dividerSize = 4.pt
        mRecyclerView.addItemDecoration(ItemDivider(Color.TRANSPARENT, dividerSize, dividerSize))
        mRecyclerView.paddingAll(2.pt)
        // 初始化适配器
        mAdapter = AlbumAdapter(hasCamera, choiceMode, widget.mediaItemCheckSelector)
        // 点击拍照
        mAdapter?.setAddClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                getPresenter().clickCamera(view)
            }
        })
        // 点击选择框
        mAdapter?.setCheckedClickListener(object : OnCheckedClickListener {
            override fun onCheckedClick(button: CompoundButton?, position: Int) {
                getPresenter().tryCheckItem(button, position)
            }
        })
        // 点击预览图片
        mAdapter?.setItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                getPresenter().tryPreviewItem(position)
            }
        })
        mRecyclerView.setAdapter(mAdapter)
    }

    /**
     * 显示/隐藏加载动画
     */
    override fun setLoadingDisplay(display: Boolean) {
        if (display) {
            mLayoutLoading.visible()
            mProgressBar.isIndeterminate = true
        } else {
            mLayoutLoading.fade {
                mProgressBar.isIndeterminate = false
            }
        }
    }

    /**
     * 显示/隐藏右上角完成按钮
     */
    override fun setCompleteDisplay(display: Boolean) {
        mCompleteMenu?.isVisible = display
    }

    /**
     * 绑定文件夹数据：刷新列表
     */
    override fun bindAlbumFolder(albumFolder: AlbumFolder) {
        mTvSwitchFolder.text = albumFolder.name
        mAdapter?.setAlbumFiles(albumFolder.albumFiles)
        mAdapter?.notifyDataSetChanged()
        mRecyclerView.scrollToPosition(0)
    }

    /**
     * 插入条目（拍照后添加图片）
     */
    override fun notifyInsertItem(position: Int) {
        mAdapter?.notifyItemInserted(position)
    }

    /**
     * 刷新单个条目
     */
    override fun notifyItem(position: Int) {
        mAdapter?.notifyItemChanged(position)
    }

    /**
     * 更新预览按钮上的选中数量 (5)
     */
    override fun setCheckedCount(count: Int) {
        mTvPreview.text = " ($count)"
    }

    /**
     * 点击事件
     */
    override fun onClick(v: View?) {
        when (v) {
            mToolbar -> mRecyclerView.smoothScrollToPosition(0)
            mSwitchFolder -> getPresenter().clickFolderSwitch()
            mPreview -> getPresenter().tryPreviewChecked()
        }
    }

}