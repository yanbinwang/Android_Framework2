package com.example.gallery.feature.album.app.album

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.example.gallery.feature.album.model.AlbumFolder
import com.example.gallery.feature.album.widget.recyclerview.OnItemClickListener
import com.example.gallery.feature.album.Album

/**
 * 文件夹选择弹窗 列表适配器
 * 功能：展示所有图片/视频文件夹，带单选、封面图、数量显示
 */
@SuppressLint("SetTextI18n")
class FolderAdapter(private val albumFolders: List<AlbumFolder>, private val colorStates: ColorStateList) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {
    // 条目点击回调
    private var mItemClickListener: OnItemClickListener? = null

    /**
     * 创建 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        // 内部处理单选逻辑（自动切换上一个/当前选中状态）
        return FolderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.album_item_dialog_folder, parent, false), colorStates, object :
            OnItemClickListener {
            private var oldPosition = 0

            override fun onItemClick(view: View?, position: Int) {
                // 回调外部点击
                mItemClickListener?.onItemClick(view, position)
                // 内部单选逻辑：取消上一个，选中当前
                val albumFolder = albumFolders[position]
                if (!albumFolder.isChecked) {
                    albumFolder.isChecked = true
                    albumFolders[oldPosition].isChecked = false
                    // 局部刷新，避免整个列表刷新
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(position)
                    oldPosition = position
                }
            }
        })
    }

    /**
     * 绑定数据
     */
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val newPosition = holder.getBindingAdapterPosition()
        holder.setData(albumFolders[newPosition])
    }

    /**
     * 条目数量
     */
    override fun getItemCount(): Int {
        return albumFolders.size
    }

    /**
     * 设置外部点击回调
     */
    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.mItemClickListener = itemClickListener
    }

    /**
     * ViewHolder 缓存
     */
    class FolderViewHolder(itemView: View, selector: ColorStateList, private val itemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        // 文件夹封面图
        private var mIvImage = itemView.findViewById<ImageView>(R.id.iv_gallery_preview_image)
        // 文件夹名称 + 文件数量
        private var mTvTitle = itemView.findViewById<TextView>(R.id.tv_gallery_preview_title)
        // 单选按钮
        private var mCheckBox = itemView.findViewById<RadioButton>(R.id.rb_gallery_preview_check)

        init {
            // 设置单选按钮颜色
            mCheckBox?.setButtonTintList(selector)
            // 设置点击事件
            clicks(itemView)
        }

        /**
         * 给控件设置数据
         */
        fun setData(albumFolder: AlbumFolder) {
            val albumFiles = albumFolder.albumFiles
            // 显示：(数量) 文件夹名称
            mTvTitle?.text = "(" + albumFiles.size + ") " + albumFolder.name
            // 选中状态
            mCheckBox?.setChecked(albumFolder.isChecked)
            // 加载文件夹第一张图作为封面
            Album.getAlbumConfig().albumLoader.load(mIvImage, albumFiles[0])
        }

        /**
         * 条目点击
         */
        override fun onClick(v: View?) {
            itemClickListener?.onItemClick(v, getBindingAdapterPosition())
        }

    }

}