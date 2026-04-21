package com.yanzhenjie.durban.widget.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;
import com.yanzhenjie.durban.utils.RectUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 裁剪遮罩层View
 * 功能：绘制半透明背景、裁剪框、网格线、支持自由拖拽裁剪框
 */
@SuppressLint("ClickableViewAccessibility")
public class OverlayView extends View {
    // 自由裁剪模式
    @FreestyleMode
    private int mFreestyleCropMode = DEFAULT_FREESTYLE_CROP_MODE;
    // 裁剪网格行列数
    private int mCropGridRowCount, mCropGridColumnCount;
    // 遮罩半透明颜色
    private int mDimmedColor;
    // 当前触摸的裁剪框角点索引
    private int mCurrentTouchCornerIndex = -1;
    // 触摸识别阈值
    private int mTouchPointThreshold;
    // 裁剪框最小尺寸
    private int mCropRectMinSize;
    // 裁剪框角点触摸区域长度
    private int mCropRectCornerTouchAreaLineLength;
    // 目标宽高比
    private float mTargetAspectRatio;
    // 上一次触摸坐标
    private float mPreviousTouchX = -1, mPreviousTouchY = -1;
    // 网格线坐标点
    private float[] mGridPoints = null;
    // 是否显示裁剪框、网格
    private boolean mShowCropFrame, mShowCropGrid;
    // 是否圆形遮罩
    private boolean mCircleDimmedLayer;
    // 是否需要初始化裁剪边界
    private boolean mShouldSetupCropBounds;
    // 裁剪框变化回调
    private OverlayViewChangeListener mCallback;
    // 圆形裁剪路径
    private final Path mCircularPath = new Path();
    // 半透明背景画笔
    private final Paint mDimmedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 裁剪网格画笔
    private final Paint mCropGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 裁剪框画笔
    private final Paint mCropFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 裁剪框角点画笔
    private final Paint mCropFrameCornersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 裁剪框矩形
    private final RectF mCropViewRect = new RectF();
    // 临时矩形
    private final RectF mTempRect = new RectF();
    // View宽高
    protected int mThisWidth, mThisHeight;
    // 裁剪框四个角坐标、中心坐标
    protected float[] mCropGridCorners;
    protected float[] mCropGridCenter;
    // 自由裁剪模式常量
    public static final int FREESTYLE_CROP_MODE_DISABLE = 0; // 禁用自由裁剪
    public static final int FREESTYLE_CROP_MODE_ENABLE = 1; // 启用自由裁剪
    public static final int FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH = 2; // 启用并支持触摸穿透
    public static final int DEFAULT_FREESTYLE_CROP_MODE = FREESTYLE_CROP_MODE_DISABLE; // 默认模式

    public OverlayView(Context context) {
        this(context, null);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化：尺寸、系统版本兼容处理
     */
    protected void init() {
        mTouchPointThreshold = getResources().getDimensionPixelSize(R.dimen.gallery_dp_30);
        mCropRectMinSize = getResources().getDimensionPixelSize(R.dimen.gallery_dp_100);
        mCropRectCornerTouchAreaLineLength = getResources().getDimensionPixelSize(R.dimen.gallery_dp_10);
    }

    /**
     * View布局完成，计算宽高
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            left = getPaddingLeft();
            top = getPaddingTop();
            right = getWidth() - getPaddingRight();
            bottom = getHeight() - getPaddingBottom();
            mThisWidth = right - left;
            mThisHeight = bottom - top;
            if (mShouldSetupCropBounds) {
                mShouldSetupCropBounds = false;
                setTargetAspectRatio(mTargetAspectRatio);
            }
        }
    }

    /**
     * 绘制：半透明背景 + 裁剪网格 + 裁剪框
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawDimmedLayer(canvas);
        drawCropGrid(canvas);
    }

    /**
     * 触摸事件：处理自由裁剪模式下的裁剪框拖拽、缩放
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCropViewRect.isEmpty() || mFreestyleCropMode == FREESTYLE_CROP_MODE_DISABLE) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        // 手指按下：判断是否触摸到裁剪框角点
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            mCurrentTouchCornerIndex = getCurrentTouchIndex(x, y);
            boolean shouldHandle = mCurrentTouchCornerIndex != -1;
            if (!shouldHandle) {
                mPreviousTouchX = -1;
                mPreviousTouchY = -1;
            } else if (mPreviousTouchX < 0) {
                mPreviousTouchX = x;
                mPreviousTouchY = y;
            }
            return shouldHandle;
        }
        // 手指移动：更新裁剪框大小/位置
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() == 1 && mCurrentTouchCornerIndex != -1) {
                // 限制触摸范围在View内
                x = Math.min(Math.max(x, getPaddingLeft()), getWidth() - getPaddingRight());
                y = Math.min(Math.max(y, getPaddingTop()), getHeight() - getPaddingBottom());
                updateCropViewRect(x, y);
                mPreviousTouchX = x;
                mPreviousTouchY = y;
                return true;
            }
        }
        // 手指抬起：重置状态，通知裁剪框变化
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            mPreviousTouchX = -1;
            mPreviousTouchY = -1;
            mCurrentTouchCornerIndex = -1;
            if (mCallback != null) {
                mCallback.onCropRectUpdated(mCropViewRect);
            }
        }
        return false;
    }

    /**
     * 根据触摸点更新裁剪框矩形（缩放/移动）
     * 角点顺序：0左上 1右上 2右下 3左下 4内部拖动
     */
    private void updateCropViewRect(float touchX, float touchY) {
        mTempRect.set(mCropViewRect);
        switch (mCurrentTouchCornerIndex) {
            // 缩放裁剪框
            case 0:
                mTempRect.set(touchX, touchY, mCropViewRect.right, mCropViewRect.bottom);
                break;
            case 1:
                mTempRect.set(mCropViewRect.left, touchY, touchX, mCropViewRect.bottom);
                break;
            case 2:
                mTempRect.set(mCropViewRect.left, mCropViewRect.top, touchX, touchY);
                break;
            case 3:
                mTempRect.set(touchX, mCropViewRect.top, mCropViewRect.right, touchY);
                break;
            // 移动整个裁剪框
            case 4:
                mTempRect.offset(touchX - mPreviousTouchX, touchY - mPreviousTouchY);
                if (mTempRect.left > getLeft() && mTempRect.top > getTop() && mTempRect.right < getRight() && mTempRect.bottom < getBottom()) {
                    mCropViewRect.set(mTempRect);
                    updateGridPoints();
                    postInvalidate();
                }
                return;
        }
        // 限制最小尺寸
        boolean changeHeight = mTempRect.height() >= mCropRectMinSize;
        boolean changeWidth = mTempRect.width() >= mCropRectMinSize;
        mCropViewRect.set(changeWidth ? mTempRect.left : mCropViewRect.left, changeHeight ? mTempRect.top : mCropViewRect.top, changeWidth ? mTempRect.right : mCropViewRect.right, changeHeight ? mTempRect.bottom : mCropViewRect.bottom);
        if (changeHeight || changeWidth) {
            updateGridPoints();
            postInvalidate();
        }
    }

    /**
     * 获取触摸点对应的裁剪框角点索引
     */
    private int getCurrentTouchIndex(float touchX, float touchY) {
        int closestPointIndex = -1;
        double closestPointDistance = mTouchPointThreshold;
        for (int i = 0; i < 8; i += 2) {
            // 计算触摸点到四个角的距离
            double distanceToCorner = Math.sqrt(Math.pow(touchX - mCropGridCorners[i], 2) + Math.pow(touchY - mCropGridCorners[i + 1], 2));
            if (distanceToCorner < closestPointDistance) {
                closestPointDistance = distanceToCorner;
                closestPointIndex = i / 2;
            }
        }
        // 在裁剪框内部且允许自由裁剪 → 返回拖动模式
        if (mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE && closestPointIndex < 0 && mCropViewRect.contains(touchX, touchY)) {
            return 4;
        }
        return closestPointIndex;
    }

    /**
     * 绘制半透明遮罩层（圆形/矩形）
     */
    protected void drawDimmedLayer(@NonNull Canvas canvas) {
        canvas.save();
        // 裁剪掉中间区域，只留四周半透明
        if (mCircleDimmedLayer) {
            canvas.clipPath(mCircularPath, Region.Op.DIFFERENCE);
        } else {
            canvas.clipRect(mCropViewRect, Region.Op.DIFFERENCE);
        }
        canvas.drawColor(mDimmedColor);
        canvas.restore();
        // 圆形遮罩绘制1px边框修复抗锯齿
        if (mCircleDimmedLayer) {
            canvas.drawCircle(mCropViewRect.centerX(), mCropViewRect.centerY(), Math.min(mCropViewRect.width(), mCropViewRect.height()) / 2.f, mDimmedStrokePaint);
        }
    }

    /**
     * 绘制裁剪网格 + 裁剪框 + 角点
     */
    protected void drawCropGrid(@NonNull Canvas canvas) {
        // 绘制网格线
        if (mShowCropGrid) {
            if (mGridPoints == null && !mCropViewRect.isEmpty()) {
                mGridPoints = new float[(mCropGridRowCount) * 4 + (mCropGridColumnCount) * 4];
                int index = 0;
                // 绘制水平网格线
                for (int i = 0; i < mCropGridRowCount; i++) {
                    mGridPoints[index++] = mCropViewRect.left;
                    mGridPoints[index++] = (mCropViewRect.height() * (((float) i + 1.0f) / (float) (mCropGridRowCount + 1))) + mCropViewRect.top;
                    mGridPoints[index++] = mCropViewRect.right;
                    mGridPoints[index++] = (mCropViewRect.height() * (((float) i + 1.0f) / (float) (mCropGridRowCount + 1))) + mCropViewRect.top;
                }
                // 绘制垂直网格线
                for (int i = 0; i < mCropGridColumnCount; i++) {
                    mGridPoints[index++] = (mCropViewRect.width() * (((float) i + 1.0f) / (float) (mCropGridColumnCount + 1))) + mCropViewRect.left;
                    mGridPoints[index++] = mCropViewRect.top;
                    mGridPoints[index++] = (mCropViewRect.width() * (((float) i + 1.0f) / (float) (mCropGridColumnCount + 1))) + mCropViewRect.left;
                    mGridPoints[index++] = mCropViewRect.bottom;
                }
            }
            if (mGridPoints != null) {
                canvas.drawLines(mGridPoints, mCropGridPaint);
            }
        }
        // 绘制裁剪框
        if (mShowCropFrame) {
            canvas.drawRect(mCropViewRect, mCropFramePaint);
        }
        // 自由模式下绘制角点
        if (mFreestyleCropMode != FREESTYLE_CROP_MODE_DISABLE) {
            canvas.save();
            // 只绘制四个角，不绘制边
            mTempRect.set(mCropViewRect);
            mTempRect.inset(mCropRectCornerTouchAreaLineLength, -mCropRectCornerTouchAreaLineLength);
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE);
            mTempRect.set(mCropViewRect);
            mTempRect.inset(-mCropRectCornerTouchAreaLineLength, mCropRectCornerTouchAreaLineLength);
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE);
            canvas.drawRect(mCropViewRect, mCropFrameCornersPaint);
            canvas.restore();
        }
    }

    /**
     * 解析XML自定义属性
     */
    public void processStyledAttributes(@NonNull TypedArray a) {
        mCircleDimmedLayer = a.getBoolean(R.styleable.durban_CropView_durban_circle_dimmed_layer, false);
        mDimmedColor = a.getColor(R.styleable.durban_CropView_durban_dimmed_color, ContextCompat.getColor(getContext(), R.color.durbanCropDimmed));
        mDimmedStrokePaint.setColor(mDimmedColor);
        mDimmedStrokePaint.setStyle(Paint.Style.STROKE);
        mDimmedStrokePaint.setStrokeWidth(1);
        initCropFrameStyle(a);
        mShowCropFrame = a.getBoolean(R.styleable.durban_CropView_durban_show_frame, true);
        initCropGridStyle(a);
        mShowCropGrid = a.getBoolean(R.styleable.durban_CropView_durban_show_grid, true);
    }

    /**
     * 初始化裁剪框画笔
     */
    private void initCropFrameStyle(@NonNull TypedArray a) {
        int cropFrameStrokeSize = a.getDimensionPixelSize(R.styleable.durban_CropView_durban_frame_stroke_size, getResources().getDimensionPixelSize(R.dimen.gallery_dp_1));
        int cropFrameColor = a.getColor(R.styleable.durban_CropView_durban_frame_color, ContextCompat.getColor(getContext(), R.color.durbanCropFrameLine));
        mCropFramePaint.setStrokeWidth(cropFrameStrokeSize);
        mCropFramePaint.setColor(cropFrameColor);
        mCropFramePaint.setStyle(Paint.Style.STROKE);
        mCropFrameCornersPaint.setStrokeWidth(cropFrameStrokeSize * 3);
        mCropFrameCornersPaint.setColor(cropFrameColor);
        mCropFrameCornersPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 初始化裁剪框画笔
     */
    private void initCropGridStyle(@NonNull TypedArray a) {
        int cropGridStrokeSize = a.getDimensionPixelSize(R.styleable.durban_CropView_durban_grid_stroke_size, getResources().getDimensionPixelSize(R.dimen.gallery_dp_1));
        int cropGridColor = a.getColor(R.styleable.durban_CropView_durban_grid_color, ContextCompat.getColor(getContext(), R.color.durbanCropGridLine));
        mCropGridPaint.setStrokeWidth(cropGridStrokeSize);
        mCropGridPaint.setColor(cropGridColor);
        mCropGridRowCount = a.getInt(R.styleable.durban_CropView_durban_grid_row_count, 2);
        mCropGridColumnCount = a.getInt(R.styleable.durban_CropView_durban_grid_column_count, 2);
    }

    /**
     * 获取裁剪框矩形
     */
    @NonNull
    public RectF getCropViewRect() {
        return mCropViewRect;
    }

    /**
     * @deprecated 已废弃，使用getFreestyleCropMode
     */
    @Deprecated
    public boolean isFreestyleCropEnabled() {
        return mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE;
    }

    @Deprecated
    public void setFreestyleCropEnabled(boolean freestyleCropEnabled) {
        mFreestyleCropMode = freestyleCropEnabled ? FREESTYLE_CROP_MODE_ENABLE : FREESTYLE_CROP_MODE_DISABLE;
    }

    /**
     * 获取/设置自由裁剪模式
     */
    @FreestyleMode
    public int getFreestyleCropMode() {
        return mFreestyleCropMode;
    }

    public void setFreestyleCropMode(@FreestyleMode int mFreestyleCropMode) {
        this.mFreestyleCropMode = mFreestyleCropMode;
        postInvalidate();
    }

    /**
     * 设置是否圆形遮罩
     */
    public void setCircleDimmedLayer(boolean circleDimmedLayer) {
        mCircleDimmedLayer = circleDimmedLayer;
    }

    /**
     * 设置裁剪网格行数
     */
    public void setCropGridRowCount(@IntRange(from = 0) int cropGridRowCount) {
        mCropGridRowCount = cropGridRowCount;
        mGridPoints = null;
    }

    /**
     * 设置裁剪网格列数
     */
    public void setCropGridColumnCount(@IntRange(from = 0) int cropGridColumnCount) {
        mCropGridColumnCount = cropGridColumnCount;
        mGridPoints = null;
    }

    /**
     * 设置是否显示裁剪框
     */
    public void setShowCropFrame(boolean showCropFrame) {
        mShowCropFrame = showCropFrame;
    }

    /**
     * 设置是否显示裁剪网格
     */
    public void setShowCropGrid(boolean showCropGrid) {
        mShowCropGrid = showCropGrid;
    }

    /**
     * 设置遮罩颜色
     */
    public void setDimmedColor(@ColorInt int dimmedColor) {
        mDimmedColor = dimmedColor;
    }

    /**
     * 设置裁剪框线条宽度
     */
    public void setCropFrameStrokeWidth(@IntRange(from = 0) int width) {
        mCropFramePaint.setStrokeWidth(width);
    }

    /**
     * 设置网格线条宽度
     */
    public void setCropGridStrokeWidth(@IntRange(from = 0) int width) {
        mCropGridPaint.setStrokeWidth(width);
    }

    /**
     * 设置裁剪框颜色
     */
    public void setCropFrameColor(@ColorInt int color) {
        mCropFramePaint.setColor(color);
    }

    /**
     * 设置网格颜色
     */
    public void setCropGridColor(@ColorInt int color) {
        mCropGridPaint.setColor(color);
    }

    /**
     * 设置裁剪框宽高比
     */
    public void setTargetAspectRatio(final float targetAspectRatio) {
        mTargetAspectRatio = targetAspectRatio;
        if (mThisWidth > 0) {
            setupCropBounds();
            postInvalidate();
        } else {
            mShouldSetupCropBounds = true;
        }
    }

    /**
     * 根据宽高比计算并设置裁剪框位置大小
     */
    public void setupCropBounds() {
        int height = (int) (mThisWidth / mTargetAspectRatio);
        if (height > mThisHeight) {
            int width = (int) (mThisHeight * mTargetAspectRatio);
            int halfDiff = (mThisWidth - width) / 2;
            mCropViewRect.set(getPaddingLeft() + halfDiff, getPaddingTop(), getPaddingLeft() + width + halfDiff, getPaddingTop() + mThisHeight);
        } else {
            int halfDiff = (mThisHeight - height) / 2;
            mCropViewRect.set(getPaddingLeft(), getPaddingTop() + halfDiff, getPaddingLeft() + mThisWidth, getPaddingTop() + height + halfDiff);
        }
        if (mCallback != null) {
            mCallback.onCropRectUpdated(mCropViewRect);
        }
        updateGridPoints();
    }

    /**
     * 更新裁剪框角点、中心点、圆形路径
     */
    private void updateGridPoints() {
        mCropGridCorners = RectUtil.getCornersFromRect(mCropViewRect);
        mCropGridCenter = RectUtil.getCenterFromRect(mCropViewRect);
        mGridPoints = null;
        mCircularPath.reset();
        mCircularPath.addCircle(mCropViewRect.centerX(), mCropViewRect.centerY(), Math.min(mCropViewRect.width(), mCropViewRect.height()) / 2.f, Path.Direction.CW);
    }

    /**
     * 设置裁剪框变化监听
     */
    public void setOverlayViewChangeListener(OverlayViewChangeListener callback) {
        mCallback = callback;
    }

    /**
     * 自由裁剪模式注解
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FREESTYLE_CROP_MODE_DISABLE, FREESTYLE_CROP_MODE_ENABLE, FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH})
    public @interface FreestyleMode {
    }

}