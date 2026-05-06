package com.example.gallery.feature.durban.app.photobox.data

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.gone
import com.example.gallery.R
import com.example.gallery.feature.durban.bean.ExifInfo
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
    val crop by lazy { MutableLiveData<String?>() }
    val load by lazy { MutableLiveData<Pair<Bitmap, ExifInfo>>() }

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
    fun cropExecute(mCrop: DurbanCrop) {
        val dialogShowTime = System.currentTimeMillis()
        cropJob?.cancel()
        cropJob = activity.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mCrop.crop() })
            }.withHandling(err = {
                cropComplete(dialogShowTime) {
                    dismissDialog()
                    crop.postValue(null)
                }
            }).onStart {
                showDialog()
            }.collect { (imagePath, _, _) ->
                cropComplete(dialogShowTime) {
                    dismissDialog()
                    crop.postValue(imagePath)
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
    fun loadExecute(imageView: WeakReference<TransformImageView>, mLoad: DurbanLoad, imagePath: String) {
        loadJob?.cancel()
        loadJob = activity.lifecycleScope.launch(Main.immediate) {
            flow {
                emit(requestAffair { mLoad.load(imagePath) })
            }.withHandling(err = {
                imageView.get()?.getTransformImageListener()?.onLoadFailure()
            }, end = {
                imageView.get().appear()
            }).onStart {
                imageView.get().gone()
            }.collect { (bitmap, exifInfo) ->
                load.postValue(bitmap to exifInfo)
            }
        }
    }

}