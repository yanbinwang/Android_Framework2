package com.yanzhenjie.durban.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.yanzhenjie.durban.callback.BitmapCropCallback;
import com.yanzhenjie.durban.callback.CropBoundsChangeListener;
import com.yanzhenjie.durban.model.CropParameters;
import com.yanzhenjie.durban.model.ImageState;
import com.yanzhenjie.durban.app.data.BitmapCropTask;
import com.yanzhenjie.durban.utils.CubicEasing;
import com.yanzhenjie.durban.utils.RectUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * 图片裁剪核心View
 * 功能：缩放、旋转、裁剪、保存、动画、边界控制
 * 父类：TransformImageView（负责基础矩阵变换）
 */
public class CropImageView extends TransformImageView {
    // 输出图片最大宽高
    private int mMaxResultImageSizeX = 0, mMaxResultImageSizeY = 0;
    // 目标裁剪比例（1:1 / 4:3 / 16:9）
    private float mTargetAspectRatio;
    // 最大/最小缩放倍数
    private float mMaxScale, mMinScale;
    // 最大缩放倍数 = 最小缩放 * 此值
    private float mMaxScaleMultiplier = DEFAULT_MAX_SCALE_MULTIPLIER;
    // 图片自适应裁剪框动画时间
    private long mImageToWrapCropBoundsAnimDuration = DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION;
    // 裁剪比例变化监听
    private CropBoundsChangeListener mCropBoundsChangeListener;
    // 动画任务
    private Runnable mWrapCropBoundsRunnable, mZoomImageToPositionRunnable = null;
    // 裁剪区域矩形
    private final RectF mCropRect = new RectF();
    // 临时矩阵（计算用，避免频繁创建对象）
    private final Matrix mTempMatrix = new Matrix();
    // 静态常量
    public static final int DEFAULT_MAX_BITMAP_SIZE = 0;
    public static final int DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = 500;
    public static final float SOURCE_IMAGE_ASPECT_RATIO = 0f;
    public static final float DEFAULT_ASPECT_RATIO = SOURCE_IMAGE_ASPECT_RATIO;
    public static final float DEFAULT_MAX_SCALE_MULTIPLIER = 10.0f;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 图片加载完成 → 初始化裁剪框位置
     */
    @Override
    protected void onImageLaidOut() {
        super.onImageLaidOut();
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();
        if (mTargetAspectRatio == SOURCE_IMAGE_ASPECT_RATIO) {
            mTargetAspectRatio = drawableWidth / drawableHeight;
        }
        // 根据比例计算裁剪框位置
        int height = (int) (mThisWidth / mTargetAspectRatio);
        if (height > mThisHeight) {
            int width = (int) (mThisHeight * mTargetAspectRatio);
            int halfDiff = (mThisWidth - width) / 2;
            mCropRect.set(halfDiff, 0, width + halfDiff, mThisHeight);
        } else {
            int halfDiff = (mThisHeight - height) / 2;
            mCropRect.set(0, halfDiff, mThisWidth, height + halfDiff);
        }
        // 计算缩放范围
        calculateImageScaleBounds(drawableWidth, drawableHeight);
        // 初始化图片位置
        setupInitialImagePosition(drawableWidth, drawableHeight);
        // 通知外部
        if (mCropBoundsChangeListener != null) {
            mCropBoundsChangeListener.onCropAspectRatioChanged(mTargetAspectRatio);
        }
        if (mTransformImageListener != null) {
            mTransformImageListener.onScale(getCurrentScale());
            mTransformImageListener.onRotate(getCurrentAngle());
        }
    }

    /**
     * 计算图片与裁剪框的间距
     */
    private float[] calculateImageIndents() {
        mTempMatrix.reset();
        mTempMatrix.setRotate(-getCurrentAngle());
        float[] unRotatedImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
        float[] unRotatedCropBoundsCorners = RectUtil.getCornersFromRect(mCropRect);
        mTempMatrix.mapPoints(unRotatedImageCorners);
        mTempMatrix.mapPoints(unRotatedCropBoundsCorners);
        RectF unRotatedImageRect = RectUtil.trapToRect(unRotatedImageCorners);
        RectF unRotatedCropRect = RectUtil.trapToRect(unRotatedCropBoundsCorners);
        float deltaLeft = unRotatedImageRect.left - unRotatedCropRect.left;
        float deltaTop = unRotatedImageRect.top - unRotatedCropRect.top;
        float deltaRight = unRotatedImageRect.right - unRotatedCropRect.right;
        float deltaBottom = unRotatedImageRect.bottom - unRotatedCropRect.bottom;
        float[] indents = new float[4];
        indents[0] = (deltaLeft > 0) ? deltaLeft : 0;
        indents[1] = (deltaTop > 0) ? deltaTop : 0;
        indents[2] = (deltaRight < 0) ? deltaRight : 0;
        indents[3] = (deltaBottom < 0) ? deltaBottom : 0;
        mTempMatrix.reset();
        mTempMatrix.setRotate(getCurrentAngle());
        mTempMatrix.mapPoints(indents);
        return indents;
    }

    /**
     * 计算最小/最大缩放
     */
    private void calculateImageScaleBounds() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        calculateImageScaleBounds(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    private void calculateImageScaleBounds(float drawableWidth, float drawableHeight) {
        float widthScale = Math.min(mCropRect.width() / drawableWidth, mCropRect.width() / drawableHeight);
        float heightScale = Math.min(mCropRect.height() / drawableHeight, mCropRect.height() / drawableWidth);
        mMinScale = Math.min(widthScale, heightScale);
        mMaxScale = mMinScale * mMaxScaleMultiplier;
    }

    /**
     * 初始化图片居中
     */
    private void setupInitialImagePosition(float drawableWidth, float drawableHeight) {
        float cropRectWidth = mCropRect.width();
        float cropRectHeight = mCropRect.height();
        float widthScale = mCropRect.width() / drawableWidth;
        float heightScale = mCropRect.height() / drawableHeight;
        float initialMinScale = Math.max(widthScale, heightScale);
        float tw = (cropRectWidth - drawableWidth * initialMinScale) / 2.0f + mCropRect.left;
        float th = (cropRectHeight - drawableHeight * initialMinScale) / 2.0f + mCropRect.top;
        mCurrentImageMatrix.reset();
        mCurrentImageMatrix.postScale(initialMinScale, initialMinScale);
        mCurrentImageMatrix.postTranslate(tw, th);
        setImageMatrix(mCurrentImageMatrix);
    }

    /**
     * 判断图片是否填满裁剪框
     */
    protected boolean isImageWrapCropBounds() {
        return isImageWrapCropBounds(mCurrentImageCorners);
    }

    protected boolean isImageWrapCropBounds(float[] imageCorners) {
        mTempMatrix.reset();
        mTempMatrix.setRotate(-getCurrentAngle());
        float[] unRotatedImageCorners = Arrays.copyOf(imageCorners, imageCorners.length);
        mTempMatrix.mapPoints(unRotatedImageCorners);
        float[] unRotatedCropBoundsCorners = RectUtil.getCornersFromRect(mCropRect);
        mTempMatrix.mapPoints(unRotatedCropBoundsCorners);
        return RectUtil.trapToRect(unRotatedImageCorners).contains(RectUtil.trapToRect(unRotatedCropBoundsCorners));
    }

    /**
     * 缩放动画
     */
    protected void zoomImageToPosition(float scale, float centerX, float centerY, long durationMs) {
        if (scale > getMaxScale()) {
            scale = getMaxScale();
        }
        final float oldScale = getCurrentScale();
        final float deltaScale = scale - oldScale;
        post(mZoomImageToPositionRunnable = new ZoomImageToPosition(CropImageView.this, durationMs, oldScale, deltaScale, centerX, centerY));
    }

    /**
     * 读取XML自定义属性
     */
    protected void processStyledAttributes(@NonNull TypedArray a) {
        float targetAspectRatioX = Math.abs(a.getFloat(R.styleable.durban_CropView_durban_aspect_ratio_x, DEFAULT_ASPECT_RATIO));
        float targetAspectRatioY = Math.abs(a.getFloat(R.styleable.durban_CropView_durban_aspect_ratio_y, DEFAULT_ASPECT_RATIO));
        if (targetAspectRatioX == SOURCE_IMAGE_ASPECT_RATIO || targetAspectRatioY == SOURCE_IMAGE_ASPECT_RATIO) {
            mTargetAspectRatio = SOURCE_IMAGE_ASPECT_RATIO;
        } else {
            mTargetAspectRatio = targetAspectRatioX / targetAspectRatioY;
        }
    }

    /**
     * 裁剪并保存图片
     */
    public void cropAndSaveImage(@NonNull Bitmap.CompressFormat compressFormat, int compressQuality, @Nullable BitmapCropCallback cropCallback) {
        // 取消所有动画
        cancelAllAnimations();
        // 让图片适应裁剪框
        setImageToWrapCropBounds(false);
        // 封装当前图片状态
        final ImageState imageState = new ImageState(mCropRect, RectUtil.trapToRect(mCurrentImageCorners), getCurrentScale(), getCurrentAngle());
        // 封装裁剪参数
        final CropParameters cropParameters = new CropParameters(mMaxResultImageSizeX, mMaxResultImageSizeY, compressFormat, compressQuality, getImagePath(), getOutputDirectory(), getExifInfo());
        // 异步裁剪并保存
        new BitmapCropTask(getContext(), getViewBitmap(), imageState, cropParameters, cropCallback).execute();
    }

    /**
     * 获取缩放范围
     */
    public float getMaxScale() {
        return mMaxScale;
    }

    public float getMinScale() {
        return mMinScale;
    }

    public float getTargetAspectRatio() {
        return mTargetAspectRatio;
    }

    /**
     * 设置裁剪区域
     */
    public void setCropRect(RectF cropRect) {
        // 计算比例
        mTargetAspectRatio = cropRect.width() / cropRect.height();
        // 设置裁剪区域（减去内边距）
        mCropRect.set(cropRect.left - getPaddingLeft(), cropRect.top - getPaddingTop(), cropRect.right - getPaddingRight(), cropRect.bottom - getPaddingBottom());
        // 重新计算缩放范围
        calculateImageScaleBounds();
        // 让图片适应新裁剪框
        setImageToWrapCropBounds();
    }

    /**
     * 设置裁剪比例
     */
    public void setTargetAspectRatio(float targetAspectRatio) {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            mTargetAspectRatio = targetAspectRatio;
            return;
        }
        // 使用图片原始比例
        if (targetAspectRatio == SOURCE_IMAGE_ASPECT_RATIO) {
            mTargetAspectRatio = drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight();
        } else {
            mTargetAspectRatio = targetAspectRatio;
        }
        // 通知外部：比例已改变
        if (mCropBoundsChangeListener != null) {
            mCropBoundsChangeListener.onCropAspectRatioChanged(mTargetAspectRatio);
        }
    }

    /**
     * 设置输出图片最大尺寸
     */
    public void setMaxResultImageSizeX(@IntRange(from = 10) int maxResultImageSizeX) {
        mMaxResultImageSizeX = maxResultImageSizeX;
    }

    public void setMaxResultImageSizeY(@IntRange(from = 10) int maxResultImageSizeY) {
        mMaxResultImageSizeY = maxResultImageSizeY;
    }

    /**
     * 动画时间配置
     */
    public void setImageToWrapCropBoundsAnimDuration(@IntRange(from = 100) long imageToWrapCropBoundsAnimDuration) {
        if (imageToWrapCropBoundsAnimDuration > 0) {
            mImageToWrapCropBoundsAnimDuration = imageToWrapCropBoundsAnimDuration;
        } else {
            throw new IllegalArgumentException("Animation duration cannot be negative value.");
        }
    }

    /**
     * 最大放大倍数
     */
    public void setMaxScaleMultiplier(float maxScaleMultiplier) {
        mMaxScaleMultiplier = maxScaleMultiplier;
    }

    /**
     * 缩放方法
     */
    public void zoomOutImage(float deltaScale) {
        zoomOutImage(deltaScale, mCropRect.centerX(), mCropRect.centerY());
    }

    public void zoomOutImage(float scale, float centerX, float centerY) {
        if (scale >= getMinScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY);
        }
    }

    public void zoomInImage(float deltaScale) {
        zoomInImage(deltaScale, mCropRect.centerX(), mCropRect.centerY());
    }

    public void zoomInImage(float scale, float centerX, float centerY) {
        if (scale <= getMaxScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY);
        }
    }

    /**
     * 安全缩放（不超过最大/最小范围）
     */
    public void postScale(float deltaScale, float px, float py) {
        if (deltaScale > 1 && getCurrentScale() * deltaScale <= getMaxScale()) {
            super.postScale(deltaScale, px, py);
        } else if (deltaScale < 1 && getCurrentScale() * deltaScale >= getMinScale()) {
            super.postScale(deltaScale, px, py);
        }
    }

    /**
     * 旋转（以裁剪框中心为中心）
     */
    public void postRotate(float deltaAngle) {
        postRotate(deltaAngle, mCropRect.centerX(), mCropRect.centerY());
    }

    /**
     * 取消所有动画
     */
    public void cancelAllAnimations() {
        removeCallbacks(mWrapCropBoundsRunnable);
        removeCallbacks(mZoomImageToPositionRunnable);
    }

    /**
     * 让图片填满裁剪框
     */
    public void setImageToWrapCropBounds() {
        setImageToWrapCropBounds(true);
    }

    /**
     * 自动调整图片位置与缩放，使其填满裁剪框
     */
    public void setImageToWrapCropBounds(boolean animate) {
        if (mBitmapLaidOut && !isImageWrapCropBounds()) {
            float currentX = mCurrentImageCenter[0];
            float currentY = mCurrentImageCenter[1];
            float currentScale = getCurrentScale();
            float deltaX = mCropRect.centerX() - currentX;
            float deltaY = mCropRect.centerY() - currentY;
            float deltaScale = 0;
            // 临时计算
            mTempMatrix.reset();
            mTempMatrix.setTranslate(deltaX, deltaY);
            final float[] tempCurrentImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
            mTempMatrix.mapPoints(tempCurrentImageCorners);
            // 是否仅仅移动就可以填满
            boolean willImageWrapCropBoundsAfterTranslate = isImageWrapCropBounds(tempCurrentImageCorners);
            if (willImageWrapCropBoundsAfterTranslate) {
                final float[] imageIndents = calculateImageIndents();
                deltaX = -(imageIndents[0] + imageIndents[2]);
                deltaY = -(imageIndents[1] + imageIndents[3]);
            } else {
                // 需要同时移动 + 缩放
                RectF tempCropRect = new RectF(mCropRect);
                mTempMatrix.reset();
                mTempMatrix.setRotate(getCurrentAngle());
                mTempMatrix.mapRect(tempCropRect);
                final float[] currentImageSides = RectUtil.getRectSidesFromCorners(mCurrentImageCorners);
                deltaScale = Math.max(tempCropRect.width() / currentImageSides[0], tempCropRect.height() / currentImageSides[1]);
                deltaScale = deltaScale * currentScale - currentScale;
            }
            // 执行动画 / 直接移动
            if (animate) {
                post(mWrapCropBoundsRunnable = new WrapCropBoundsRunnable(CropImageView.this, mImageToWrapCropBoundsAnimDuration, currentX, currentY, deltaX, deltaY, currentScale, deltaScale, willImageWrapCropBoundsAfterTranslate));
            } else {
                postTranslate(deltaX, deltaY);
                if (!willImageWrapCropBoundsAfterTranslate) {
                    zoomInImage(currentScale + deltaScale, mCropRect.centerX(), mCropRect.centerY());
                }
            }
        }
    }

    /**
     * 设置/获取裁剪框比例变化监听器
     */
    public void setCropBoundsChangeListener(@Nullable CropBoundsChangeListener cropBoundsChangeListener) {
        mCropBoundsChangeListener = cropBoundsChangeListener;
    }

    @Nullable
    public CropBoundsChangeListener getCropBoundsChangeListener() {
        return mCropBoundsChangeListener;
    }

    /**
     * 动画：自动填满裁剪框
     */
    private static class WrapCropBoundsRunnable implements Runnable {
        private final float mOldX, mOldY;
        private final float mCenterDiffX, mCenterDiffY;
        private final float mOldScale;
        private final float mDeltaScale;
        private final long mDurationMs, mStartTime;
        private final boolean mWillBeImageInBoundsAfterTranslate;
        private final WeakReference<CropImageView> mCropImageView;

        public WrapCropBoundsRunnable(CropImageView cropImageView, long durationMs, float oldX, float oldY, float centerDiffX, float centerDiffY, float oldScale, float deltaScale, boolean willBeImageInBoundsAfterTranslate) {
            mCropImageView = new WeakReference<>(cropImageView);
            mDurationMs = durationMs;
            mStartTime = System.currentTimeMillis();
            mOldX = oldX;
            mOldY = oldY;
            mCenterDiffX = centerDiffX;
            mCenterDiffY = centerDiffY;
            mOldScale = oldScale;
            mDeltaScale = deltaScale;
            mWillBeImageInBoundsAfterTranslate = willBeImageInBoundsAfterTranslate;
        }

        @Override
        public void run() {
            CropImageView cropImageView = mCropImageView.get();
            if (cropImageView == null) {
                return;
            }
            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);
            float newX = CubicEasing.easeOut(currentMs, 0, mCenterDiffX, mDurationMs);
            float newY = CubicEasing.easeOut(currentMs, 0, mCenterDiffY, mDurationMs);
            float newScale = CubicEasing.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);
            if (currentMs < mDurationMs) {
                cropImageView.postTranslate(newX - (cropImageView.mCurrentImageCenter[0] - mOldX), newY - (cropImageView.mCurrentImageCenter[1] - mOldY));
                if (!mWillBeImageInBoundsAfterTranslate) {
                    cropImageView.zoomInImage(mOldScale + newScale, cropImageView.mCropRect.centerX(), cropImageView.mCropRect.centerY());
                }
                if (!cropImageView.isImageWrapCropBounds()) {
                    cropImageView.post(this);
                }
            }
        }
    }

    /**
     * 动画：缩放图片
     */
    private static class ZoomImageToPosition implements Runnable {
        private final float mOldScale;
        private final float mDeltaScale;
        private final float mDestX;
        private final float mDestY;
        private final long mDurationMs, mStartTime;
        private final WeakReference<CropImageView> mCropImageView;

        public ZoomImageToPosition(CropImageView cropImageView, long durationMs, float oldScale, float deltaScale, float destX, float destY) {
            mCropImageView = new WeakReference<>(cropImageView);
            mStartTime = System.currentTimeMillis();
            mDurationMs = durationMs;
            mOldScale = oldScale;
            mDeltaScale = deltaScale;
            mDestX = destX;
            mDestY = destY;
        }

        @Override
        public void run() {
            CropImageView cropImageView = mCropImageView.get();
            if (cropImageView == null) {
                return;
            }
            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);
            float newScale = CubicEasing.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);
            if (currentMs < mDurationMs) {
                cropImageView.zoomInImage(mOldScale + newScale, mDestX, mDestY);
                cropImageView.post(this);
            } else {
                cropImageView.setImageToWrapCropBounds();
            }
        }
    }

}