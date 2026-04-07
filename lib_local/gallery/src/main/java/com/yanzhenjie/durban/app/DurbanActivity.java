package com.yanzhenjie.durban.app;

import static com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.gallery.R;
import com.example.gallery.base.BaseActivity;
import com.yanzhenjie.durban.Durban;
import com.yanzhenjie.durban.callback.BitmapCropCallback;
import com.yanzhenjie.durban.model.Controller;
import com.yanzhenjie.durban.widget.CropView;
import com.yanzhenjie.durban.widget.GestureCropImageView;
import com.yanzhenjie.durban.widget.OverlayView;
import com.yanzhenjie.durban.widget.TransformImageView;

import java.util.ArrayList;

/**
 * 图片裁剪页
 * 功能：接收配置 → 显示裁剪 → 旋转/缩放 → 保存 → 返回结果
 */
public class DurbanActivity extends BaseActivity {
    // 状态栏/导航栏颜色
    private int mStatusBarColor;
    private int mNavigationBarColor;
    // 手势类型：旋转/缩放
    private int mGesture;
    // 压缩质量
    private int mCompressQuality;
    // 标题
    private String mTitle;
    // 输出目录
    private String mOutputDirectory;
    // 输出最大宽高
    private int[] mMaxWidthHeight;
    // 裁剪比例
    private float[] mAspectRatio;
    // 输入/输出图片列表
    private ArrayList<String> mInputPathList;
    private ArrayList<String> mOutputPathList;
    // 压缩格式
    private Bitmap.CompressFormat mCompressFormat;
    // 底部按钮控制器
    private Controller mController;
    // 裁剪视图
    private CropView mCropView;
    private GestureCropImageView mCropImageView;

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 读取所有配置
        initArgument();
        // 设置布局
        setContentView(R.layout.durban_activity_photobox);
        // 初始化状态栏、标题栏
        initFrameViews();
        // 初始化裁剪图片视图
        initContentViews();
        // 初始化底部旋转/缩放按钮
        initControllerViews();
        // 开始裁剪第一张图
        cropNextImage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 页面不可见时，取消所有动画，防止内存泄漏
        if (mCropImageView != null) {
            mCropImageView.cancelAllAnimations();
        }
    }

    /**
     * 处理页面传输而来的参数
     */
    private void initArgument() {
        Bundle argument = getIntent().getExtras();
        if (null != argument) {
            // 状态栏/导航栏颜色
            mStatusBarColor = argument.getInt(Durban.KEY_INPUT_STATUS_COLOR, R.color.galleryStatusBar);
            mNavigationBarColor = argument.getInt(Durban.KEY_INPUT_NAVIGATION_COLOR, R.color.galleryNavigationBar);
            // 标题
            mTitle = argument.getString(Durban.KEY_INPUT_TITLE);
            if (TextUtils.isEmpty(mTitle)) {
                mTitle = getString(R.string.durban_title_crop);
            }
            // 手势：旋转 / 缩放
            mGesture = argument.getInt(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL);
            // 裁剪比例
            mAspectRatio = argument.getFloatArray(Durban.KEY_INPUT_ASPECT_RATIO);
            if (mAspectRatio == null) {
                mAspectRatio = new float[]{0, 0};
            }
            // 输出最大尺寸
            mMaxWidthHeight = argument.getIntArray(Durban.KEY_INPUT_MAX_WIDTH_HEIGHT);
            if (mMaxWidthHeight == null) {
                mMaxWidthHeight = new int[]{500, 500};
            }
            // 压缩格式
            int compressFormat = argument.getInt(Durban.KEY_INPUT_COMPRESS_FORMAT, 0);
            mCompressFormat = compressFormat == Durban.COMPRESS_PNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            // 压缩质量
            mCompressQuality = argument.getInt(Durban.KEY_INPUT_COMPRESS_QUALITY, 90);
            // 输出文件夹
            mOutputDirectory = argument.getString(Durban.KEY_INPUT_DIRECTORY);
            if (TextUtils.isEmpty(mOutputDirectory)) {
                mOutputDirectory = getFilesDir().getAbsolutePath();
            }
            // 要裁剪的图片列表
            mInputPathList = argument.getStringArrayList(Durban.KEY_INPUT_PATH_ARRAY);
            // 底部按钮配置
            mController = argument.getParcelable(Durban.KEY_INPUT_CONTROLLER);
            if (mController == null) {
                mController = Controller.newBuilder().build();
            }
            // 输出结果列表
            mOutputPathList = new ArrayList<>();
        } else {
            finish();
        }
    }

    /**
     * 初始化窗体(状态栏/导航栏)
     */
    private void initFrameViews() {
        // 获取自定义Toolbar并设置为ActionBar替代品
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 通过getSupportActionBar()操作这个Toolbar
        final ActionBar actionBar = getSupportActionBar();
        // 显示返回键
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        // 设置Toolbar样式
        setSupportToolbar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, mStatusBarColor));
        // 设置图标样式
        boolean statusBarBattery = shouldUseWhiteSystemBarsForRes(mStatusBarColor);
        boolean navigationBarBattery = shouldUseWhiteSystemBarsForRes(mNavigationBarColor);
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mNavigationBarColor);
        // 设置标题
        final TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(mTitle);
        if (statusBarBattery) {
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.galleryFontLight));
        } else {
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.galleryFontDark));
        }
        // 设置返回按钮
        Drawable navigationIcon = ContextCompat.getDrawable(this, R.mipmap.gallery_ic_done);
        if (!statusBarBattery && null != navigationIcon) {
            navigationIcon.setTint(ContextCompat.getColor(this, R.color.galleryIconDark));
        }
        toolbar.setNavigationIcon(navigationIcon);
    }

    private void initContentViews() {
        mCropView = findViewById(R.id.crop_view);
        mCropImageView = mCropView.getCropImageView();
        // 设置输出目录
        mCropImageView.setOutputDirectory(mOutputDirectory);
        // 图片加载监听
        mCropImageView.setTransformImageListener(mImageListener);
        // 是否允许缩放
        mCropImageView.setScaleEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE);
        // 是否允许旋转
        mCropImageView.setRotateEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE);
        mCropImageView.setMaxBitmapSize(GestureCropImageView.DEFAULT_MAX_BITMAP_SIZE);
        mCropImageView.setMaxScaleMultiplier(GestureCropImageView.DEFAULT_MAX_SCALE_MULTIPLIER);
        mCropImageView.setImageToWrapCropBoundsAnimDuration(GestureCropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION);
        // 裁剪视图样式
        OverlayView overlayView = mCropView.getOverlayView();
        overlayView.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_DISABLE);
        overlayView.setDimmedColor(ContextCompat.getColor(this, R.color.durbanCropDimmed));
        overlayView.setCircleDimmedLayer(false);
        overlayView.setShowCropFrame(true);
        overlayView.setCropFrameColor(ContextCompat.getColor(this, R.color.durbanCropFrameLine));
        overlayView.setCropFrameStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1));
        overlayView.setShowCropGrid(true);
        overlayView.setCropGridRowCount(2);
        overlayView.setCropGridColumnCount(2);
        overlayView.setCropGridColor(ContextCompat.getColor(this, R.color.durbanCropGridLine));
        overlayView.setCropGridStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1));
        // 设置裁剪比例
        if (mAspectRatio[0] > 0 && mAspectRatio[1] > 0) {
            mCropImageView.setTargetAspectRatio(mAspectRatio[0] / mAspectRatio[1]);
        } else {
            mCropImageView.setTargetAspectRatio(GestureCropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        }
        // 设置输出最大宽高
        if (mMaxWidthHeight[0] > 0 && mMaxWidthHeight[1] > 0) {
            mCropImageView.setMaxResultImageSizeX(mMaxWidthHeight[0]);
            mCropImageView.setMaxResultImageSizeY(mMaxWidthHeight[1]);
        }
    }

    private final TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
        }

        @Override
        public void onScale(float currentScale) {
        }

        @Override
        public void onLoadComplete() {
            mCropView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
        }

        @Override
        public void onLoadFailure() {
            cropNextImage();
        }
    };

    private void initControllerViews() {
        View controllerRoot = findViewById(R.id.iv_controller_root);
        View rotationTitle = findViewById(R.id.tv_controller_title_rotation);
        View rotationLeft = findViewById(R.id.layout_controller_rotation_left);
        View rotationRight = findViewById(R.id.layout_controller_rotation_right);
        View scaleTitle = findViewById(R.id.tv_controller_title_scale);
        View scaleBig = findViewById(R.id.layout_controller_scale_big);
        View scaleSmall = findViewById(R.id.layout_controller_scale_small);
        // 根据配置显示/隐藏按钮
        controllerRoot.setVisibility(mController.isEnable() ? View.VISIBLE : View.GONE);
        rotationTitle.setVisibility(mController.isRotationTitle() ? View.VISIBLE : View.INVISIBLE);
        rotationLeft.setVisibility(mController.isRotation() ? View.VISIBLE : View.GONE);
        rotationRight.setVisibility(mController.isRotation() ? View.VISIBLE : View.GONE);
        scaleTitle.setVisibility(mController.isScaleTitle() ? View.VISIBLE : View.INVISIBLE);
        scaleBig.setVisibility(mController.isScale() ? View.VISIBLE : View.GONE);
        scaleSmall.setVisibility(mController.isScale() ? View.VISIBLE : View.GONE);
        // 隐藏所有标题时，隐藏标题栏
        if (!mController.isRotationTitle() && !mController.isScaleTitle()) {
            findViewById(R.id.layout_controller_title_root).setVisibility(View.GONE);
        }
        if (!mController.isRotation()) {
            rotationTitle.setVisibility(View.GONE);
        }
        if (!mController.isScale()) {
            scaleTitle.setVisibility(View.GONE);
        }
        // 点击事件
        rotationLeft.setOnClickListener(mControllerClick);
        rotationRight.setOnClickListener(mControllerClick);
        scaleBig.setOnClickListener(mControllerClick);
        scaleSmall.setOnClickListener(mControllerClick);
    }

    private final View.OnClickListener mControllerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.layout_controller_rotation_left) {
                // 左转 90
                mCropImageView.postRotate(-90);
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_rotation_right) {
                // 右转 90
                mCropImageView.postRotate(90);
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_scale_big) {
                // 放大
                mCropImageView.zoomOutImage(mCropImageView.getCurrentScale() + ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10));
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_scale_small) {
                // 缩小
                mCropImageView.zoomInImage(mCropImageView.getCurrentScale() - ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10));
                mCropImageView.setImageToWrapCropBounds();
            }
        }
    };

    /**
     * 开始裁剪下一张
     */
    private void cropNextImage() {
        resetRotation();
        cropNextImageWithPermission();
    }

    /**
     * 重置图片旋转角度
     */
    private void resetRotation() {
        mCropImageView.postRotate(-mCropImageView.getCurrentAngle());
        mCropImageView.setImageToWrapCropBounds();
    }

    /**
     * 加载图片并裁剪
     */
    private void cropNextImageWithPermission() {
        if (mInputPathList != null) {
            if (!mInputPathList.isEmpty()) {
                String currentPath = mInputPathList.remove(0);
                try {
                    mCropImageView.setImagePath(currentPath);
                } catch (Exception e) {
                    // 加载失败直接下一张
                    cropNextImage();
                }
            } else {
                // 全部裁剪完成
                if (!mOutputPathList.isEmpty()) {
                    setResultSuccessful();
                } else {
                    setResultFailure();
                }
            }
        } else {
            Log.e("Durban", "The file list is empty.");
            setResultFailure();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.durban_menu_activity, menu);
        // 获取右侧菜单按钮的 MenuItem
        MenuItem okItem = menu.findItem(R.id.menu_action_ok);
        // 去除长按的文字提示
        okItem.setTitle("");
        // 根据导航栏颜色定义对应的图片
        if (!shouldUseWhiteSystemBarsForRes(mStatusBarColor)) {
            Drawable doneIcon = ContextCompat.getDrawable(this, R.mipmap.gallery_ic_done);
            if (null != doneIcon) {
                doneIcon.setTint(ContextCompat.getColor(this, R.color.galleryIconDark));
                // 如果菜单按钮是自定义 View（通过 actionLayout 指定）
                View okView = okItem.getActionView();
                if (okView != null) {
                    okView.setBackground(doneIcon);
                }
            }
        }
        // 设置额外添加的按钮外层间距
        setSupportMenuView(findViewById(R.id.toolbar), mStatusBarColor);
        return true;
    }

    /**
     * 菜单点击：确定 / 返回
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_ok) {
            // 保存
            cropAndSaveImage();
        } else if (item.getItemId() == android.R.id.home) {
            // 取消
            setResultFailure();
        }
        return true;
    }

    /**
     * 执行裁剪并保存
     */
    private void cropAndSaveImage() {
        mCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, cropCallback);
    }

    /**
     * 保存回调
     */
    private final BitmapCropCallback cropCallback = new BitmapCropCallback() {
        @Override
        public void onBitmapCropped(@NonNull String imagePath, int imageWidth, int imageHeight) {
            mOutputPathList.add(imagePath);
            cropNextImage();
        }

        @Override
        public void onCropFailure(@NonNull Throwable t) {
            cropNextImage();
        }
    };

    /**
     * 返回结果给上一页
     */
    private void setResultSuccessful() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList);
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setResultFailure() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList);
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}