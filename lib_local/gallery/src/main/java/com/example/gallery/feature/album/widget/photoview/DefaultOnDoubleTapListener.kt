package com.example.gallery.feature.album.widget.photoview

import android.view.GestureDetector
import android.view.MotionEvent

/**
 * PhotoView 默认的双击事件监听器
 * 负责处理：单击确认、双击缩放、点击图片/点击外部事件
 * 可通过 setOnDoubleTapListener 覆盖自定义逻辑
 */
class DefaultOnDoubleTapListener(private val photoViewAttacher: PhotoViewAttacher?) : GestureDetector.OnDoubleTapListener {

    /**
     * 双击事件
     * 作用：实现 双击放大 → 再双击放大 → 再双击还原 的循环逻辑
     */
    override fun onDoubleTap(ev: MotionEvent): Boolean {
        if (photoViewAttacher == null) return false
        try {
            // 获取当前缩放值
            val scale = photoViewAttacher.getScale()
            // 获取双击坐标（以该点为中心缩放）
            val x = ev.x
            val y = ev.y
            // 三级缩放逻辑
            if (scale < photoViewAttacher.getMediumScale()) {
                // 当前 < 中等 → 缩放到中等
                photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true)
            } else if (scale >= photoViewAttacher.getMediumScale() && scale < photoViewAttacher.getMaximumScale()) {
                // 当前在中等与最大之间 → 缩放到最大
                photoViewAttacher.setScale(photoViewAttacher.getMaximumScale(), x, y, true)
            } else {
                // 当前 >= 最大 → 缩放到最小（还原）
                photoViewAttacher.setScale(photoViewAttacher.getMinimumScale(), x, y, true)
            }
        } catch (_: ArrayIndexOutOfBoundsException) {
            // 部分机型/版本在获取坐标时可能触发的系统BUG，直接忽略防崩溃
        }
        return true
    }

    /**
     * 双击过程中的触摸事件（按下/抬起）
     * 这里不需要处理，交给 onDoubleTap 统一处理
     */
    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    /**
     * 单击确认（系统确保不是双击）
     * 作用：区分 点击图片 / 点击外部
     */
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (photoViewAttacher == null) return false
        val imageView = photoViewAttacher.getImageView()
        // 处理【点击图片】监听
        val displayRect = photoViewAttacher.getDisplayRect()
        if (null != displayRect) {
            val x = e.x
            val y = e.y
            // 判断点击坐标是否在图片范围内
            if (displayRect.contains(x, y)) {
                // 计算点击位置相对于图片的百分比坐标
                val xResult = (x - displayRect.left) / displayRect.width()
                val yResult = (y - displayRect.top) / displayRect.height()
                photoViewAttacher.getOnPhotoTapListener()?.onPhotoTap(imageView, xResult, yResult)
                return true
            } else {
                // 点击了图片外部
                photoViewAttacher.getOnPhotoTapListener()?.onOutsidePhotoTap()
            }
        }
        // 处理【点击控件任意区域】监听
        photoViewAttacher.getOnViewTapListener()?.onViewTap(imageView, e.x, e.y)
        return false
    }

}