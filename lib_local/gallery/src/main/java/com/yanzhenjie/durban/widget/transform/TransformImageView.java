package com.yanzhenjie.durban.widget.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.yanzhenjie.durban.app.data.BitmapLoadCallback;
import com.yanzhenjie.durban.app.data.BitmapLoadTask;
import com.yanzhenjie.durban.model.ExifInfo;
import com.yanzhenjie.durban.utils.BitmapLoadUtil;
import com.yanzhenjie.durban.utils.RectUtil;

/**
 * 图片变换【基类】
 * 功能：图片加载、矩阵变换（移动/缩放/旋转）、坐标计算
 * 所有裁剪View的父类
 */
public class TransformImageView extends AppCompatImageView {
    // 图片最大尺寸（防止OOM）
    private int mMaxBitmapSize = 0;
    // 图片路径 / 输出目录 / 图片信息（旋转、翻转）
    private String mImagePath, mOutputDirectory;
    private ExifInfo mExifInfo;
    // 图片初始状态：四个角坐标、中心点坐标
    private float[] mInitialImageCorners;
    private float[] mInitialImageCenter;
    // 加载线程
    private BitmapLoadTask mBitmapLoadTask;
    // 常量：坐标点数量
    private static final String TAG = "TransformImageView";
    // 常量：坐标点数量
    private static final int RECT_CORNER_POINTS_COORDS = 8;
    // 中心点 → x,y
    private static final int RECT_CENTER_POINT_COORDS = 2;
    // 矩阵一共9个值
    private static final int MATRIX_VALUES_COUNT = 9;
    // 临时存储矩阵数据（避免重复创建对象）
    private final float[] mMatrixValues = new float[MATRIX_VALUES_COUNT];
    // View自身宽高
    protected int mThisWidth, mThisHeight;
    // 状态标记：是否解码完成 / 是否布局完成
    protected boolean mBitmapDecoded = false;
    protected boolean mBitmapLaidOut = false;
    // 图片变换监听（加载、旋转、缩放）
    protected TransformImageListener mTransformImageListener;
    // 当前图片矩阵（核心：所有变换都存在这里）
    protected Matrix mCurrentImageMatrix = new Matrix();
    // 当前图片四个角坐标 / 中心点坐标
    protected final float[] mCurrentImageCorners = new float[RECT_CORNER_POINTS_COORDS];
    protected final float[] mCurrentImageCenter = new float[RECT_CENTER_POINT_COORDS];

    public TransformImageView(Context context) {
        this(context, null);
    }

    public TransformImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化设置
     */
    protected void init() {
        setScaleType(ScaleType.MATRIX);
    }

    /**
     * 强制使用 MATRIX 模式
     * 因为只有矩阵才能自由缩放旋转
     */
    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType);
        } else {
            Log.w(TAG, "Invalid ScaleType. Only ScaleType.MATRIX can be used");
        }
    }

    /**
     * 设置 Bitmap
     */
    @Override
    public void setImageBitmap(final Bitmap bitmap) {
        setImageDrawable(new FastBitmapDrawable(bitmap));
    }

    /**
     * 设置矩阵，并更新坐标
     */
    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        mCurrentImageMatrix.set(matrix);
        updateCurrentImagePoints();
    }

    /**
     * 根据矩阵，实时更新图片四个角和中心点
     */
    private void updateCurrentImagePoints() {
        mCurrentImageMatrix.mapPoints(mCurrentImageCorners, mInitialImageCorners);
        mCurrentImageMatrix.mapPoints(mCurrentImageCenter, mInitialImageCenter);
    }

    /**
     * View布局完成
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || (mBitmapDecoded && !mBitmapLaidOut)) {
            left = getPaddingLeft();
            top = getPaddingTop();
            right = getWidth() - getPaddingRight();
            bottom = getHeight() - getPaddingBottom();
            mThisWidth = right - left;
            mThisHeight = bottom - top;
            onImageLaidOut();
        }
    }

    /**
     * 销毁
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmapLoadTask != null) {
            mBitmapLoadTask.cancel(true);
            mBitmapLoadTask = null;
        }
    }

    /**
     * 图片布局完成 → 记录初始坐标
     */
    protected void onImageLaidOut() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        float w = drawable.getIntrinsicWidth();
        float h = drawable.getIntrinsicHeight();
        Log.d(TAG, String.format("Image size: [%d:%d]", (int) w, (int) h));
        RectF initialImageRect = new RectF(0, 0, w, h);
        mInitialImageCorners = RectUtil.getCornersFromRect(initialImageRect);
        mInitialImageCenter = RectUtil.getCenterFromRect(initialImageRect);
        mBitmapLaidOut = true;
        if (mTransformImageListener != null) {
            mTransformImageListener.onLoadComplete();
        }
    }

    /**
     * 设置/获取 图片最大尺寸
     */
    public void setMaxBitmapSize(int maxBitmapSize) {
        mMaxBitmapSize = maxBitmapSize;
    }

    public int getMaxBitmapSize() {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = BitmapLoadUtil.calculateMaxBitmapSize(getContext());
        }
        return mMaxBitmapSize;
    }

    /**
     * 获取 / 加载 图片路径 (异步)
     */
    public void setImagePath(@NonNull String inputImagePath) throws Exception {
        mImagePath = inputImagePath;
        int maxBitmapSize = getMaxBitmapSize();
        if (mBitmapLoadTask != null) {
            mBitmapLoadTask.cancel(true);
            mBitmapLoadTask = null;
        }
        mBitmapLoadTask = new BitmapLoadTask(getContext(), maxBitmapSize, maxBitmapSize, new BitmapLoadCallback() {
            @Override
            public void onSuccessfully(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo) {
                mExifInfo = exifInfo;
                mBitmapDecoded = true;
                setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {
                if (mTransformImageListener != null) {
                    mTransformImageListener.onLoadFailure();
                }
            }
        });
        mBitmapLoadTask.execute(inputImagePath);
    }

    public String getImagePath() {
        return mImagePath;
    }

    /**
     * 获取路径、目录、图片信息
     */
    public void setOutputDirectory(String outputDirectory) {
        mOutputDirectory = outputDirectory;
    }

    public String getOutputDirectory() {
        return mOutputDirectory;
    }

    public ExifInfo getExifInfo() {
        return mExifInfo;
    }

    /**
     * 获取当前缩放倍数
     */
    public float getCurrentScale() {
        return getMatrixScale(mCurrentImageMatrix);
    }

    /**
     * 获取当前旋转角度
     */
    public float getCurrentAngle() {
        return getMatrixAngle(mCurrentImageMatrix);
    }

    /**
     * 公式计算：矩阵 → 缩放值
     */
    public float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2) + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    /**
     * 公式计算：矩阵 → 角度
     */
    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X), getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    /**
     * 获取矩阵中某个值
     */
    private float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = MATRIX_VALUES_COUNT) int valueIndex) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }

    /**
     * 获取当前显示的Bitmap
     */
    @Nullable
    public Bitmap getViewBitmap() {
        if (getDrawable() == null || !(getDrawable() instanceof FastBitmapDrawable)) {
            return null;
        } else {
            return ((FastBitmapDrawable) getDrawable()).getBitmap();
        }
    }

    /**
     * 移动图片
     */
    public void postTranslate(float deltaX, float deltaY) {
        if (deltaX != 0 || deltaY != 0) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY);
            setImageMatrix(mCurrentImageMatrix);
        }
    }

    /**
     * 缩放图片
     */
    public void postScale(float deltaScale, float px, float py) {
        if (deltaScale != 0) {
            mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py);
            setImageMatrix(mCurrentImageMatrix);
            if (mTransformImageListener != null) {
                mTransformImageListener.onScale(getMatrixScale(mCurrentImageMatrix));
            }
        }
    }

    /**
     * 旋转图片
     */
    public void postRotate(float deltaAngle, float px, float py) {
        if (deltaAngle != 0) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py);
            setImageMatrix(mCurrentImageMatrix);
            if (mTransformImageListener != null) {
                mTransformImageListener.onRotate(getMatrixAngle(mCurrentImageMatrix));
            }
        }
    }

    /**
     * 设置监听
     */
    public void setTransformImageListener(TransformImageListener transformImageListener) {
        mTransformImageListener = transformImageListener;
    }

    /**
     * 图片状态监听接口
     */
    public interface TransformImageListener {
        /**
         * 加载完成
         */
        void onLoadComplete();

        /**
         * 加载失败
         */
        void onLoadFailure();

        /**
         * 旋转回调
         */
        void onRotate(float currentAngle);

        /**
         * 缩放回调
         */
        void onScale(float currentScale);
    }

}