package com.example.gallery.feature.durban.app.photobox.view

import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.pt
import com.example.common.widget.AppToolbar
import com.example.framework.utils.function.view.clicks
import com.example.gallery.R
import com.example.gallery.feature.durban.Durban
import com.example.gallery.feature.durban.app.Contract
import com.example.gallery.feature.durban.bean.Controller
import com.example.gallery.feature.durban.widget.CropImageView
import com.example.gallery.feature.durban.widget.CropView
import com.example.gallery.feature.durban.widget.GestureCropImageView
import com.example.gallery.feature.durban.widget.OverlayView
import com.example.gallery.feature.durban.widget.TransformImageView

class PhotoBoxView(activity: AppCompatActivity, presenter: Contract.PhotoBoxPresenter) : Contract.PhotoBoxView(activity, presenter), View.OnClickListener {
    // 标题头
    private val mToolbar = activity.findViewById<AppToolbar>(R.id.toolbar)
    // 裁剪视图
    private val mCropView = activity.findViewById<CropView>(R.id.crop_view)
    private val mCropImageView by lazy { mCropView.getCropImageView() }
    // 控件盘
    private val controllerTitleRoot = activity.findViewById<View>(R.id.layout_controller_title_root)
    private val controllerRoot = activity.findViewById<View>(R.id.iv_controller_root)
    private val rotationTitle = activity.findViewById<View>(R.id.tv_controller_title_rotation)
    private val rotationLeft = activity.findViewById<View>(R.id.layout_controller_rotation_left)
    private val rotationRight = activity.findViewById<View>(R.id.layout_controller_rotation_right)
    private val scaleTitle = activity.findViewById<View>(R.id.tv_controller_title_scale)
    private val scaleBig = activity.findViewById<View>(R.id.layout_controller_scale_big)
    private val scaleSmall = activity.findViewById<View>(R.id.layout_controller_scale_small)

    override fun setupViews(mStatusBarColor: Int, mTitle: String, mOutputDirectory: String, mGesture: Int, mAspectRatio: FloatArray, mMaxWidthHeight: IntArray, mController: Controller) {
        // 标题同步状态栏颜色
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mStatusBarColor)
        val titleColor = if (statusBarBattery) R.color.galleryFontLight else R.color.galleryFontDark
        val tintColor = if (statusBarBattery) R.color.galleryIconLight else R.color.galleryIconDark
        mToolbar
            .setTitle(mTitle, titleColor, mStatusBarColor)
            .setLeftButton(tintColor = tintColor) {
                getPresenter().setResultFailure()
            }
            .setRightButton(R.mipmap.gallery_ic_done, tintColor) {
                getPresenter().cropAndSaveImage()
            }
        // 设置输出目录
        mCropImageView.setOutputDirectory(mOutputDirectory)
        // 是否允许缩放
        mCropImageView.setScaleEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_SCALE)
        // 是否允许旋转
        mCropImageView.setRotateEnabled(mGesture == Durban.GESTURE_ALL || mGesture == Durban.GESTURE_ROTATE)
        mCropImageView.setMaxBitmapSize(CropImageView.DEFAULT_MAX_BITMAP_SIZE)
        mCropImageView.setMaxScaleMultiplier(CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER)
        mCropImageView.setImageToWrapCropBoundsAnimDuration(CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong())
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
                getPresenter().cropNextImage()
            }

            override fun onRotate(currentAngle: Float) {
            }

            override fun onScale(currentScale: Float) {
            }
        })
        // 裁剪视图样式
        val overlayView = mCropView.getOverlayView()
        overlayView.setFreestyleCropMode(OverlayView.FREESTYLE_CROP_MODE_DISABLE)
        overlayView.setDimmedColor(getColor(R.color.durbanCropDimmed))
        overlayView.setCircleDimmedLayer(false)
        overlayView.setShowCropFrame(true)
        overlayView.setCropFrameColor(getColor(R.color.durbanCropFrameLine))
        overlayView.setCropFrameStrokeWidth(1.pt)
        overlayView.setShowCropGrid(true)
        overlayView.setCropGridRowCount(2)
        overlayView.setCropGridColumnCount(2)
        overlayView.setCropGridColor(getColor(R.color.durbanCropGridLine))
        overlayView.setCropGridStrokeWidth(1.pt)
        // 设置裁剪比例
        if (mAspectRatio[0] > 0 && mAspectRatio[1] > 0) {
            mCropImageView.setTargetAspectRatio(mAspectRatio[0] / mAspectRatio[1])
        } else {
            mCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO)
        }
        // 设置输出最大宽高
        if (mMaxWidthHeight[0] > 0 && mMaxWidthHeight[1] > 0) {
            mCropImageView.setMaxResultImageSizeX(mMaxWidthHeight[0])
            mCropImageView.setMaxResultImageSizeY(mMaxWidthHeight[1])
        }
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
            controllerTitleRoot.visibility = View.GONE
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

    override fun getGestureCropImageView(): GestureCropImageView {
        return mCropImageView
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // 页面不可见时，取消所有动画，防止内存泄漏
        mCropImageView.cancelAllAnimations()
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

}