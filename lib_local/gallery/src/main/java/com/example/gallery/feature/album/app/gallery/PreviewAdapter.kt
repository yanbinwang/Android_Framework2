package com.example.gallery.feature.album.app.gallery

import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.example.gallery.feature.album.widget.photoview.AttacherImageView
import com.example.gallery.feature.album.widget.photoview.PhotoViewAttacher

/**
 * 图片预览适配器（给 ViewPager 用）
 * 基类适配器，专门用于预览大图，支持：
 * 点击、长按、缩放（PhotoView）子类只需要实现图片加载逻辑即可
 */
abstract class PreviewAdapter<T>(private val previewList: List<T>) : PagerAdapter(), PhotoViewAttacher.OnViewTapListener, OnLongClickListener {
    // 单击监听
    private var mItemClickListener: View.OnClickListener? = null
    // 长按监听
    private var mItemLongClickListener: View.OnClickListener? = null

    /**
     * 条目数量
     */
    override fun getCount(): Int {
        return previewList.size
    }

    /**
     * View 是否对应对象（固定写法）
     */
    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    /**
     * 创建预览页面
     * 1) 创建可缩放的 ImageView
     * 2) 绑定点击/长按
     * 3) 让子类去加载图片
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // 创建支持 PhotoView 的 ImageView
        val imageView = AttacherImageView(container.context)
        imageView.setLayoutParams(ViewGroup.LayoutParams(-1, -1))
        // 子类实现：加载图片
        loadPreview(imageView, previewList[position], position)
        // 添加到 ViewPager
        container.addView(imageView)
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
        imageView.setAttacher(attacher)
        return imageView
    }

    /**
     * 销毁页面
     */
    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
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
    protected abstract fun loadPreview(imageView: ImageView, item: T, position: Int)

}