/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.durban;

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
import androidx.core.view.ViewCompat;

import com.example.gallery.R;
import com.yanzhenjie.album.mvp.BaseActivity;
import com.yanzhenjie.durban.callback.BitmapCropCallback;
import com.yanzhenjie.durban.view.CropView;
import com.yanzhenjie.durban.view.GestureCropImageView;
import com.yanzhenjie.durban.view.OverlayView;
import com.yanzhenjie.durban.view.TransformImageView;

import java.util.ArrayList;

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
public class DurbanActivity extends BaseActivity {
    private int mStatusColor;
    private int mNavigationColor;
    private String mTitle;

    private int mGesture;
    private float[] mAspectRatio;
    private int[] mMaxWidthHeight;

    private Bitmap.CompressFormat mCompressFormat;
    private int mCompressQuality;

    private String mOutputDirectory;
    private ArrayList<String> mInputPathList;

    private Controller mController;
    private CropView mCropView;
    private GestureCropImageView mCropImageView;
    private ArrayList<String> mOutputPathList;

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.durban_activity_photobox);

        final Intent intent = getIntent();
        initArgument(intent);
        initFrameViews();
        initContentViews();
        initControllerViews();
        cropNextImage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCropImageView != null) mCropImageView.cancelAllAnimations();
    }

    /**
     * 处理页面传输而来的参数
     */
    private void initArgument(Intent intent) {
        mStatusColor = R.color.durban_ColorPrimaryDark;
        mNavigationColor = R.color.durban_ColorPrimaryBlack;

        mStatusColor = intent.getIntExtra(Durban.KEY_INPUT_STATUS_COLOR, mStatusColor);
        mNavigationColor = intent.getIntExtra(Durban.KEY_INPUT_NAVIGATION_COLOR, mNavigationColor);
        mTitle = intent.getStringExtra(Durban.KEY_INPUT_TITLE);
        if (TextUtils.isEmpty(mTitle)) mTitle = getString(R.string.durban_title_crop);

        mGesture = intent.getIntExtra(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL);
        mAspectRatio = intent.getFloatArrayExtra(Durban.KEY_INPUT_ASPECT_RATIO);
        if (mAspectRatio == null) mAspectRatio = new float[]{0, 0};
        mMaxWidthHeight = intent.getIntArrayExtra(Durban.KEY_INPUT_MAX_WIDTH_HEIGHT);
        if (mMaxWidthHeight == null) mMaxWidthHeight = new int[]{500, 500};

        //noinspection JavacQuirks
        int compressFormat = intent.getIntExtra(Durban.KEY_INPUT_COMPRESS_FORMAT, 0);
        mCompressFormat = compressFormat == Durban.COMPRESS_PNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        mCompressQuality = intent.getIntExtra(Durban.KEY_INPUT_COMPRESS_QUALITY, 90);

        mOutputDirectory = intent.getStringExtra(Durban.KEY_INPUT_DIRECTORY);
        if (TextUtils.isEmpty(mOutputDirectory)) mOutputDirectory = getFilesDir().getAbsolutePath();
        mInputPathList = intent.getStringArrayListExtra(Durban.KEY_INPUT_PATH_ARRAY);

        mController = intent.getParcelableExtra(Durban.KEY_INPUT_CONTROLLER);
        if (mController == null) mController = Controller.newBuilder().build();

        mOutputPathList = new ArrayList<>();
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
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true); // 显示返回键
        actionBar.setTitle("");

        // 设置Toolbar样式
        setSupportToolbar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, mStatusColor));

        // 设置图标样式
        boolean statusBarBattery = getBatteryIcon(mStatusColor);
        boolean navigationBarBattery = getBatteryIcon(mNavigationColor);
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mNavigationColor);

        // 设置标题
        final TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(mTitle);
        if (statusBarBattery) {
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.textWhite));
        } else {
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.textBlack));
        }

        // 设置返回按钮
        Drawable navigationIcon = ContextCompat.getDrawable(this, R.drawable.durban_ic_back_white);
        assert navigationIcon != null;
        if (!statusBarBattery) {
            navigationIcon.setTint(ContextCompat.getColor(this, R.color.bgBlack));
        }
        toolbar.setNavigationIcon(navigationIcon);
    }

    private void initContentViews() {
        mCropView = findViewById(R.id.crop_view);
        mCropImageView = mCropView.getCropImageView();
        mCropImageView.setOutputDirectory(mOutputDirectory);
        mCropImageView.setTransformImageListener(mImageListener);
        mCropImageView.setScaleEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE);
        mCropImageView.setRotateEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE);
        // Durban image view options
        mCropImageView.setMaxBitmapSize(GestureCropImageView.DEFAULT_MAX_BITMAP_SIZE);
        mCropImageView.setMaxScaleMultiplier(GestureCropImageView.DEFAULT_MAX_SCALE_MULTIPLIER);
        mCropImageView.setImageToWrapCropBoundsAnimDuration(GestureCropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION);

        // Overlay view options
        OverlayView overlayView = mCropView.getOverlayView();
        overlayView.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_DISABLE);
        overlayView.setDimmedColor(ContextCompat.getColor(this, R.color.durban_CropDimmed));
        overlayView.setCircleDimmedLayer(false);
        overlayView.setShowCropFrame(true);
        overlayView.setCropFrameColor(ContextCompat.getColor(this, R.color.durban_CropFrameLine));
        overlayView.setCropFrameStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1));
        overlayView.setShowCropGrid(true);
        overlayView.setCropGridRowCount(2);
        overlayView.setCropGridColumnCount(2);
        overlayView.setCropGridColor(ContextCompat.getColor(this, R.color.durban_CropGridLine));
        overlayView.setCropGridStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1));

        // Aspect ratio options
        if (mAspectRatio[0] > 0 && mAspectRatio[1] > 0)
            mCropImageView.setTargetAspectRatio(mAspectRatio[0] / mAspectRatio[1]);
        else mCropImageView.setTargetAspectRatio(GestureCropImageView.SOURCE_IMAGE_ASPECT_RATIO);

        // Result exception max size options
        if (mMaxWidthHeight[0] > 0 && mMaxWidthHeight[1] > 0) {
            mCropImageView.setMaxResultImageSizeX(mMaxWidthHeight[0]);
            mCropImageView.setMaxResultImageSizeY(mMaxWidthHeight[1]);
        }
    }

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
        }

        @Override
        public void onScale(float currentScale) {
        }

        @Override
        public void onLoadComplete() {
            ViewCompat.animate(mCropView)
                    .alpha(1)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator());
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

        controllerRoot.setVisibility(mController.isEnable() ? View.VISIBLE : View.GONE);
        rotationTitle.setVisibility(mController.isRotationTitle() ? View.VISIBLE : View.INVISIBLE);
        rotationLeft.setVisibility(mController.isRotation() ? View.VISIBLE : View.GONE);
        rotationRight.setVisibility(mController.isRotation() ? View.VISIBLE : View.GONE);
        scaleTitle.setVisibility(mController.isScaleTitle() ? View.VISIBLE : View.INVISIBLE);
        scaleBig.setVisibility(mController.isScale() ? View.VISIBLE : View.GONE);
        scaleSmall.setVisibility(mController.isScale() ? View.VISIBLE : View.GONE);

        if (!mController.isRotationTitle() && !mController.isScaleTitle())
            findViewById(R.id.layout_controller_title_root).setVisibility(View.GONE);
        if (!mController.isRotation())
            rotationTitle.setVisibility(View.GONE);
        if (!mController.isScale())
            scaleTitle.setVisibility(View.GONE);

        rotationLeft.setOnClickListener(mControllerClick);
        rotationRight.setOnClickListener(mControllerClick);
        scaleBig.setOnClickListener(mControllerClick);
        scaleSmall.setOnClickListener(mControllerClick);
    }

    private View.OnClickListener mControllerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.layout_controller_rotation_left) {
                mCropImageView.postRotate(-90);
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_rotation_right) {
                mCropImageView.postRotate(90);
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_scale_big) {
                mCropImageView.zoomOutImage(mCropImageView.getCurrentScale()
                        + ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10));
                mCropImageView.setImageToWrapCropBounds();
            } else if (id == R.id.layout_controller_scale_small) {
                mCropImageView.zoomInImage(mCropImageView.getCurrentScale()
                        - ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10));
                mCropImageView.setImageToWrapCropBounds();
            }
        }
    };

    /**
     * Start cropping and request permission if there is no permission.
     */
    private void cropNextImage() {
        resetRotation();
        cropNextImageWithPermission();
    }

    /**
     * Restore the rotation angle.
     */
    private void resetRotation() {
        mCropImageView.postRotate(-mCropImageView.getCurrentAngle());
        mCropImageView.setImageToWrapCropBounds();
    }

    private void cropNextImageWithPermission() {
        if (mInputPathList != null) {
            if (mInputPathList.size() > 0) {
                String currentPath = mInputPathList.remove(0);
                try {
                    mCropImageView.setImagePath(currentPath);
                } catch (Exception e) {
                    cropNextImage();
                }
            } else if (mOutputPathList.size() > 0) setResultSuccessful();
            else setResultFailure();
        } else {
            Log.e("Durban", "The file list is empty.");
            setResultFailure();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.durban_menu_activity, menu);
        if (!getBatteryIcon(mStatusColor)) {
            Drawable doneIcon = ContextCompat.getDrawable(this, R.drawable.durban_ic_done_white);
            assert doneIcon != null;
            doneIcon.setTint(ContextCompat.getColor(this, R.color.bgBlack));
            // 获取右侧菜单按钮的 MenuItem
            MenuItem okItem = menu.findItem(R.id.menu_action_ok);
            // 如果菜单按钮是自定义 View（通过 actionLayout 指定）
            View okView = okItem.getActionView();
            if (okView != null) {
                okView.setBackground(doneIcon);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_ok) {
            cropAndSaveImage();
        } else if (item.getItemId() == android.R.id.home) {
            setResultFailure();
        }
        return true;
    }

    private void cropAndSaveImage() {
        mCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, cropCallback);
    }

    private BitmapCropCallback cropCallback = new BitmapCropCallback() {
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

    private void setResultSuccessful() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setResultFailure() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}