package com.yanzhenjie.album.widget.photoview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * 绑定了 PhotoViewAttacher 的 ImageView
 * 作用：当图片改变时，自动通知缩放控制器刷新矩阵、缩放、位置信息
 */
class AttacherImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 图片缩放、手势、矩阵控制核心
    private var mAttacher: PhotoViewAttacher? = null

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

}