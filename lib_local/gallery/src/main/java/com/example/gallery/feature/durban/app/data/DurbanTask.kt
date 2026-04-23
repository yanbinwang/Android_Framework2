package com.example.gallery.feature.durban.app.data

import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.gone
import com.example.gallery.R
import com.example.gallery.feature.durban.model.ExifInfo
import com.example.gallery.feature.durban.widget.TransformImageView
import com.example.gallery.widget.LoadingDialog
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * 裁剪库整体执行
 */
class DurbanTask(private val activity: FragmentActivity) {
    private var cropJob: Job? = null
    private var loadJob: Job? = null

    // 加载对话框
    private val dialog by lazy { LoadingDialog(activity) }

    init {
        dialog.setupViews(activity.color(R.color.galleryIconDark), R.string.durban_loading_message)
        activity.doOnDestroy {
            cropJob?.cancel()
            loadJob?.cancel()
        }
    }

    /**
     * 裁剪图片 → 保存到文件 → 返回路径
     */
    fun cropExecute(mCrop: DurbanCrop, listener: BitmapCropCallback) {
        val dialogShowTime = System.currentTimeMillis()
        cropJob?.cancel()
        cropJob = activity.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mCrop.crop() })
            }.withHandling(err = {
                cropComplete(dialogShowTime) {
                    dismissDialog()
                    listener.onFailure(it)
                }
            }).onStart {
                showDialog()
            }.collect { (imagePath, imageWidth, imageHeight) ->
                cropComplete(dialogShowTime) {
                    dismissDialog()
                    listener.onSuccess(imagePath, imageWidth, imageHeight)
                }
            }
        }
    }

    /**
     * 完成回调
     */
    private fun cropComplete(dialogShowTime: Long, listener: () -> Unit) {
        val duration = System.currentTimeMillis() - dialogShowTime
        if (duration < 1000) {
            // 不足1000ms，延迟到1000ms再消失
            schedule(activity, {
                listener.invoke()
            }, 1000 - duration)
        } else {
            listener.invoke()
        }
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
     * 从文件加载图片 → 纠正旋转 → 返回 Bitmap
     */
    fun loadExecute(imageView: WeakReference<TransformImageView>, mLoad: DurbanLoad, imagePath: String, listener: BitmapLoadCallback) {
        loadJob?.cancel()
        loadJob = activity.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mLoad.load(imagePath) })
            }.withHandling(err = {
                listener.onFailure(it)
            }, end = {
                imageView.get().appear()
            }).onStart {
                imageView.get().gone()
            }.collect { (bitmap, exifInfo) ->
                listener.onSuccess(bitmap, exifInfo)
            }
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
        fun onFailure(t: Throwable)
    }

}