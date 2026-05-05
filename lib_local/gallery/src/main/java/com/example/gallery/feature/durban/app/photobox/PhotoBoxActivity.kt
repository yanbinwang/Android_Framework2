package com.example.gallery.feature.durban.app.photobox

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.example.common.config.Constants
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.framework.utils.function.hasExtras
import com.example.framework.utils.function.intentInt
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.intentStringArrayList
import com.example.gallery.R
import com.example.gallery.base.BaseActivity
import com.example.gallery.feature.durban.Durban
import com.example.gallery.feature.durban.app.Contract
import com.example.gallery.feature.durban.app.photobox.data.DurbanTask
import com.example.gallery.feature.durban.app.photobox.view.PhotoBoxView
import com.example.gallery.feature.durban.bean.Controller

/**
 * 图片裁剪页
 * 功能：接收配置 → 显示裁剪 → 旋转/缩放 → 保存 → 返回结果
 */
internal class PhotoBoxActivity : BaseActivity(), Contract.PhotoBoxPresenter {
    // 状态栏/导航栏颜色
    private val mStatusBarColor by lazy { intentInt(Durban.KEY_INPUT_STATUS_COLOR, R.color.galleryStatusBar) }
    private val mNavigationBarColor by lazy { intentInt(Durban.KEY_INPUT_NAVIGATION_COLOR, R.color.galleryNavigationBar) }
    // 手势类型：旋转/缩放
    private val mGesture by lazy { intentInt(Durban.KEY_INPUT_GESTURE, Durban.GESTURE_ALL) }
    // 压缩质量
    private val mCompressQuality by lazy { intentInt(Durban.KEY_INPUT_COMPRESS_QUALITY, 90) }
    // 标题
    private val mTitle by lazy { intentString(Durban.KEY_INPUT_TITLE, Constants.NO_DATA) }
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
    // 裁剪全局任务
    private val mTask by lazy { DurbanTask(this) }
    // MVP & UI
    private val mView by lazy { PhotoBoxView(this, this) }
    private val mCropImageView get() = mView.getGestureCropImageView()

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
        // 初始化状态栏
        val statusBarBattery = shouldUseWhiteSystemBarsForRes(mStatusBarColor)
        val navigationBarBattery = shouldUseWhiteSystemBarsForRes(mNavigationBarColor)
        initImmersionBar(!statusBarBattery, !navigationBarBattery, mNavigationBarColor)
        // MVP设置
        mView.setupViews(mStatusBarColor, mTitle, mOutputDirectory, mGesture, mAspectRatio, mMaxWidthHeight, mController)
        // 建立页面数据订阅
        mTask.load.observe {
            val (bitmap, exifInfo) = this
            mCropImageView.setImageLoad(bitmap, exifInfo)
        }
        mTask.crop.observe {
            if (!isNullOrEmpty()) {
                mOutputPathList.add(this)
            }
            cropNextImage()
        }
        // 开始裁剪第一张图
        cropNextImage()
    }

    /**
     * 执行裁剪并保存
     */
    override fun cropAndSaveImage() {
        val cropData = mCropImageView.buildImageCropData(mCompressFormat, mCompressQuality)
        if (null != cropData) {
            mTask.cropExecute(cropData)
        } else {
            cropNextImage()
        }
    }

    /**
     * 开始裁剪下一张
     */
    override fun cropNextImage() {
        // 重置图片旋转角度
        mCropImageView.postRotate(-mCropImageView.getCurrentAngle())
        mCropImageView.setImageToWrapCropBounds()
        // 加载图片并裁剪
        if (!mInputPathList.isEmpty()) {
            val currentPath = mInputPathList.removeAt(0)
            // 加载失败直接下一张 -> 监听回调
            val (viewRef, loadData) = mCropImageView.buildImageLoadData(currentPath)
            mTask.loadExecute(viewRef, loadData, currentPath)
        } else {
            // 全部裁剪完成
            if (!mOutputPathList.isEmpty()) {
                setResultSuccess()
            } else {
                setResultFailure()
            }
        }
    }

    /**
     * 返回成功/失败结果
     */
    override fun setResultSuccess() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun setResultFailure() {
        val intent = Intent()
        intent.putStringArrayListExtra(Durban.KEY_OUTPUT_IMAGE_LIST, mOutputPathList)
        intent.putStringArrayListExtra(Durban.KEY_ORIGINAL_PATH_LIST, mInputPathList)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_out)
    }

}