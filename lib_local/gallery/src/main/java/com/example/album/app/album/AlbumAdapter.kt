package com.example.album.app.album

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.example.album.Album
import com.example.album.Album.getAlbumConfig
import com.example.album.model.AlbumFile
import com.example.album.utils.AlbumUtil.convertDuration
import com.example.album.widget.recyclerview.OnCheckedClickListener
import com.example.album.widget.recyclerview.OnItemClickListener

/**
 * 相册主列表适配器
 * 三种类型：拍照按钮 / 图片 / 视频
 * 支持：单选、多选、预览、选择、禁用遮罩
 */
class AlbumAdapter(private val hasCamera: Boolean, private val choiceMode: Int, private val selector: ColorStateList) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 媒体文件列表
    private var mAlbumFiles = ArrayList<AlbumFile>()
    // 三种点击回调
    private var mAddPhotoClickListener: OnItemClickListener? = null // 点击拍照
    private var mItemClickListener: OnItemClickListener? = null // 点击预览
    private var mCheckedClickListener: OnCheckedClickListener? = null // 点击选择框
    // 相机下标需要重新计算
    private val cameraOffset get() = if (hasCamera) 1 else 0

    companion object {
        // 三种条目类型
        private const val TYPE_BUTTON = 1 // 拍照按钮
        private const val TYPE_IMAGE = 2 // 图片
        private const val TYPE_VIDEO = 3 // 视频
    }

    /**
     * 创建三种不同的 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BUTTON -> {
                ButtonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.album_item_content_button, parent, false), mAddPhotoClickListener)
            }
            TYPE_IMAGE -> {
                ImageHolder(LayoutInflater.from(parent.context).inflate(R.layout.album_item_content_image, parent, false), hasCamera, mItemClickListener, mCheckedClickListener).also {
                    // 多选模式显示选择框
                    if (choiceMode == Album.MODE_MULTIPLE) {
                        it.mCheckBox.visibility = View.VISIBLE
                        it.mCheckBox.setButtonTintList(selector)
                        it.mCheckBox.setTextColor(selector)
                    } else {
                        it.mCheckBox.visibility = View.GONE
                    }
                }
            }
            TYPE_VIDEO -> {
                VideoHolder(LayoutInflater.from(parent.context).inflate(R.layout.album_item_content_video, parent, false), hasCamera, mItemClickListener, mCheckedClickListener).also {
                    if (choiceMode == Album.MODE_MULTIPLE) {
                        it.mCheckBox.visibility = View.VISIBLE
                        it.mCheckBox.setButtonTintList(selector)
                        it.mCheckBox.setTextColor(selector)
                    } else {
                        it.mCheckBox.visibility = View.GONE
                    }
                }
            }
            else -> throw AssertionError("This should not be the case.")
        }
    }

    /**
     * 绑定数据
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_BUTTON -> {}
            // 图片/视频统一处理
            TYPE_IMAGE, TYPE_VIDEO -> {
                val mediaHolder = holder as? MediaViewHolder
                val mPosition = holder.getBindingAdapterPosition() - cameraOffset
                val albumFile = mAlbumFiles[mPosition]
                mediaHolder?.setData(albumFile)
            }
            else -> throw AssertionError("This should not be the case.")
        }
    }

    /**
     * 条目总数 = 拍照(1) + 媒体数量
     */
    override fun getItemCount(): Int {
        return mAlbumFiles.size + cameraOffset
    }

    /**
     * 根据位置返回条目类型
     * 0号位置：拍照按钮（如果开启）
     * 其他位置：图片/视频
     */
    override fun getItemViewType(position: Int): Int {
        if (position == 0) return if (hasCamera) TYPE_BUTTON else TYPE_IMAGE
        val pos = position - if (hasCamera) 1 else 0
        return if (mAlbumFiles[pos].mediaType == AlbumFile.TYPE_VIDEO) TYPE_VIDEO else TYPE_IMAGE
    }

    /**
     * 设置数据
     */
    fun setAlbumFiles(albumFiles: ArrayList<AlbumFile>) {
        this.mAlbumFiles = albumFiles
    }

    /**
     * 各种点击事件
     */
    fun setAddClickListener(addPhotoClickListener: OnItemClickListener) {
        this.mAddPhotoClickListener = addPhotoClickListener
    }

    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.mItemClickListener = itemClickListener
    }

    fun setCheckedClickListener(checkedClickListener: OnCheckedClickListener) {
        this.mCheckedClickListener = checkedClickListener
    }

    /**
     * 拍照按钮
     */
    class ButtonViewHolder(itemView: View, private val mItemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            clicks(itemView)
        }

        override fun onClick(v: View?) {
            if (v == itemView) {
                mItemClickListener?.onItemClick(v, 0)
            }
        }
    }

    /**
     * 图片条目
     */
    class ImageHolder(itemView: View, private val hasCamera: Boolean, private val mItemClickListener: OnItemClickListener?, private val mCheckedClickListener: OnCheckedClickListener?) : MediaViewHolder(itemView), View.OnClickListener {
        val mIvImage = itemView.findViewById<ImageView>(R.id.iv_album_content_image)
        val mCheckBox = itemView.findViewById<CheckBox>(R.id.check_box)
        val mLayoutLayer = itemView.findViewById<FrameLayout>(R.id.layout_layer)

        init {
            clicks(itemView, mCheckBox, mLayoutLayer)
        }

        override fun setData(albumFile: AlbumFile) {
            mCheckBox.isChecked = albumFile.isChecked
            // 加载缩略图
            getAlbumConfig().albumLoader.load(mIvImage, albumFile)
            // 禁用文件显示遮罩
            mLayoutLayer.visibility = if (albumFile.isDisable) View.VISIBLE else View.GONE
        }

        override fun onClick(v: View?) {
            val pos = getBindingAdapterPosition() - (if (hasCamera) 1 else 0)
            when (v) {
                itemView -> mItemClickListener?.onItemClick(v, pos)
                mCheckBox -> mCheckedClickListener?.onCheckedClick(mCheckBox, pos)
                mLayoutLayer -> mItemClickListener?.onItemClick(v, pos)
            }
        }
    }

    /**
     * 视频条目（带时长）
     */
    class VideoHolder(itemView: View, private val hasCamera: Boolean, private val mItemClickListener: OnItemClickListener?, private val mCheckedClickListener: OnCheckedClickListener?) : MediaViewHolder(itemView), View.OnClickListener {
        val mIvImage = itemView.findViewById<ImageView>(R.id.iv_album_content_image)
        val mCheckBox = itemView.findViewById<CheckBox>(R.id.check_box)
        val mTvDuration = itemView.findViewById<TextView>(R.id.tv_duration)
        val mLayoutLayer = itemView.findViewById<FrameLayout>(R.id.layout_layer)

        init {
            clicks(itemView, mCheckBox, mLayoutLayer)
        }

        override fun setData(albumFile: AlbumFile) {
            getAlbumConfig().albumLoader.load(mIvImage, albumFile)
            mCheckBox.isChecked = albumFile.isChecked
            mTvDuration.text = convertDuration(albumFile.duration)
            mLayoutLayer.visibility = if (albumFile.isDisable) View.VISIBLE else View.GONE
        }

        override fun onClick(v: View?) {
            val pos = getBindingAdapterPosition() - (if (hasCamera) 1 else 0)
            when (v) {
                itemView -> mItemClickListener?.onItemClick(v, pos)
                mCheckBox -> mCheckedClickListener?.onCheckedClick(mCheckBox, pos)
                mLayoutLayer -> mItemClickListener?.onItemClick(v, pos)
            }
        }
    }

    /**
     * 抽象基类：统一图片/视频
     */
    abstract class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun setData(albumFile: AlbumFile)
    }

}