package com.yanzhenjie.album.widget.photoview;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * 图片预览控件的功能接口
 * 定义：缩放、旋转、拖动、矩阵、各种点击事件
 */
public interface IPhotoView {
    // 缩放动画默认时长：200毫秒
    int DEFAULT_ZOOM_DURATION = 200;
    // 最大缩放比例：3倍
    float DEFAULT_MAX_SCALE = 3.0f;
    // 中等缩放比例：1.75倍（双击放大到这个值）
    float DEFAULT_MID_SCALE = 1.75f;
    // 最小缩放比例：1倍（原图）
    float DEFAULT_MIN_SCALE = 1.0f;

    /**
     * 判断当前图片是否允许缩放
     *
     * @return true=允许
     */
    boolean canZoom();

    /**
     * 获取当前图片在控件中的显示区域（坐标、宽高）
     * 包含缩放、平移后的最终位置
     *
     * @return 图片显示区域 RectF
     */
    RectF getDisplayRect();

    /**
     * 直接设置图片的显示矩阵（控制位置、缩放、旋转）
     *
     * @param finalMatrix 要设置的最终矩阵
     * @return 是否设置成功
     */
    boolean setDisplayMatrix(Matrix finalMatrix);

    /**
     * 获取当前图片的显示矩阵（复制到传入的 matrix 中）
     *
     * @param matrix 用于接收矩阵数据的对象
     */
    void getDisplayMatrix(Matrix matrix);

    /**
     * 获取当前最小缩放比例
     */
    float getMinimumScale();

    /**
     * 获取当前中等缩放比例
     */
    float getMediumScale();

    /**
     * 获取当前最大缩放比例
     */
    float getMaximumScale();

    /**
     * 获取当前图片的缩放比例
     */
    float getScale();

    /**
     * 获取当前 ImageView 的缩放模式（ScaleType）
     */
    ImageView.ScaleType getScaleType();

    /**
     * 设置：当图片滑动到边缘时，是否允许父控件拦截触摸事件
     * 用于解决 ViewPager 滑动冲突
     *
     * @param allow true=允许
     */
    void setAllowParentInterceptOnEdge(boolean allow);

    /**
     * 设置最小缩放比例
     */
    void setMinimumScale(float minimumScale);

    /**
     * 设置中等缩放比例
     */
    void setMediumScale(float mediumScale);

    /**
     * 设置最大缩放比例
     */
    void setMaximumScale(float maximumScale);

    /**
     * 一次性设置 最小/中等/最大 三个缩放级别
     * 避免单独设置时出现比例冲突
     */
    void setScaleLevels(float minimumScale, float mediumScale, float maximumScale);

    /**
     * 设置图片长按监听
     */
    void setOnLongClickListener(View.OnLongClickListener listener);

    /**
     * 设置图片矩阵变化监听（拖动/缩放/旋转时回调）
     */
    void setOnMatrixChangeListener(PhotoViewAttacher.OnMatrixChangedListener listener);

    /**
     * 设置点击图片的监听
     */
    void setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener listener);

    /**
     * 设置点击控件空白区域的监听
     */
    void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener);

    /**
     * 直接将图片旋转到指定角度
     *
     * @param rotationDegree 目标角度 0~360
     */
    void setRotationTo(float rotationDegree);

    /**
     * 在当前角度基础上再旋转多少度
     *
     * @param rotationDegree 旋转增量 0~360
     */
    void setRotationBy(float rotationDegree);

    /**
     * 设置图片缩放比例（默认开启动画）
     */
    void setScale(float scale);

    /**
     * 设置图片缩放比例，并指定是否开启动画
     */
    void setScale(float scale, boolean animate);

    /**
     * 以指定坐标为中心点，进行缩放
     *
     * @param scale   目标比例
     * @param focalX  中心点X
     * @param focalY  中心点Y
     * @param animate 是否动画
     */
    void setScale(float scale, float focalX, float focalY, boolean animate);

    /**
     * 设置图片的缩放模式（ScaleType）
     */
    void setScaleType(ImageView.ScaleType scaleType);

    /**
     * 开启/关闭图片缩放功能
     */
    void setZoomable(boolean zoomable);

    /**
     * 获取当前屏幕显示区域的图片片段（截图当前可见区域）
     *
     * @return 可见区域的Bitmap，无图时返回null
     */
    Bitmap getVisibleRectangleBitmap();

    /**
     * 设置缩放动画的过渡时长
     */
    void setZoomTransitionDuration(int milliseconds);

    /**
     * 获取 IPhotoView 实现类实例（用于扩展）
     */
    IPhotoView getIPhotoViewImplementation();

    /**
     * 设置自定义双击事件监听，传null则恢复默认行为
     */
    void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener);

    /**
     * 设置缩放比例变化监听
     */
    void setOnScaleChangeListener(PhotoViewAttacher.OnScaleChangeListener onScaleChangeListener);

    /**
     * 设置单手快速滑动（Fling）监听
     */
    void setOnSingleFlingListener(PhotoViewAttacher.OnSingleFlingListener onSingleFlingListener);

}