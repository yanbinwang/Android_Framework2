package com.example.gallery.activity

import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.LogUtil
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.gallery.utils.Durban
import com.yanzhenjie.durban.Controller
import com.yanzhenjie.durban.R
import com.yanzhenjie.durban.callback.BitmapCropCallback
import com.yanzhenjie.durban.util.DurbanUtils
import com.yanzhenjie.durban.view.CropView
import com.yanzhenjie.durban.view.GestureCropImageView
import com.yanzhenjie.durban.view.OverlayView
import com.yanzhenjie.durban.view.TransformImageView

/**
 * 裁剪页
 */
class DurbanActivity : AppCompatActivity() {
    private var mStatusColor = 0
    private var mNavigationColor = 0
    private var mToolbarColor = 0
    private var mTitle: String? = null

    private var mGesture = 0
    private var mAspectRatio: FloatArray? = null
    private var mMaxWidthHeight: IntArray? = null

    private var mCompressFormat: CompressFormat? = null
    private var mCompressQuality = 0

    private var mOutputDirectory: String? = null
    private var mInputPathList: ArrayList<String>? = null

    private var mController: Controller? = null

    private var mCropView: CropView? = null
    private var mCropImageView: GestureCropImageView? = null

    private var mOutputPathList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Durban.getDurbanConfig(this)?.locale
        DurbanUtils.applyLanguageForContext(this, locale)
        setContentView(R.layout.durban_activity_photobox)
        initArgument()
        initFrameViews()
        initContentViews()
        initControllerViews()
        cropNextImage()
    }

    private fun initArgument() {
        mStatusColor = ContextCompat.getColor(this, R.color.durban_ColorPrimaryDark)
        mToolbarColor = ContextCompat.getColor(this, R.color.durban_ColorPrimary)
        mNavigationColor = ContextCompat.getColor(this, R.color.durban_ColorPrimaryBlack)

        mStatusColor = intent.getIntExtra(Durban.KEY_INPUT_STATUS_COLOR, mStatusColor)
        mToolbarColor = intent.getIntExtra(Durban.KEY_INPUT_TOOLBAR_COLOR, mToolbarColor)
        mNavigationColor = intent.getIntExtra(Durban.KEY_INPUT_NAVIGATION_COLOR, mNavigationColor)
        mTitle = intent.getStringExtra(Durban.KEY_INPUT_TITLE)
        if (mTitle.isNullOrEmpty()) mTitle = getString(R.string.durban_title_crop)

        mGesture = intent.getIntExtra(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL)
        mAspectRatio = intent.getFloatArrayExtra(Durban.KEY_INPUT_ASPECT_RATIO)
        if (mAspectRatio == null) mAspectRatio = floatArrayOf(0f, 0f)
        mMaxWidthHeight = intent.getIntArrayExtra(Durban.KEY_INPUT_MAX_WIDTH_HEIGHT)
        if (mMaxWidthHeight == null) mMaxWidthHeight = intArrayOf(500, 500)

        val compressFormat = intent.getIntExtra(Durban.KEY_INPUT_COMPRESS_FORMAT, 0)
        mCompressFormat = if (compressFormat == Durban.COMPRESS_PNG) CompressFormat.PNG else CompressFormat.JPEG
        mCompressQuality = intent.getIntExtra(Durban.KEY_INPUT_COMPRESS_QUALITY, 90)

        mOutputDirectory = intent.getStringExtra(Durban.KEY_INPUT_DIRECTORY)
        if (mOutputDirectory.isNullOrEmpty()) mOutputDirectory = filesDir.absolutePath
        mInputPathList = intent.getStringArrayListExtra(Durban.KEY_INPUT_PATH_ARRAY)

        mController = intent.getParcelableExtra(Durban.KEY_INPUT_CONTROLLER)
        if (mController == null) mController = Controller.newBuilder().build()

        mOutputPathList = ArrayList()
    }

    private fun initFrameViews() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = mStatusColor
        window.navigationBarColor = mNavigationColor

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(mToolbarColor)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        checkNotNull(actionBar)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.title = mTitle

        //标题栏兼容
        toolbar.doOnceAfterLayout {
            it.size(height = it.measuredHeight + getStatusBarHeight())
            it.padding(top = getStatusBarHeight())
        }
    }

    private fun initContentViews() {
        mCropView = findViewById(R.id.crop_view)
        mCropImageView = mCropView?.cropImageView
        mCropImageView?.outputDirectory = mOutputDirectory
        mCropImageView?.setTransformImageListener(mImageListener)
        mCropImageView?.isScaleEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE
        mCropImageView?.isRotateEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE

        // Durban image view options
        mCropImageView?.maxBitmapSize = GestureCropImageView.DEFAULT_MAX_BITMAP_SIZE
        mCropImageView?.setMaxScaleMultiplier(GestureCropImageView.DEFAULT_MAX_SCALE_MULTIPLIER)
        mCropImageView?.setImageToWrapCropBoundsAnimDuration(GestureCropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong())

        // Overlay view options
        val overlayView = mCropView?.overlayView
        overlayView?.freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_DISABLE
        overlayView?.setDimmedColor(ContextCompat.getColor(this, R.color.durban_CropDimmed))
        overlayView?.setCircleDimmedLayer(false)
        overlayView?.setShowCropFrame(true)
        overlayView?.setCropFrameColor(ContextCompat.getColor(this, R.color.durban_CropFrameLine))
        overlayView?.setCropFrameStrokeWidth(resources.getDimensionPixelSize(R.dimen.durban_dp_1))
        overlayView?.setShowCropGrid(true)
        overlayView?.setCropGridRowCount(2)
        overlayView?.setCropGridColumnCount(2)
        overlayView?.setCropGridColor(ContextCompat.getColor(this, R.color.durban_CropGridLine))
        overlayView?.setCropGridStrokeWidth(resources.getDimensionPixelSize(R.dimen.durban_dp_1))

        // Aspect ratio options
        if (mAspectRatio?.get(0).orZero > 0 && mAspectRatio?.get(1).orZero > 0) mCropImageView?.targetAspectRatio = mAspectRatio?.get(0).orZero / mAspectRatio?.get(1).orZero
        else mCropImageView?.targetAspectRatio = GestureCropImageView.SOURCE_IMAGE_ASPECT_RATIO

        // Result exception max size options
        if (mMaxWidthHeight?.get(0).orZero > 0 && mMaxWidthHeight?.get(1).orZero > 0) {
            mCropImageView?.setMaxResultImageSizeX(mMaxWidthHeight?.get(0).orZero)
            mCropImageView?.setMaxResultImageSizeY(mMaxWidthHeight?.get(1).orZero)
        }
    }

    private val mImageListener = object : TransformImageView.TransformImageListener {
        override fun onLoadComplete() {
            mCropView?.animate()
                ?.alpha(1f)
                ?.setDuration(300)
                ?.setInterpolator(AccelerateInterpolator())
                ?.start()
        }

        override fun onLoadFailure() {
            cropNextImage()
        }

        override fun onRotate(currentAngle: Float) {
        }

        override fun onScale(currentScale: Float) {
        }
    }

    /**
     * Start cropping and request permission if there is no permission.
     */
    private fun cropNextImage() {
        resetRotation()
        processNextImageForCrop()
    }

    private fun resetRotation() {
        mCropImageView?.postRotate(-mCropImageView?.currentAngle.orZero)
        mCropImageView?.setImageToWrapCropBounds()
    }

    private fun processNextImageForCrop() {
        if (mInputPathList != null) {
            if (mInputPathList?.size.orZero > 0) {
                val currentPath = mInputPathList?.removeAt(0)
                try {
                    mCropImageView?.setImagePath(currentPath.orEmpty())
                } catch (e: Exception) {
                    cropNextImage()
                }
            } else {
                if (mOutputPathList?.size.orZero > 0) {
                    setResultSuccessful()
                } else {
                    setResultFailure()
                }
            }
        } else {
            LogUtil.e("Durban", "The file list is empty.")
            setResultFailure()
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

    private fun initControllerViews() {
        val controllerRoot = findViewById<LinearLayout>(R.id.iv_controller_root)

        val rotationTitle = findViewById<TextView>(R.id.tv_controller_title_rotation)
        val rotationLeft = findViewById<FrameLayout>(R.id.layout_controller_rotation_left)
        val rotationRight = findViewById<FrameLayout>(R.id.layout_controller_rotation_right)
        val scaleTitle = findViewById<TextView>(R.id.tv_controller_title_scale)
        val scaleBig = findViewById<FrameLayout>(R.id.layout_controller_scale_big)
        val scaleSmall = findViewById<FrameLayout>(R.id.layout_controller_scale_small)

        controllerRoot.visibility = if (mController?.isEnable.orFalse) View.VISIBLE else View.GONE

        rotationTitle.visibility = if (mController?.isRotationTitle.orFalse) View.VISIBLE else View.INVISIBLE
        rotationLeft.visibility = if (mController?.isRotation.orFalse) View.VISIBLE else View.GONE
        rotationRight.visibility = if (mController?.isRotation.orFalse) View.VISIBLE else View.GONE
        scaleTitle.visibility = if (mController?.isScaleTitle.orFalse) View.VISIBLE else View.INVISIBLE
        scaleBig.visibility = if (mController?.isScale.orFalse) View.VISIBLE else View.GONE
        scaleSmall.visibility = if (mController?.isScale.orFalse) View.VISIBLE else View.GONE

        if (!mController?.isRotationTitle.orFalse && !mController?.isScaleTitle.orFalse) findViewById<LinearLayout>(R.id.layout_controller_title_root).visibility = View.GONE
        if (!mController?.isRotation.orFalse) rotationTitle.visibility = View.GONE
        if (!mController?.isScale.orFalse) scaleTitle.visibility = View.GONE

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

    override fun onStop() {
        super.onStop()
        mCropImageView?.cancelAllAnimations()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.durban_menu_activity, menu)
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

    private fun cropAndSaveImage() {
        mCropImageView?.cropAndSaveImage(mCompressFormat ?: CompressFormat.JPEG, mCompressQuality, cropCallback)
    }

    private val cropCallback = object : BitmapCropCallback {
        override fun onBitmapCropped(imagePath: String, imageWidth: Int, imageHeight: Int) {
            mOutputPathList?.add(imagePath)
            cropNextImage()
        }

        override fun onCropFailure(t: Throwable) {
            cropNextImage()
        }
    }

}