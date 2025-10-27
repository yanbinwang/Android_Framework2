package com.yanzhenjie.durban

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.common.utils.ScreenUtil
import com.example.framework.utils.function.color
import com.example.framework.utils.function.drawable
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.logWTF
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.yanzhenjie.durban.callback.BitmapCropCallback
import com.yanzhenjie.durban.view.CropView
import com.yanzhenjie.durban.view.GestureCropImageView
import com.yanzhenjie.durban.view.OverlayView
import com.yanzhenjie.durban.view.TransformImageView

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
class DurbanActivity : BaseActivity() {
    private var mCropView: CropView? = null
    private var mCropImageView: GestureCropImageView? = null
    private val mStatusColor by lazy { intentInt(Durban.KEY_INPUT_STATUS_COLOR, R.color.durban_ColorPrimaryDark) }
    private val mNavigationColor by lazy { intentInt(Durban.KEY_INPUT_NAVIGATION_COLOR, R.color.durban_ColorPrimaryBlack) }
    private val mGesture by lazy { intentInt(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL) }
    private val mCompressQuality by lazy { intentInt(Durban.KEY_INPUT_COMPRESS_QUALITY, 90) }
    private val mCompressFormat by lazy {
        val compressFormat = intentInt(Durban.KEY_INPUT_COMPRESS_FORMAT, 0)
        if (compressFormat == Durban.COMPRESS_PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    }
    private val mTitle by lazy { intentString(Durban.KEY_INPUT_TITLE, string(R.string.durban_title_crop)) }
    private val mOutputDirectory by lazy { intentString(Durban.KEY_INPUT_DIRECTORY, filesDir.absolutePath) }
    private val mController by lazy { intentParcelable(Durban.KEY_INPUT_CONTROLLER) ?: Controller.newBuilder().build() }
    private val mMaxWidthHeight by lazy { intent.getIntArrayExtra(Durban.KEY_INPUT_MAX_WIDTH_HEIGHT) ?: intArrayOf(500, 500) }
    private val mAspectRatio by lazy { intent.getFloatArrayExtra(Durban.KEY_INPUT_ASPECT_RATIO) ?: floatArrayOf(0f, 0f) }
    private val mInputPathList by lazy { intent.getStringArrayListExtra(Durban.KEY_INPUT_PATH_ARRAY) }
    private val mOutputPathList by lazy { ArrayList<String>() }
    private val TAG = "Durban"

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.durban_activity_photobox)
        initFrameViews()
        initContentViews()
        initControllerViews()
        cropNextImage()
    }

    /**
     * 初始化窗体(状态栏/导航栏)
     */
    private fun initFrameViews() {
        // 获取自定义Toolbar并设置为ActionBar替代品
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // 通过getSupportActionBar()操作这个Toolbar
        val actionBar = supportActionBar
        // 显示返回键
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = ""
        // 设置Toolbar样式
        setSupportToolbar(toolbar)
        val mStatusColorRes = color(mStatusColor)
        toolbar.setBackgroundColor(mStatusColorRes)
        toolbar.setSubtitleTextColor(mStatusColorRes)
        toolbar.setTitleTextColor(mStatusColorRes)
        // 设置图标样式
        val statusBarBattery = ScreenUtil.shouldUseWhiteSystemBarsForRes(mStatusColor)
        val navigationBarBattery = ScreenUtil.shouldUseWhiteSystemBarsForRes(mNavigationColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mNavigationColor)
        // 设置标题
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = mTitle
        if (statusBarBattery) {
            tvTitle.setTextColor(color(R.color.textWhite))
        } else {
            tvTitle.setTextColor(color(R.color.textBlack))
        }
        // 设置返回按钮
        val navigationIcon = drawable(R.drawable.durban_ic_back_white)
        if (!statusBarBattery) {
            navigationIcon?.setTint(color(R.color.bgBlack))
        }
        toolbar.setNavigationIcon(navigationIcon)
    }

    private fun initContentViews() {
        mCropView = findViewById(R.id.crop_view)
        mCropImageView = mCropView?.cropImageView
        mCropImageView?.outputDirectory = mOutputDirectory
        mCropImageView?.setTransformImageListener(object : TransformImageView.TransformImageListener {
            override fun onLoadComplete() {
                mCropView?.animate()
                    ?.alpha(1f)
                    ?.setDuration(300)
                    ?.interpolator = AccelerateInterpolator()
            }

            override fun onLoadFailure() {
                cropNextImage()
            }

            override fun onRotate(currentAngle: Float) {
            }

            override fun onScale(currentScale: Float) {
            }
        })
        mCropImageView?.isScaleEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE
        mCropImageView?.isRotateEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE
        // Durban image view options
        mCropImageView?.maxBitmapSize = GestureCropImageView.DEFAULT_MAX_BITMAP_SIZE
        mCropImageView?.setMaxScaleMultiplier(GestureCropImageView.DEFAULT_MAX_SCALE_MULTIPLIER)
        mCropImageView?.setImageToWrapCropBoundsAnimDuration(GestureCropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong())
        // Overlay view options
        val overlayView = mCropView?.overlayView
        overlayView?.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_DISABLE)
        overlayView?.setDimmedColor(color(R.color.durban_CropDimmed))
        overlayView?.setCircleDimmedLayer(false)
        overlayView?.setShowCropFrame(true)
        overlayView?.setCropFrameColor(color(R.color.durban_CropFrameLine))
        overlayView?.setCropFrameStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1))
        overlayView?.setShowCropGrid(true)
        overlayView?.setCropGridRowCount(2)
        overlayView?.setCropGridColumnCount(2)
        overlayView?.setCropGridColor(color(R.color.durban_CropGridLine))
        overlayView?.setCropGridStrokeWidth(getResources().getDimensionPixelSize(R.dimen.durban_dp_1))
        // Aspect ratio options
        if (mAspectRatio[0] > 0 && mAspectRatio[1] > 0) mCropImageView?.setTargetAspectRatio(mAspectRatio[0] / mAspectRatio[1])
        else mCropImageView?.setTargetAspectRatio(GestureCropImageView.SOURCE_IMAGE_ASPECT_RATIO)
        // Result exception max size options
        if (mMaxWidthHeight[0] > 0 && mMaxWidthHeight[1] > 0) {
            mCropImageView?.setMaxResultImageSizeX(mMaxWidthHeight[0])
            mCropImageView?.setMaxResultImageSizeY(mMaxWidthHeight[1])
        }
    }

    private fun initControllerViews() {
        val controllerRoot = findViewById<View>(R.id.iv_controller_root)
        val rotationTitle = findViewById<View>(R.id.tv_controller_title_rotation)
        val rotationLeft = findViewById<View>(R.id.layout_controller_rotation_left)
        val rotationRight = findViewById<View>(R.id.layout_controller_rotation_right)
        val scaleTitle = findViewById<View>(R.id.tv_controller_title_scale)
        val scaleBig = findViewById<View>(R.id.layout_controller_scale_big)
        val scaleSmall = findViewById<View>(R.id.layout_controller_scale_small)
        controllerRoot.visibility = if (mController.isEnable) View.VISIBLE else View.GONE
        rotationTitle.visibility = if (mController.isRotationTitle) View.VISIBLE else View.INVISIBLE
        rotationLeft.visibility = if (mController.isRotation) View.VISIBLE else View.GONE
        rotationRight.visibility = if (mController.isRotation) View.VISIBLE else View.GONE
        scaleTitle.visibility = if (mController.isScaleTitle) View.VISIBLE else View.INVISIBLE
        scaleBig.visibility = if (mController.isScale) View.VISIBLE else View.GONE
        scaleSmall.visibility = if (mController.isScale) View.VISIBLE else View.GONE
        if (!mController.isRotationTitle && !mController.isScaleTitle) findViewById<View>(R.id.layout_controller_title_root).visibility = View.GONE
        if (!mController.isRotation) rotationTitle.visibility = View.GONE
        if (!mController.isScale) scaleTitle.visibility = View.GONE
        rotationLeft.setOnClickListener(mControllerClick)
        rotationRight.setOnClickListener(mControllerClick)
        scaleBig.setOnClickListener(mControllerClick)
        scaleSmall.setOnClickListener(mControllerClick)
    }

    private val mControllerClick = View.OnClickListener { v ->
        when (v?.id) {
            R.id.layout_controller_rotation_left -> {
                mCropImageView?.postRotate(-90f)
                mCropImageView?.setImageToWrapCropBounds()
            }
            R.id.layout_controller_rotation_right -> {
                mCropImageView?.postRotate(90f)
                mCropImageView?.setImageToWrapCropBounds()
            }
            R.id.layout_controller_scale_big -> {
                mCropImageView?.zoomOutImage(mCropImageView?.currentScale.orZero + ((mCropImageView?.maxScale.orZero - mCropImageView?.minScale.orZero) / 10))
                mCropImageView?.setImageToWrapCropBounds()
            }
            R.id.layout_controller_scale_small -> {
                mCropImageView?.zoomInImage(mCropImageView?.currentScale.orZero - ((mCropImageView?.maxScale.orZero - mCropImageView?.minScale.orZero) / 10))
                mCropImageView?.setImageToWrapCropBounds()
            }
        }
    }

    /**
     * Start cropping and request permission if there is no permission.
     */
    private fun cropNextImage() {
        resetRotation()
        cropNextImageWithPermission()
    }

    /**
     * Restore the rotation angle.
     */
    private fun resetRotation() {
        mCropImageView?.postRotate(-mCropImageView?.currentAngle.orZero)
        mCropImageView?.setImageToWrapCropBounds()
    }

    private fun cropNextImageWithPermission() {
        if (mInputPathList != null) {
            if (mInputPathList.safeSize > 0) {
                val currentPath = mInputPathList?.removeAt(0).orEmpty()
                try {
                    mCropImageView?.setImagePath(currentPath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    cropNextImage()
                }
            } else if (mOutputPathList.safeSize > 0) setResultSuccessful()
            else setResultFailure()
        } else {
            "The file list is empty.".logWTF(TAG)
            setResultFailure()
        }
    }

    private fun cropAndSaveImage() {
        mCropImageView?.cropAndSaveImage(mCompressFormat, mCompressQuality, cropCallback)
    }

    private val cropCallback = object : BitmapCropCallback {
        override fun onBitmapCropped(imagePath: String, imageWidth: Int, imageHeight: Int) {
            mOutputPathList.add(imagePath)
            cropNextImage()
        }

        override fun onCropFailure(t: Throwable) {
            cropNextImage()
        }
    }

    private fun setResultSuccessful() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setResultFailure() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        mCropImageView?.cancelAllAnimations()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.durban_menu_activity, menu)
        // 获取右侧菜单按钮的 MenuItem
        val okItem = menu?.findItem(R.id.menu_action_ok)
        // 去除长按的文字提示
        okItem?.title = ""
        // 根据导航栏颜色定义对应的图片
        if (!ScreenUtil.shouldUseWhiteSystemBarsForRes(mStatusColor)) {
            val doneIcon = drawable(R.drawable.durban_ic_done_white)
            doneIcon?.setTint(color(R.color.bgBlack))
            // 如果菜单按钮是自定义 View（通过 actionLayout 指定）
            val okView = okItem?.actionView
            okView?.background = doneIcon
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_action_ok) {
            cropAndSaveImage()
        } else if (item.itemId == android.R.id.home) {
            setResultFailure()
        }
        return true
    }

}