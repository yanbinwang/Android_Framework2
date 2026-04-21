package com.example.gallery.album.widget.photoview

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.view.GestureDetector
import android.view.View.OnLongClickListener
import android.widget.ImageView.ScaleType

/**
 * 图片预览控件的功能接口
 * 定义：缩放、旋转、拖动、矩阵、各种点击事件
 */
interface IPhotoView {

    companion object {
        // 缩放动画默认时长：200毫秒
        const val DEFAULT_ZOOM_DURATION = 200
        // 最大缩放比例：3倍
        const val DEFAULT_MAX_SCALE = 3.0f
        // 中等缩放比例：1.75倍（双击放大到这个值）
        const val DEFAULT_MID_SCALE = 1.75f
        // 最小缩放比例：1倍（原图）
        const val DEFAULT_MIN_SCALE = 1.0f
    }

    /**
     * 判断当前图片是否允许缩放
     *
     * @return true=允许
     */
    fun canZoom(): Boolean

    /**
     * 获取当前图片在控件中的显示区域（坐标、宽高）
     * 包含缩放、平移后的最终位置
     *
     * @return 图片显示区域 RectF
     */
    fun getDisplayRect(): RectF?

    /**
     * 直接设置图片的显示矩阵（控制位置、缩放、旋转）
     *
     * @param finalMatrix 要设置的最终矩阵
     * @return 是否设置成功
     */
    fun setDisplayMatrix(finalMatrix: Matrix?): Boolean

    /**
     * 获取当前图片的显示矩阵（复制到传入的 matrix 中）
     *
     * @param matrix 用于接收矩阵数据的对象
     */
    fun getDisplayMatrix(matrix: Matrix)

    /**
     * 获取当前最小缩放比例
     */
    fun getMinimumScale(): Float

    /**
     * 获取当前中等缩放比例
     */
    fun getMediumScale(): Float

    /**
     * 获取当前最大缩放比例
     */
    fun getMaximumScale(): Float

    /**
     * 获取当前图片的缩放比例
     */
    fun getScale(): Float

    /**
     * 获取当前 ImageView 的缩放模式（ScaleType）
     */
    fun getScaleType(): ScaleType

    /**
     * 设置：当图片滑动到边缘时，是否允许父控件拦截触摸事件
     * 用于解决 ViewPager 滑动冲突
     *
     * @param allow true=允许
     */
    fun setAllowParentInterceptOnEdge(allow: Boolean)

    /**
     * 设置最小缩放比例
     */
    fun setMinimumScale(minimumScale: Float)

    /**
     * 设置中等缩放比例
     */
    fun setMediumScale(mediumScale: Float)

    /**
     * 设置最大缩放比例
     */
    fun setMaximumScale(maximumScale: Float)

    /**
     * 一次性设置 最小/中等/最大 三个缩放级别
     * 避免单独设置时出现比例冲突
     */
    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float)

    /**
     * 设置图片长按监听
     */
    fun setOnLongClickListener(listener: OnLongClickListener)

    /**
     * 设置图片矩阵变化监听（拖动/缩放/旋转时回调）
     */
    fun setOnMatrixChangeListener(listener: PhotoViewAttacher.OnMatrixChangedListener)

    /**
     * 设置点击图片的监听
     */
    fun setOnPhotoTapListener(listener: PhotoViewAttacher.OnPhotoTapListener)

    /**
     * 设置点击控件空白区域的监听
     */
    fun setOnViewTapListener(listener: PhotoViewAttacher.OnViewTapListener)

    /**
     * 直接将图片旋转到指定角度
     *
     * @param rotationDegree 目标角度 0~360
     */
    fun setRotationTo(rotationDegree: Float)

    /**
     * 在当前角度基础上再旋转多少度
     *
     * @param rotationDegree 旋转增量 0~360
     */
    fun setRotationBy(rotationDegree: Float)

    /**
     * 设置图片缩放比例（默认开启动画）
     */
    fun setScale(scale: Float)

    /**
     * 设置图片缩放比例，并指定是否开启动画
     */
    fun setScale(scale: Float, animate: Boolean)

    /**
     * 以指定坐标为中心点，进行缩放
     *
     * @param scale   目标比例
     * @param focalX  中心点X
     * @param focalY  中心点Y
     * @param animate 是否动画
     */
    fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean)

    /**
     * 设置图片的缩放模式（ScaleType）
     */
    fun setScaleType(scaleType: ScaleType)

    /**
     * 开启/关闭图片缩放功能
     */
    fun setZoomable(zoomable: Boolean)

    /**
     * 获取当前屏幕显示区域的图片片段（截图当前可见区域）
     *
     * @return 可见区域的Bitmap，无图时返回null
     */
    fun getVisibleRectangleBitmap(): Bitmap?

    /**
     * 设置缩放动画的过渡时长
     */
    fun setZoomTransitionDuration(milliseconds: Int)

    /**
     * 获取 IPhotoView 实现类实例（用于扩展）
     */
    fun getIPhotoViewImplementation(): IPhotoView

    /**
     * 设置自定义双击事件监听，传null则恢复默认行为
     */
    fun setOnDoubleTapListener(newOnDoubleTapListener: GestureDetector.OnDoubleTapListener?)

    /**
     * 设置缩放比例变化监听
     */
    fun setOnScaleChangeListener(onScaleChangeListener: PhotoViewAttacher.OnScaleChangeListener)

    /**
     * 设置单手快速滑动（Fling）监听
     */
    fun setOnSingleFlingListener(onSingleFlingListener: PhotoViewAttacher.OnSingleFlingListener)
}