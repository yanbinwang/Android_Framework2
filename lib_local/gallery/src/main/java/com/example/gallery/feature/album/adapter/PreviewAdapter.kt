package com.example.gallery.feature.album.adapter

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.R
import com.example.gallery.feature.album.bean.AlbumFile
import com.example.gallery.feature.album.widget.photoview.AttacherImageView
import com.example.gallery.feature.album.widget.photoview.PhotoViewAttacher

/**
 * 图片预览适配器
 * 基类适配器，专门用于预览大图，支持：
 * 点击、长按、缩放（PhotoView）子类只需要实现图片加载逻辑即可
 */
abstract class PreviewAdapter<T>(private val previewList: List<T>) : RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder>(), PhotoViewAttacher.OnViewTapListener, View.OnLongClickListener {
    // 单击监听
    private var mItemClickListener: View.OnClickListener? = null
    // 长按监听
    private var mItemLongClickListener: View.OnClickListener? = null

    /**
     * 条目数量
     */
    override fun getItemCount(): Int {
        return previewList.size
    }

    /**
     * 创建预览页面
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        return PreviewViewHolder(AttacherImageView(parent.context))
    }

    /**
     * 帮你预览页数据
     */
    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        // 获取支持 PhotoView 的 ImageView
        val imageView = holder.itemView as AttacherImageView
        // 获取此次加载使用的数据体
        val item = previewList[position]
        // 每次复用时先重置点击，防止旧事件干扰
        imageView.setOnClickListener(null)
        imageView.setOnLongClickListener(null)
        // 子类实现：加载图片
        loadPreview(imageView, item, position)
        // 视频单独处理
        if (item is AlbumFile && item.mediaType == AlbumFile.TYPE_VIDEO) {
            imageView.showPlayIcon(R.mipmap.album_ic_video_gallery)
            if (mItemClickListener != null) {
                imageView.setOnClickListener { v ->
                    mItemClickListener?.onClick(v)
                }
            }
            if (mItemLongClickListener != null) {
                imageView.setOnLongClickListener { v ->
                    mItemLongClickListener?.onClick(v)
                    true
                }
            }
        } else {
            // 避免缓存误加载
            imageView.hidePlayIcon()
            // 绑定 PhotoView 缩放能力
            val attacher = PhotoViewAttacher(imageView)
            // 设置单击
            if (mItemClickListener != null) {
                attacher.setOnViewTapListener(this)
            }
            // 设置长按
            if (mItemLongClickListener != null) {
                attacher.setOnLongClickListener(this)
            }
            // 设置手势控制器
            imageView.setAttacher(attacher)
        }
    }

    /**
     * 单击回调
     */
    override fun onViewTap(v: View?, x: Float, y: Float) {
        mItemClickListener?.onClick(v)
    }

    /**
     * 长按回调
     */
    override fun onLongClick(v: View?): Boolean {
        mItemLongClickListener?.onClick(v)
        return true
    }

    /**
     * 设置单击监听
     */
    fun setItemClickListener(onClickListener: View.OnClickListener) {
        mItemClickListener = onClickListener
    }

    /**
     * 设置长按监听
     */
    fun setItemLongClickListener(longClickListener: View.OnClickListener) {
        mItemLongClickListener = longClickListener
    }

    /**
     * 子类必须实现：加载图片
     */
    abstract fun loadPreview(imageView: ImageView, item: T, position: Int)

    /**
     * 加载holder整体
     */
    class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

}