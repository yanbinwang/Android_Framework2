package com.example.gallery.feature.album.widget.photoview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.common.utils.function.pt
import com.example.framework.utils.function.drawable

/**
 * 绑定了 PhotoViewAttacher 的 ImageView
 * 作用：当图片改变时，自动通知缩放控制器刷新矩阵、缩放、位置信息
 */
class AttacherImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 图片缩放、手势、矩阵控制核心
    private var mAttacher: PhotoViewAttacher? = null
    // 播放按钮
    private var mPlayIcon: Drawable? = null

    /**
     * 重写绘制方法,添加播放图标
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPlayIcon?.let { icon ->
            val left = (width - icon.intrinsicWidth) / 2
            val top = (height - icon.intrinsicHeight) / 2
            icon.setBounds(left, top, left + icon.intrinsicWidth, top + icon.intrinsicHeight)
            icon.draw(canvas)
        }
    }

    /**
     * 重写设置图片方法
     * 当图片改变时，自动通知 PhotoViewAttacher 刷新
     */
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mAttacher?.update()
    }

    /**
     * 绑定 PhotoView 控制器
     *
     * @param attacher 手势缩放、矩阵计算核心类
     */
    fun setAttacher(attacher: PhotoViewAttacher) {
        mAttacher = attacher
    }

    /**
     * 显示播放图标（只有图标不一样才刷新）
     */
    fun showPlayIcon(resId: Int) {
        val newIcon = context.drawable(resId)?.also { it.setBounds(0, 0, 40.pt, 40.pt) }
        // 状态没变 → 不执行任何操作
        if (mPlayIcon === newIcon) return
        mPlayIcon = newIcon
        invalidate()
    }

    /**
     * 隐藏播放图标（只有当前显示着才隐藏）
     */
    fun hidePlayIcon() {
        // 本来就是空 → 直接 return，不刷新、不赋值
        if (mPlayIcon == null) return
        mPlayIcon = null
        invalidate()
    }

}