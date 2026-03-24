package com.yanzhenjie.loading;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/**
 * 仿Android 官方 Material Design 风格的三级渐变转圈加载动画
 */
public class LevelLoadingRenderer extends LoadingRenderer {
    // 三层颜色 + 三层圆弧角度
    private int[] mLevelColors;
    private float[] mLevelSwipeDegrees;
    // 圆弧起点、终点、整体旋转角度
    private float mStartDegrees;
    private float mEndDegrees;
    private float mGroupRotation;
    // 画笔 + 绘制区域
    private final Paint mPaint = new Paint();
    private final RectF mTempBounds = new RectF();
    // 内边距、旋转计数、原始起点终点、线条宽度、中心半径
    private float mStrokeInset;
    private float mRotationCount;
    private float mOriginStartDegrees;
    private float mOriginEndDegrees;
    private float mStrokeWidth;
    private float mCenterRadius;
    // 4 个插值器：匀速、快出慢进、加速、减速
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    // 动画分段
    private static final int NUM_POINTS = 5;
    // 一圈360度
    private static final int DEGREE_360 = 360;
    // 最大圆弧 288度
    private static final float MAX_SWIPE_DEGREES = 0.8f * DEGREE_360;
    // 整体每次转3圈
    private static final float FULL_GROUP_ROTATION = 3.0f * DEGREE_360;
    // 动画计算参数
    private static final float START_TRIM_DURATION_OFFSET = 0.5f;
    private static final float END_TRIM_DURATION_OFFSET = 1.0f;
    private static final float DEFAULT_CENTER_RADIUS = 12.5f;
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;
    private static final int[] DEFAULT_LEVEL_COLORS = new int[]{Color.parseColor("#55ffffff"), Color.parseColor("#b1ffffff"), Color.parseColor("#ffffffff")};
    private static final float[] LEVEL_SWEEP_ANGLE_OFFSETS = new float[]{1.0f, 7.0f / 8.0f, 5.0f / 8.0f};

    /**
     * 构造方法 + 初始化
     */
    public LevelLoadingRenderer(Context context) {
        super(context);
        init(context);
        setupPaint();
        addRenderListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animator) {
                super.onAnimationRepeat(animator);
                storeOriginals();
                mStartDegrees = mEndDegrees;
                mRotationCount = (mRotationCount + 1) % (NUM_POINTS);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRotationCount = 0;
            }
        });
    }

    /**
     * 绘制方法 (真正把动画画出来)
     */
    @Override
    protected void draw(Canvas canvas) {
        int saveCount = canvas.save();
        mTempBounds.set(mBounds);
        mTempBounds.inset(mStrokeInset, mStrokeInset);
        canvas.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY());
        for (int i = 0; i < 3; i++) {
            if (mLevelSwipeDegrees[i] != 0) {
                mPaint.setColor(mLevelColors[i]);
                canvas.drawArc(mTempBounds, mEndDegrees, mLevelSwipeDegrees[i], false, mPaint);
            }
        }
        canvas.restoreToCount(saveCount);
    }

    /**
     * 动画计算 (根据进度计算圆弧角度)
     */
    @Override
    protected void computeRender(float renderProgress) {
        // 前 50% 进度：移动【起点】
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            float startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET;
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress);
            float mSwipeDegrees = mEndDegrees - mStartDegrees;
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;
            float level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);
            float level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress);
            mLevelSwipeDegrees[0] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[0] * (1.0f + level1Increment);
            mLevelSwipeDegrees[1] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[1] * 1.0f;
            mLevelSwipeDegrees[2] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[2] * (1.0f + level3Increment);
        }
        // 后 50% 进度：移动【终点】
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            float endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET);
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress);
            float mSwipeDegrees = mEndDegrees - mStartDegrees;
            float levelSwipeDegreesProgress = Math.abs(mSwipeDegrees) / MAX_SWIPE_DEGREES;
            if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[1]) {
                mLevelSwipeDegrees[0] = -mSwipeDegrees;
                mLevelSwipeDegrees[1] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[1];
                mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
            } else if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[2]) {
                mLevelSwipeDegrees[0] = 0;
                mLevelSwipeDegrees[1] = -mSwipeDegrees;
                mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2];
            } else {
                mLevelSwipeDegrees[0] = 0;
                mLevelSwipeDegrees[1] = 0;
                mLevelSwipeDegrees[2] = -mSwipeDegrees;
            }
        }
        // 整体旋转角度
        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS));
    }

    @Override
    protected void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    protected void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    protected void reset() {
        resetOriginals();
    }

    /**
     * 初始化大小、颜色数组
     */
    private void init(Context context) {
        mStrokeWidth = DensityUtils.dip2px(context, DEFAULT_STROKE_WIDTH);
        mCenterRadius = DensityUtils.dip2px(context, DEFAULT_CENTER_RADIUS);
        mLevelSwipeDegrees = new float[3];
        mLevelColors = DEFAULT_LEVEL_COLORS;
    }

    /**
     * 设置画笔（抗锯齿、圆角、线条宽度）
     */
    private void setupPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        initStrokeInset((int) mWidth, (int) mHeight);
    }

    /**
     * 计算居中内边距 (让圆环居中显示)
     */
    private void initStrokeInset(float width, float height) {
        float minSize = Math.min(width, height);
        float strokeInset = minSize / 2.0f - mCenterRadius;
        float minStrokeInset = (float) Math.ceil(mStrokeWidth / 2.0f);
        mStrokeInset = Math.max(strokeInset, minStrokeInset);
    }

    /**
     * 保存上一次的终点角度
     */
    private void storeOriginals() {
        mOriginEndDegrees = mEndDegrees;
        mOriginStartDegrees = mEndDegrees;
    }

    /**
     * 所有角度归零
     */
    private void resetOriginals() {
        mOriginEndDegrees = 0;
        mOriginStartDegrees = 0;
        mEndDegrees = 0;
        mStartDegrees = 0;
        mLevelSwipeDegrees[0] = 0;
        mLevelSwipeDegrees[1] = 0;
        mLevelSwipeDegrees[2] = 0;
    }

    /**
     * 添加外部配置类
     */
    private void apply(Builder builder) {
        this.mWidth = builder.mWidth > 0 ? builder.mWidth : this.mWidth;
        this.mHeight = builder.mHeight > 0 ? builder.mHeight : this.mHeight;
        this.mStrokeWidth = builder.mStrokeWidth > 0 ? builder.mStrokeWidth : this.mStrokeWidth;
        this.mCenterRadius = builder.mCenterRadius > 0 ? builder.mCenterRadius : this.mCenterRadius;
        this.mDuration = builder.mDuration > 0 ? builder.mDuration : this.mDuration;
        this.mLevelColors = builder.mLevelColors != null ? builder.mLevelColors : this.mLevelColors;
        setupPaint();
        initStrokeInset(this.mWidth, this.mHeight);
    }

    /**
     * 设置三段圆弧颜色
     */
    public void setCircleColors(int r1, int r2, int r3) {
        mLevelColors = new int[]{r1, r2, r3};
    }

    /**
     * 外部配置动画
     */
    public static class Builder {
        private int mWidth;
        private int mHeight;
        private int mStrokeWidth;
        private int mCenterRadius;
        private int mDuration;
        private int[] mLevelColors;
        private final Context mContext;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        public Builder setStrokeWidth(int strokeWidth) {
            this.mStrokeWidth = strokeWidth;
            return this;
        }

        public Builder setCenterRadius(int centerRadius) {
            this.mCenterRadius = centerRadius;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setLevelColors(int[] colors) {
            this.mLevelColors = colors;
            return this;
        }

        public Builder setLevelColor(int color) {
            return setLevelColors(new int[]{oneThirdAlphaColor(color), twoThirdAlphaColor(color), color});
        }

        public LevelLoadingRenderer build() {
            LevelLoadingRenderer loadingRenderer = new LevelLoadingRenderer(mContext);
            loadingRenderer.apply(this);
            return loadingRenderer;
        }

        private int oneThirdAlphaColor(int colorValue) {
            int startA = (colorValue >> 24) & 0xff;
            int startR = (colorValue >> 16) & 0xff;
            int startG = (colorValue >> 8) & 0xff;
            int startB = colorValue & 0xff;
            return (startA / 3 << 24) | (startR << 16) | (startG << 8) | startB;
        }

        private int twoThirdAlphaColor(int colorValue) {
            int startA = (colorValue >> 24) & 0xff;
            int startR = (colorValue >> 16) & 0xff;
            int startG = (colorValue >> 8) & 0xff;
            int startB = colorValue & 0xff;
            return (startA * 2 / 3 << 24) | (startR << 16) | (startG << 8) | startB;
        }
    }

}