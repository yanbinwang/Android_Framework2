package com.example.thirdparty.media.utils.helper

import android.app.Activity.RESULT_OK
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.FileBuilder
import com.example.common.utils.file.deleteFile
import com.example.common.utils.function.pullUpOverlay
import com.example.common.utils.function.pullUpScreen
import com.example.common.utils.function.registerResult
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeSize
import com.example.thirdparty.R
import com.example.thirdparty.media.service.ScreenService
import com.example.thirdparty.media.service.ShotObserver
import java.io.File

/**
 * @description 录屏工具类
 * @author yan
 */
class ScreenHelper(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var isDestroy = false
    private var listener: (filePath: String?, isZip: Boolean) -> Unit = { _, _ -> }
    private val list by lazy { ArrayList<String>() }
    private val builder by lazy { FileBuilder(mActivity) }
    private val loading by lazy { LoadingDialog(mActivity) }
    /**
     * 处理录屏的回调
     */
    private val result = mActivity.registerResult {
        list.clear()
        if (it.resultCode == RESULT_OK) {
            R.string.screenStart.shortToast()
            isRecording = true
            mActivity.apply {
                startService(ScreenService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
                moveTaskToBack(true)
            }
        } else {
            R.string.screenCancel.shortToast()
            isRecording = false
        }
    }

    companion object {
        var previewWidth = screenWidth
        var previewHeight = screenHeight
        var isRecording = false
    }

    init {
        mActivity.lifecycle.addObserver(this)
        //获取录屏屏幕宽高，高版本进行修正
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var destroy = false
            if (mActivity.isFinishing.orFalse) destroy = true
            if (mActivity.isDestroyed.orFalse) destroy = true
            if (mActivity.windowManager == null) destroy = true
            if (mActivity.window?.decorView == null) destroy = true
            if (mActivity.window?.decorView?.parent == null) destroy = true
            if (!destroy) {
                val decorView = mActivity.window.decorView
                decorView.post {
                    val displayCutout = decorView.rootWindowInsets.displayCutout
                    val rectLists = displayCutout?.boundingRects
                    if (null != rectLists && rectLists.size > 0) {
                        previewWidth = screenWidth - displayCutout.safeInsetLeft - displayCutout.safeInsetRight
                        previewHeight = screenHeight - displayCutout.safeInsetTop - displayCutout.safeInsetBottom
                    }
                }
            }
        }
        //只要在录屏中，截一张图就copy一张到目标目录，但是需要及时清空
        ShotObserver.instance.setOnScreenShotListener {
            it ?: return@setOnScreenShotListener
            if (isRecording) {
                if (!File(it).exists()) return@setOnScreenShotListener
                list.add(it)
            }
        }
        //录屏文件创建/停止录屏时（exists=false）都会回调
        ScreenService.setOnScreenListener { folderPath, isRecoding ->
            if(isDestroy) return@setOnScreenListener
            if (!isRecoding) {
                folderPath ?: return@setOnScreenListener
                //说明未截图
                if (list.safeSize == 0) {
                    listener.invoke(folderPath, false)
                } else {
                    //拿到保存的截屏文件夹地址下的所有文件目录，并将录屏源文件路径也添加进其中
                    list.add(folderPath)
                    //压缩包输出路径（会以录屏文件的命名方式来命名）
                    val zipPath = File(folderPath).name.replace("mp4", "zip")
                    //开始压包
                    builder.zipJob(list, zipPath, { showDialog() }, {
                        hideDialog()
                        folderPath.deleteFile()
                    })
                    listener.invoke(zipPath, true)
                }
            }
        }
    }

    private fun showDialog() {
        loading.shown(false)
    }

    private fun hideDialog() {
        loading.hidden()
    }

    /**
     * 开始录屏
     * 尝试唤起手机录屏弹窗，会在onActivityResult中回调结果
     */
    fun startScreen() = mActivity.execute {
        if (pullUpOverlay()) {
            result.pullUpScreen(this)
        } else {
            R.string.screenGranted.shortToast()
        }
    }

    /**
     * 结束录屏
     */
    fun stopScreen() = mActivity.execute {
        isRecording = false
        stopService(ScreenService::class.java)
    }

    /**
     * isZip->true是zip文件夹，可能包含录制时的截图
     */
    fun setOnScreenListener(listener: (filePath: String?, isZip: Boolean) -> Unit) {
        this.listener = listener
    }

    /**
     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> ShotObserver.instance.register()
            Lifecycle.Event.ON_DESTROY -> {
                isDestroy = true
                hideDialog()
                stopScreen()
                ShotObserver.instance.unregister()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}