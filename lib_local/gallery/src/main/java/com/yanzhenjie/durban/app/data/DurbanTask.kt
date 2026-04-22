package com.yanzhenjie.durban.app.data

import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.framework.utils.function.doOnDestroy
import com.example.gallery.R
import com.example.gallery.widget.LoadingDialog
import com.yanzhenjie.durban.model.ExifInfo
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 裁剪库整体执行
 */
class DurbanTask(private val activity: FragmentActivity) {
    private var cropJob: Job? = null
    private var loadJob: Job? = null

    // 加载对话框
    private val dialog by lazy { LoadingDialog(activity) }

    init {
        dialog.setupViews(
            ContextCompat.getColor(activity, R.color.galleryIconDark),
            R.string.durban_loading_message
        )
        activity.doOnDestroy {
            cropJob?.cancel()
            loadJob?.cancel()
        }
    }

    /**
     * 裁剪图片 → 保存到文件 → 返回路径
     */
    fun cropExecute(mCrop: DurbanCrop, listener: BitmapCropCallback) {
        cropJob?.cancel()
        cropJob = activity.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mCrop.crop() })
            }.withHandling(err = {
                listener.onFailure(it)
            }, end = {
                dismissDialog()
            }).onStart {
                showDialog()
            }.collect { (imagePath, imageWidth, imageHeight) ->
                listener.onSuccess(imagePath, imageWidth, imageHeight)
            }
        }
    }

    /**
     * 从文件加载图片 → 纠正旋转 → 返回 Bitmap
     */
    fun loadExecute() {

    }

    /**
     * 加载/隐藏对话框
     */
    private fun showDialog() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun dismissDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * 图片裁剪回调接口
     * 作用：监听图片裁剪的 成功 / 失败 结果
     */
    interface BitmapCropCallback {
        /**
         * 裁剪成功
         * @param imagePath  裁剪后的图片保存路径
         * @param imageWidth 裁剪后的图片宽度
         * @param imageHeight 裁剪后的图片高度
         */
        fun onSuccess(imagePath: String, imageWidth: Int, imageHeight: Int)

        /**
         * 裁剪失败（比如权限不足、内存不足、图片损坏）
         */
        fun onFailure(t: Throwable)
    }

    /**
     * 图片加载回调接口
     * 作用：监听图片加载的 成功 / 失败 结果
     */
    interface BitmapLoadCallback {
        /**
         * 加载成功
         * @param bitmap 加载完成的图片对象
         * @param exifInfo 图片的EXIF信息（旋转角度、宽高、方向等）
         */
        fun onSuccess(bitmap: Bitmap, exifInfo: ExifInfo)

        /**
         * 加载失败（文件不存在、图片损坏、内存不足等）
         */
        fun onFailure()
    }

}