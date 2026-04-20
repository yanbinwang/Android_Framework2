package com.yanzhenjie.album.widget.photoview;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;

import com.yanzhenjie.album.widget.photoview.gestures.FroyoGestureDetector;
import com.yanzhenjie.album.widget.photoview.gestures.OnGestureListener;
import com.yanzhenjie.album.widget.photoview.gestures.OnScaleDragListener;
import com.yanzhenjie.album.widget.photoview.scrollerproxy.GingerScroller;
import com.yanzhenjie.album.widget.photoview.scrollerproxy.ScrollerProxy;

import java.lang.ref.WeakReference;

/**
 * PhotoView 核心控制器
 * 所有缩放、拖动、旋转、惯性滑动、双击放大、矩阵计算 都在这里实现
 * 是整个图片预览库的大脑
 */
@SuppressLint("ClickableViewAccessibility")
public class PhotoViewAttacher implements IPhotoView, View.OnTouchListener, OnGestureListener, ViewTreeObserver.OnGlobalLayoutListener {
    // ImageView 显示区域
    private int mIvTop, mIvRight, mIvBottom, mIvLeft;
    // 图片滚动边缘状态
    private int mScrollEdge = EDGE_BOTH;
    // 基础旋转角度
    private float mBaseRotation;
    // 是否允许缩放
    private boolean mZoomEnabled;
    // 弱引用持有 ImageView，防止内存泄漏
    private WeakReference<ImageView> mImageView;
    // 图片显示模式
    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    // 惯性滑动任务
    private FlingRunnable mCurrentFlingRunnable;
    // 各种监听
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private OnLongClickListener mLongClickListener;
    private OnScaleChangeListener mScaleChangeListener;
    private OnSingleFlingListener mSingleFlingListener;
    // 手势检测器
    private GestureDetector mGestureDetector;
    private OnScaleDragListener mScaleDragDetector;
    // 矩阵对象（复用，避免频繁创建）
    private final Matrix mBaseMatrix = new Matrix(); // 基础矩阵（居中、适应屏幕）
    private final Matrix mDrawMatrix = new Matrix(); // 最终绘制矩阵
    private final Matrix mSuppMatrix = new Matrix(); // 缩放/平移/旋转矩阵
    private final RectF mDisplayRect = new RectF(); // 图片显示区域
    private final float[] mMatrixValues = new float[9]; // 矩阵数值存储
    // 缩放级别配置
    private float mMinScale = DEFAULT_MIN_SCALE; // 最小缩放
    private float mMidScale = DEFAULT_MID_SCALE; // 中等缩放（双击第一级）
    private float mMaxScale = DEFAULT_MAX_SCALE; // 最大缩放
    // 滑动边缘父控件拦截配置
    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;
    // 缩放动画插值器与时长
    private int ZOOM_DURATION = DEFAULT_ZOOM_DURATION;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    // 常量
    private static final int SINGLE_TOUCH = 1;
    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_BOTH = 2;

    /**
     * 检查缩放级别合法性：最小 < 中等 < 最大
     */
    private static void checkZoomLevels(float minZoom, float midZoom, float maxZoom) {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException("Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException("Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value");
        }
    }

    /**
     * 判断 ImageView 是否有图片
     */
    private static boolean hasDrawable(ImageView imageView) {
        return null != imageView && null != imageView.getDrawable();
    }

    /**
     * 判断 ScaleType 是否支持（不支持 MATRIX）
     */
    private static boolean isSupportedScaleType(final ScaleType scaleType) {
        if (null == scaleType) {
            return false;
        }
        if (scaleType == ScaleType.MATRIX) {
            throw new IllegalArgumentException(scaleType.name() + " is not supported in PhotoView");
        }
        return true;
    }

    /**
     * 强制把 ImageView 的 ScaleType 设为 MATRIX（必须用矩阵才能缩放）
     */
    private static void setImageViewScaleTypeMatrix(ImageView imageView) {
        if (null != imageView && !(imageView instanceof IPhotoView)) {
            if (!ScaleType.MATRIX.equals(imageView.getScaleType())) {
                imageView.setScaleType(ScaleType.MATRIX);
            }
        }
    }

    /**
     * 构造方法
     */
    public PhotoViewAttacher(ImageView imageView) {
        this(imageView, true);
    }

    /**
     * 构造方法
     *
     * @param imageView 绑定的图片控件
     * @param zoomable  是否允许缩放
     */
    public PhotoViewAttacher(ImageView imageView, boolean zoomable) {
        mImageView = new WeakReference<>(imageView);
        // 开启绘制缓存
        imageView.setDrawingCacheEnabled(true);
        // 设置触摸监听
        imageView.setOnTouchListener(this);
        // 监听布局完成
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        if (null != observer) observer.addOnGlobalLayoutListener(this);
        // 强制使用矩阵模式
        setImageViewScaleTypeMatrix(imageView);
        if (imageView.isInEditMode()) {
            return;
        }
        // 创建手势检测器
        mScaleDragDetector = new FroyoGestureDetector(imageView.getContext());
        mScaleDragDetector.setOnGestureListener(this);
        // 创建系统手势检测器，处理长按、快速滑动
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            /**
             * 长按回调
             */
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                if (null != mLongClickListener) {
                    mLongClickListener.onLongClick(getImageView());
                }
            }

            /**
             * 快速滑动回调
             */
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (mSingleFlingListener != null) {
                    if (getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (MotionEventCompat.getPointerCount(e1) > SINGLE_TOUCH || MotionEventCompat.getPointerCount(e2) > SINGLE_TOUCH) {
                        return false;
                    }
                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        // 设置默认双击监听器
        mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
        mBaseRotation = 0.0f;
        // 设置是否可缩放
        setZoomable(zoomable);
    }

    /**
     * 设置双击监听器
     */
    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        if (newOnDoubleTapListener != null) {
            mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
        } else {
            mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
        }
    }

    /**
     * 设置缩放变化监听
     */
    @Override
    public void setOnScaleChangeListener(@NonNull OnScaleChangeListener onScaleChangeListener) {
        mScaleChangeListener = onScaleChangeListener;
    }

    /**
     * 设置单指快速滑动监听
     */
    @Override
    public void setOnSingleFlingListener(@NonNull OnSingleFlingListener onSingleFlingListener) {
        mSingleFlingListener = onSingleFlingListener;
    }

    /**
     * 是否允许缩放
     */
    @Override
    public boolean canZoom() {
        return mZoomEnabled;
    }

    /**
     * 清理资源，防止内存泄漏
     * 在界面销毁时调用
     */
    public void cleanup() {
        if (null == mImageView) {
            return;
        }
        final ImageView imageView = mImageView.get();
        if (null != imageView) {
            ViewTreeObserver observer = imageView.getViewTreeObserver();
            if (null != observer && observer.isAlive()) {
                observer.removeGlobalOnLayoutListener(this);
            }
            imageView.setOnTouchListener(null);
            cancelFling();
        }
        if (null != mGestureDetector) {
            mGestureDetector.setOnDoubleTapListener(null);
        }
        mMatrixChangeListener = null;
        mPhotoTapListener = null;
        mViewTapListener = null;
        mImageView = null;
    }

    /**
     * 获取图片当前显示区域
     */
    @Override
    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    /**
     * 直接设置显示矩阵
     */
    @Override
    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        ImageView imageView = getImageView();
        if (null == imageView) {
            return false;
        }
        if (null == imageView.getDrawable()) {
            return false;
        }
        mSuppMatrix.set(finalMatrix);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
        return true;
    }

    /**
     * 设置基础旋转角度
     */
    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        update();
        setRotationBy(mBaseRotation);
        checkAndDisplayMatrix();
    }

    /**
     * 旋转图片到指定角度
     */
    @Override
    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    /**
     * 在当前角度基础上再旋转
     */
    @Override
    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    /**
     * 获取绑定的 ImageView
     */
    public ImageView getImageView() {
        ImageView imageView = null;
        if (null != mImageView) {
            imageView = mImageView.get();
        }
        if (null == imageView) {
            cleanup();
        }
        return imageView;
    }

    @Override
    public float getMinimumScale() {
        return mMinScale;
    }

    @Override
    public float getMediumScale() {
        return mMidScale;
    }

    @Override
    public float getMaximumScale() {
        return mMaxScale;
    }

    /**
     * 获取当前缩放比例
     */
    @Override
    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    @NonNull
    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * 拖动回调
     */
    @Override
    public void onDrag(float dx, float dy) {
        if (mScaleDragDetector.isScaling()) {
            return; // Do not drag if we are already scaling
        }
        ImageView imageView = getImageView();
        mSuppMatrix.postTranslate(dx, dy);
        checkAndDisplayMatrix();
        // 边缘时允许父控件拦截（解决ViewPager滑动冲突）
        ViewParent parent = imageView.getParent();
        if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
            if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_LEFT && dx >= 1f) || (mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
                if (null != parent) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
        } else {
            if (null != parent) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    /**
     * 快速滑动（惯性）回调
     */
    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        ImageView imageView = getImageView();
        mCurrentFlingRunnable = new FlingRunnable(imageView.getContext());
        mCurrentFlingRunnable.fling(getImageViewWidth(imageView), getImageViewHeight(imageView), (int) velocityX, (int) velocityY);
        imageView.post(mCurrentFlingRunnable);
    }

    /**
     * 布局完成回调
     * 重新计算图片居中、缩放、位置
     */
    @Override
    public void onGlobalLayout() {
        ImageView imageView = getImageView();
        if (null != imageView) {
            if (mZoomEnabled) {
                final int top = imageView.getTop();
                final int right = imageView.getRight();
                final int bottom = imageView.getBottom();
                final int left = imageView.getLeft();
                // 位置发生变化，重新更新矩阵
                if (top != mIvTop || bottom != mIvBottom || left != mIvLeft || right != mIvRight) {
                    // Update our base matrix, as the bounds have changed
                    updateBaseMatrix(imageView.getDrawable());
                    // Update values as something has changed
                    mIvTop = top;
                    mIvRight = right;
                    mIvBottom = bottom;
                    mIvLeft = left;
                }
            } else {
                updateBaseMatrix(imageView.getDrawable());
            }
        }
    }

    /**
     * 缩放回调
     */
    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if ((getScale() < mMaxScale || scaleFactor < 1f) && (getScale() > mMinScale || scaleFactor > 1f)) {
            if (null != mScaleChangeListener) {
                mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
            }
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkAndDisplayMatrix();
        }
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        if (mZoomEnabled && hasDrawable((ImageView) v)) {
            ViewParent parent = v.getParent();
            switch (ev.getAction()) {
                case ACTION_DOWN:
                    // 按下时禁止父控件拦截，保证图片能拖动
                    if (null != parent) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    cancelFling();
                    break;
                case ACTION_CANCEL:
                case ACTION_UP:
                    // 抬起时，如果缩放小于最小值，自动回弹到最小
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (null != rect) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                    break;
            }
            // 处理缩放、拖动
            if (null != mScaleDragDetector) {
                boolean wasScaling = mScaleDragDetector.isScaling();
                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev);
                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
                mBlockParentIntercept = didntScale && didntDrag;
            }
            // 处理双击、长按
            if (null != mGestureDetector && mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }
        }
        return handled;
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    @Override
    public void setMediumScale(float mediumScale) {
        checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    @Override
    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        checkZoomLevels(minimumScale, mediumScale, maximumScale);
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    @Override
    public void setOnLongClickListener(@NonNull OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    @Override
    public void setOnMatrixChangeListener(@NonNull OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    @Override
    public void setOnPhotoTapListener(@NonNull OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    @Nullable
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mPhotoTapListener;
    }

    @Override
    public void setOnViewTapListener(@NonNull OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    @Nullable
    public OnViewTapListener getOnViewTapListener() {
        return mViewTapListener;
    }

    @Override
    public void setScale(float scale) {
        setScale(scale, false);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        ImageView imageView = getImageView();
        if (null != imageView) {
            setScale(scale, (float) (imageView.getRight()) / 2, (float) (imageView.getBottom()) / 2, animate);
        }
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        ImageView imageView = getImageView();
        if (null != imageView) {
            if (scale < mMinScale || scale > mMaxScale) {
                return;
            }
            if (animate) {
                imageView.post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
            } else {
                mSuppMatrix.setScale(scale, scale, focalX, focalY);
                checkAndDisplayMatrix();
            }
        }
    }

    /**
     * 设置缩放动画插值器
     */
    public void setZoomInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    /**
     * 更新图片显示（重新计算矩阵、位置）
     */
    public void update() {
        ImageView imageView = getImageView();
        if (null != imageView) {
            if (mZoomEnabled) {
                setImageViewScaleTypeMatrix(imageView);
                updateBaseMatrix(imageView.getDrawable());
            } else {
                resetMatrix();
            }
        }
    }

    /**
     * 获取显示矩阵
     */
    @Override
    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(getDrawMatrix());
    }

    /**
     * 获取缩放/平移矩阵
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.set(mSuppMatrix);
    }

    /**
     * 获取最终绘制矩阵
     */
    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    /**
     * 取消惯性滑动
     */
    private void cancelFling() {
        if (null != mCurrentFlingRunnable) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    /**
     * 获取图片矩阵
     */
    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    /**
     * 检查矩阵边界并应用
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * 检查 ImageView 缩放模式是否正确
     */
    private void checkImageViewScaleType() {
        ImageView imageView = getImageView();
        if (null != imageView && !(imageView instanceof IPhotoView)) {
            if (!ScaleType.MATRIX.equals(imageView.getScaleType())) {
                throw new IllegalStateException("The ImageView's ScaleType has been changed since attaching a PhotoViewAttacher. You should call " + "setScaleType on the PhotoViewAttacher instead of on the ImageView");
            }
        }
    }

    /**
     * 检查矩阵边界，限制图片不能划出屏幕
     */
    private boolean checkMatrixBounds() {
        final ImageView imageView = getImageView();
        if (null == imageView) {
            return false;
        }
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (null == rect) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = getImageViewHeight(imageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }
        final int viewWidth = getImageViewWidth(imageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mScrollEdge = EDGE_BOTH;
        } else if (rect.left > 0) {
            mScrollEdge = EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    /**
     * 计算矩阵对应的图片显示区域
     */
    private RectF getDisplayRect(Matrix matrix) {
        ImageView imageView = getImageView();
        if (null != imageView) {
            Drawable d = imageView.getDrawable();
            if (null != d) {
                mDisplayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                matrix.mapRect(mDisplayRect);
                return mDisplayRect;
            }
        }
        return null;
    }

    /**
     * 获取当前可见区域的截图
     */
    public Bitmap getVisibleRectangleBitmap() {
        ImageView imageView = getImageView();
        return imageView == null ? null : imageView.getDrawingCache();
    }

    @Override
    public void setZoomTransitionDuration(int milliseconds) {
        if (milliseconds < 0) milliseconds = DEFAULT_ZOOM_DURATION;
        ZOOM_DURATION = milliseconds;
    }

    @NonNull
    @Override
    public IPhotoView getIPhotoViewImplementation() {
        return this;
    }

    /**
     * 从矩阵中获取指定数值
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * 重置矩阵
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    /**
     * 把矩阵应用到 ImageView
     */
    private void setImageViewMatrix(Matrix matrix) {
        ImageView imageView = getImageView();
        if (null != imageView) {
            checkImageViewScaleType();
            imageView.setImageMatrix(matrix);
            if (null != mMatrixChangeListener) {
                RectF displayRect = getDisplayRect(matrix);
                if (null != displayRect) {
                    mMatrixChangeListener.onMatrixChanged(displayRect);
                }
            }
        }
    }

    /**
     * 计算基础矩阵（图片居中、适应屏幕）
     */
    private void updateBaseMatrix(Drawable d) {
        ImageView imageView = getImageView();
        if (null == imageView || null == d) {
            return;
        }
        final float viewWidth = getImageViewWidth(imageView);
        final float viewHeight = getImageViewHeight(imageView);
        final int drawableWidth = d.getIntrinsicWidth();
        final int drawableHeight = d.getIntrinsicHeight();
        mBaseMatrix.reset();
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F, (viewHeight - drawableHeight) / 2F);
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F, (viewHeight - drawableHeight * scale) / 2F);
        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F, (viewHeight - drawableHeight * scale) / 2F);
        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, (float) drawableHeight, (float) drawableWidth);
            }
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    /**
     * 获取 ImageView 有效宽度/高度（去除内边距）
     */
    private int getImageViewWidth(ImageView imageView) {
        if (null == imageView) return 0;
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        if (null == imageView) return 0;
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    /**
     * 内部接口
     */
    public interface OnMatrixChangedListener {

        void onMatrixChanged(RectF rect);

    }

    public interface OnScaleChangeListener {

        void onScaleChange(float scaleFactor, float focusX, float focusY);

    }

    public interface OnPhotoTapListener {

        void onPhotoTap(View view, float x, float y);

        void onOutsidePhotoTap();

    }

    public interface OnViewTapListener {

        void onViewTap(View v, float x, float y);

    }

    public interface OnSingleFlingListener {

        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

    }

    /**
     * 缩放动画
     */
    private class AnimatedZoomRunnable implements Runnable {
        private final float mFocalX, mFocalY;
        private final float mZoomStart, mZoomEnd;
        private final long mStartTime;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom, final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            ImageView imageView = getImageView();
            if (imageView == null) {
                return;
            }
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onScale(deltaScale, mFocalX, mFocalY);
            if (t < 1f) {
                imageView.postOnAnimation(this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / ZOOM_DURATION;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    /**
     * 惯性滑动
     */
    private class FlingRunnable implements Runnable {
        private int mCurrentX, mCurrentY;
        private final ScrollerProxy mScroller;

        public FlingRunnable(Context context) {
            mScroller = new GingerScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            final RectF rect = getDisplayRect();
            if (null == rect) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return;
            }
            ImageView imageView = getImageView();
            if (null != imageView && mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                setImageViewMatrix(getDrawMatrix());
                mCurrentX = newX;
                mCurrentY = newY;
                imageView.postOnAnimation(this);
            }
        }
    }

}