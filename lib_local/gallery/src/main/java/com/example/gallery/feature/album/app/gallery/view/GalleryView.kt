package com.example.gallery.feature.album.app.gallery.view

import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.common.utils.function.openVideo
import com.example.common.widget.AppToolbar.Companion.KEY_RIGHT_TEXT
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.hideFadingEdge
import com.example.framework.utils.function.view.setOnPageChangeListener
import com.example.gallery.R
import com.example.gallery.feature.album.Album
import com.example.gallery.feature.album.adapter.PreviewAdapter
import com.example.gallery.feature.album.app.Contract
import com.example.gallery.feature.album.bean.AlbumFile
import com.example.gallery.feature.album.bean.Widget

/**
 * 图片/视频 预览页面 View 层
 * 功能：大图预览、选中/取消、视频时长显示、状态栏沉浸
 */
class GalleryView<Data>(activity: AppCompatActivity, presenter: Contract.GalleryPresenter) : Contract.GalleryView<Data>(activity, presenter), View.OnClickListener {
    // 右上角完成按钮
    private var mCompleteMenu: TextView? = null
    // 预览 ViewPager
    private val mViewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
    // 底部操作栏
    private val mLayoutMenu = activity.findViewById<RelativeLayout>(R.id.layout_menu)
    // 视频时长文字
    private val mTvDuration = activity.findViewById<TextView>(R.id.tv_duration)
    // 选择框
    private val mCheckBox = activity.findViewById<CheckBox>(R.id.check_box)
    // 顶层遮罩层（拦截点击事件）
    private val mLayoutLayer = activity.findViewById<FrameLayout>(R.id.layout_layer)
    // 标题栏
    private val mToolbar get() = getToolbar()

    init {
        // 设置选择框点击监听 / 遮罩层点击（拦截事件，不做处理）
        clicks(mCheckBox, mLayoutLayer)
    }

    /**
     * 初始化页面样式：状态栏、导航栏、选择框样式
     */
    override fun setupViews(widget: Widget, checkable: Boolean) {
        // 标题同步状态栏颜色
        mToolbar
            .setSecondaryTitle(tintColor = R.color.galleryIconLight, bgColor = R.color.albumGalleryBackground) {
                getPresenter().navigateBack()
            }
            .setRightText(getString(R.string.album_menu_finish), widget.getTextTintColor()) {
                getPresenter().complete()
            }
        // 创建右上角菜单（完成按钮）
        mCompleteMenu = mToolbar.findViewByKey<TextView>(KEY_RIGHT_TEXT)
        // 如果不可选，隐藏选择按钮和完成按钮
        if (!checkable) {
            mCompleteMenu.gone()
            mCheckBox.gone()
        } else {
            // 设置选择框样式
            val itemSelector = widget.mediaItemCheckSelector
            mCheckBox.buttonTintList = itemSelector
            mCheckBox.setTextColor(itemSelector)
        }
        // 页面滑动监听
        mViewPager.hideFadingEdge()
        mViewPager.setOnPageChangeListener(getObserver(), object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                getPresenter().onCurrentChanged(position)
            }
        })
    }

    /**
     * 切换到指定位置的图片
     */
    override fun setCurrentItem(position: Int) {
        mViewPager.currentItem = position
    }

    /**
     * 显示/隐藏视频时长
     */
    override fun setDurationDisplay(display: Boolean) {
        mTvDuration.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 设置视频时长
     */
    override fun setDuration(duration: String) {
        mTvDuration.text = duration
    }

    /**
     * 设置选择框状态
     */
    override fun setChecked(checked: Boolean) {
        mCheckBox.isChecked = checked
    }

    /**
     * 显示/隐藏底部栏
     */
    override fun setMenuDisplay(display: Boolean) {
        mLayoutMenu.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 显示/隐藏遮罩层
     */
    override fun setLayerDisplay(display: Boolean) {
        mLayoutLayer.visibility = if (display) View.VISIBLE else View.GONE
    }

    /**
     * 设置完成按钮文字
     */
    override fun setCompleteText(text: String) {
        mCompleteMenu?.text = text
    }

    /**
     * 绑定预览数据
     * 内部类实现 PreviewAdapter，加载图片
     */
    override fun bindData(dataList: List<Data>) {
        val adapter = object : PreviewAdapter<Data>(dataList) {
            override fun loadPreview(imageView: ImageView, item: Data, position: Int) {
                // 加载图片：支持 String 路径 或 AlbumFile
                if (item is String) {
                    Album.getAlbumConfig().albumLoader.load(imageView, item as String)
                } else if (item is AlbumFile) {
                    Album.getAlbumConfig().albumLoader.load(imageView, item as AlbumFile)
                }
            }
        }
        val itemClickAction = { isLongClick: Boolean ->
            val position = mViewPager.currentItem
            val item = dataList[position]
            if (item is AlbumFile && item.mediaType == AlbumFile.TYPE_VIDEO) {
                getContext().openVideo(item.path.orEmpty())
            } else {
                if (isLongClick) {
                    getPresenter().longClickItem(position)
                } else {
                    getPresenter().clickItem(position)
                }
            }
        }
        // 点击 -> 通知 Presenter
        adapter.setItemClickListener {
            itemClickAction(false)
        }
        // 长按 -> 通知 Presenter
        adapter.setItemLongClickListener {
            itemClickAction(true)
        }
        // 设置预加载数量
        if (adapter.itemCount > 3) {
            mViewPager.offscreenPageLimit = 3
        } else if (adapter.itemCount > 2) {
            mViewPager.offscreenPageLimit = 2
        }
        mViewPager.setAdapter(adapter)
    }

    /**
     * 点击事件：选择框
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.check_box -> getPresenter().onCheckedChanged()
            // 遮罩层只拦截事件，不做处理
            R.id.layout_layer -> {}
        }
    }

}