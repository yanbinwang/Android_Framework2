package com.yanzhenjie.durban.app

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.common.config.Constants.NO_DATA
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.framework.utils.function.color
import com.example.framework.utils.function.drawable
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.intentStringArrayList
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.yanzhenjie.durban.Durban
import com.yanzhenjie.durban.app.data.BitmapCropCallback
import com.yanzhenjie.durban.model.Controller
import com.yanzhenjie.durban.widget.TransformImageView
import com.yanzhenjie.durban.widget.CropView
import com.yanzhenjie.durban.widget.GestureCropImageView
import com.yanzhenjie.durban.widget.OverlayView

/**
 * 图片裁剪页
 * 功能：接收配置 → 显示裁剪 → 旋转/缩放 → 保存 → 返回结果
 */
internal class DurbanActivity : BaseActivity(), View.OnClickListener {
    // 状态栏/导航栏颜色
    private val mStatusBarColor by lazy { intentInt(Durban.KEY_INPUT_STATUS_COLOR, R.color.galleryStatusBar) }
    private val mNavigationBarColor by lazy { intentInt(Durban.KEY_INPUT_NAVIGATION_COLOR, R.color.galleryNavigationBar) }
    // 手势类型：旋转/缩放
    private val mGesture by lazy { intentInt(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL) }
    // 压缩质量
    private val mCompressQuality by lazy { intentInt(Durban.KEY_INPUT_COMPRESS_QUALITY, 90) }
    // 标题
    private val mTitle by lazy { intentString(Durban.KEY_INPUT_TITLE, NO_DATA) }
    // 输出目录 (默认私有目录)
    private val mOutputDirectory by lazy { intentString(Durban.KEY_INPUT_DIRECTORY, filesDir.absolutePath) }
    // 输出最大宽高
    private val mMaxWidthHeight by lazy { intent.getIntArrayExtra(Durban.KEY_INPUT_MAX_WIDTH_HEIGHT) ?: intArrayOf(500, 500) }
    // 裁剪比例
    private val mAspectRatio by lazy { intent.getFloatArrayExtra(Durban.KEY_INPUT_ASPECT_RATIO) ?: floatArrayOf(0f, 0f) }
    // 输入/输出图片列表
    private val mInputPathList by lazy { intentStringArrayList(Durban.KEY_INPUT_PATH_ARRAY) }
    private val mOutputPathList = ArrayList<String>()
    // 压缩格式
    private var mCompressFormat = Bitmap.CompressFormat.JPEG
    // 底部按钮控制器
    private val mController by lazy { intentParcelable<Controller>(Durban.KEY_INPUT_CONTROLLER) ?: Controller.newBuilder().build() }
    // 标题头
    private val mToolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    // 裁剪视图
    private val mCropView by lazy { findViewById<CropView>(R.id.crop_view) }
    private val mCropImageView by lazy { mCropView.cropImageView }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 校验参数
        if (!hasExtras()) return finish()
        // 压缩格式修正
        val compressFormat = intentInt(Durban.KEY_INPUT_COMPRESS_FORMAT, Durban.COMPRESS_JPEG)
        mCompressFormat = if (compressFormat == Durban.COMPRESS_PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        // 设置布局
        setContentView(R.layout.durban_activity_photobox)
        // 初始化状态栏、标题栏
        initFrameViews()
        // 初始化裁剪页面视图
        initContentViews()
        // 初始化底部操作按钮视图
        initControllerViews()
        // 开始裁剪第一张图
        cropNextImage()
    }

    private fun initFrameViews() {
        // 获取自定义Toolbar并设置为ActionBar替代品
        setSupportActionBar(mToolbar)
        // 通过getSupportActionBar()操作这个Toolbar / 显示返回键
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        // 设置Toolbar样式
        setSupportToolbar(mToolbar)
        mToolbar.setBackgroundColor(ContextCompat.getColor(this, mStatusBarColor))
        // 设置图标样式
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mStatusBarColor)
        val navigationBarBattery = shouldUseWhiteSystemBarsForRes(mNavigationBarColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mNavigationBarColor)
        // 设置标题
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        if (mTitle.isNotEmpty()) {
            tvTitle.text = mTitle
            tvTitle.setTextColor(color(if (statusBarBattery) R.color.galleryFontLight else R.color.galleryFontDark))
        }
        // 设置返回按钮
        val navigationIcon = drawable(R.mipmap.gallery_ic_back)
        if (!statusBarBattery && null != navigationIcon) {
            navigationIcon.setTint(color(R.color.galleryIconDark))
        }
        mToolbar.setNavigationIcon(navigationIcon)
    }

    private fun initContentViews() {
        // 设置输出目录
        mCropImageView.outputDirectory = mOutputDirectory
        // 是否允许缩放
        mCropImageView.isScaleEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE
        // 是否允许旋转
        mCropImageView.isRotateEnabled = mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE
        mCropImageView.maxBitmapSize = GestureCropImageView.DEFAULT_MAX_BITMAP_SIZE
        mCropImageView.setMaxScaleMultiplier(GestureCropImageView.DEFAULT_MAX_SCALE_MULTIPLIER)
        mCropImageView.setImageToWrapCropBoundsAnimDuration(GestureCropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong())
        // 图片加载监听
        mCropImageView.setTransformImageListener(object : TransformImageView.TransformImageListener {
            override fun onLoadComplete() {
                mCropView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(AccelerateInterpolator())
                    .start()
            }

            override fun onLoadFailure() {
                cropNextImage()
            }

            override fun onRotate(currentAngle: Float) {
            }

            override fun onScale(currentScale: Float) {
            }
        })
        // 裁剪视图样式
        val overlayView = mCropView.overlayView
        overlayView.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_DISABLE)
        overlayView.setDimmedColor(ContextCompat.getColor(this, R.color.durbanCropDimmed))
        overlayView.setCircleDimmedLayer(false)
        overlayView.setShowCropFrame(true)
        overlayView.setCropFrameColor(ContextCompat.getColor(this, R.color.durbanCropFrameLine))
        overlayView.setCropFrameStrokeWidth(getResources().getDimensionPixelSize(R.dimen.gallery_dp_1))
        overlayView.setShowCropGrid(true)
        overlayView.setCropGridRowCount(2)
        overlayView.setCropGridColumnCount(2)
        overlayView.setCropGridColor(ContextCompat.getColor(this, R.color.durbanCropGridLine))
        overlayView.setCropGridStrokeWidth(getResources().getDimensionPixelSize(R.dimen.gallery_dp_1))
        // 设置裁剪比例
        if (mAspectRatio[0] > 0 && mAspectRatio[1] > 0) {
            mCropImageView.setTargetAspectRatio(mAspectRatio[0] / mAspectRatio[1])
        } else {
            mCropImageView.setTargetAspectRatio(GestureCropImageView.SOURCE_IMAGE_ASPECT_RATIO)
        }
        // 设置输出最大宽高
        if (mMaxWidthHeight[0] > 0 && mMaxWidthHeight[1] > 0) {
            mCropImageView.setMaxResultImageSizeX(mMaxWidthHeight[0])
            mCropImageView.setMaxResultImageSizeY(mMaxWidthHeight[1])
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
        // 根据配置显示/隐藏按钮
        controllerRoot.visibility = if (mController.enable) View.VISIBLE else View.GONE
        rotationTitle.visibility = if (mController.rotationTitle) View.VISIBLE else View.INVISIBLE
        rotationLeft.visibility = if (mController.rotation) View.VISIBLE else View.GONE
        rotationRight.visibility = if (mController.rotation) View.VISIBLE else View.GONE
        scaleTitle.visibility = if (mController.scaleTitle) View.VISIBLE else View.INVISIBLE
        scaleBig.visibility = if (mController.scale) View.VISIBLE else View.GONE
        scaleSmall.visibility = if (mController.scale) View.VISIBLE else View.GONE
        // 隐藏所有标题时，隐藏标题栏
        if (!mController.rotationTitle && !mController.scaleTitle) {
            findViewById<View>(R.id.layout_controller_title_root).visibility = View.GONE
        }
        if (!mController.rotation) {
            rotationTitle.visibility = View.GONE
        }
        if (!mController.scale) {
            scaleTitle.visibility = View.GONE
        }
        // 点击事件
        clicks(rotationLeft, rotationRight, scaleBig, scaleSmall)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.durban_menu_activity, menu)
        // 获取右侧菜单按钮的 MenuItem
        val okItem = menu?.findItem(R.id.menu_action_ok)
        // 去除长按的文字提示
        okItem?.title = ""
        // 根据导航栏颜色定义对应的图片
        if (!shouldUseWhiteSystemBarsForRes(mStatusBarColor)) {
            val doneIcon = ContextCompat.getDrawable(this, R.mipmap.gallery_ic_done)
            if (null != doneIcon) {
                doneIcon.setTint(ContextCompat.getColor(this, R.color.galleryIconDark))
                // 如果菜单按钮是自定义 View（通过 actionLayout 指定）
                val okView = okItem?.actionView
                okView?.background = doneIcon
            }
        }
        // 设置额外添加的按钮外层间距
        setSupportMenuView(mToolbar, mStatusBarColor)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 保存
            R.id.menu_action_ok -> cropAndSaveImage()
            // 取消
            android.R.id.home -> setResultFailure()
        }
        return true
    }

    /**
     * 执行裁剪并保存
     */
    private fun cropAndSaveImage() {
        mCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {
            override fun onBitmapCropped(imagePath: String, imageWidth: Int, imageHeight: Int) {
                mOutputPathList.add(imagePath)
                cropNextImage()
            }

            override fun onCropFailure(t: Throwable) {
                cropNextImage()
            }
        })
    }

    /**
     * 开始裁剪下一张
     */
    private fun cropNextImage() {
        // 重置图片旋转角度
        mCropImageView.postRotate(-mCropImageView.currentAngle)
        mCropImageView.setImageToWrapCropBounds()
        // 加载图片并裁剪
        if (!mInputPathList.isEmpty()) {
            val currentPath = mInputPathList.removeAt(0)
            try {
                mCropImageView.setImagePath(currentPath)
            } catch (_: Exception) {
                // 加载失败直接下一张
                cropNextImage()
            }
        } else {
            // 全部裁剪完成
            if (!mOutputPathList.isEmpty()) {
                setResultSuccessful()
            } else {
                setResultFailure()
            }
        }
    }

    /**
     * 返回成功/失败结果
     */
    private fun setResultSuccessful() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setResultFailure() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 左转 90
            R.id.layout_controller_rotation_left -> {
                mCropImageView.postRotate(-90f)
                mCropImageView.setImageToWrapCropBounds()
            }
            // 右转 90
            R.id.layout_controller_rotation_right -> {
                mCropImageView.postRotate(90f)
                mCropImageView.setImageToWrapCropBounds()
            }
            // 放大
            R.id.layout_controller_scale_big -> {
                mCropImageView.zoomOutImage(mCropImageView.getCurrentScale() + ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10))
                mCropImageView.setImageToWrapCropBounds()
            }
            // 缩小
            R.id.layout_controller_scale_small -> {
                mCropImageView.zoomInImage(mCropImageView.getCurrentScale() - ((mCropImageView.getMaxScale() - mCropImageView.getMinScale()) / 10))
                mCropImageView.setImageToWrapCropBounds()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // 页面不可见时，取消所有动画，防止内存泄漏
        mCropImageView.cancelAllAnimations()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
    }

}