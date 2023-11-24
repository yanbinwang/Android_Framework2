package com.example.thirdparty.media.utils.helper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.FileHelper
import com.example.common.utils.file.deleteFile
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.thirdparty.media.service.ScreenService
import com.example.thirdparty.media.service.ShotObserver
import java.io.File

/**
 * @description 录屏工具类
 * @author yan
 */
class ScreenHelper(private val activity: FragmentActivity) : LifecycleEventObserver {
    private val loadingDialog by lazy { LoadingDialog(activity) }
    private val fileHelper by lazy { FileHelper(activity) }
    private val shotList by lazy { ArrayList<String>() }
    private var onShutter: (filePath: String?, isZip: Boolean) -> Unit = { _, _ -> }

    /**
     * 处理录屏的回调
     */
    private val activityResultValue = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        shotList.clear()
        if (it.resultCode == RESULT_OK) {
            "开始录屏".shortToast()
            isRecording = true
            activity.apply {
                startService(ScreenService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
                moveTaskToBack(true)
            }
        } else {
            "取消录屏".shortToast()
            isRecording = false
        }
    }

    companion object {
        var previewWidth = screenWidth
        var previewHeight = screenHeight
        var isRecording = false
    }

    init {
        activity.lifecycle.addObserver(this)
        //获取录屏屏幕宽高，高版本进行修正
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var destroy = false
            if (activity.isFinishing.orFalse) destroy = true
            if (activity.isDestroyed.orFalse) destroy = true
            if (activity.windowManager == null) destroy = true
            if (activity.window?.decorView == null) destroy = true
            if (activity.window?.decorView?.parent == null) destroy = true
            if (!destroy) {
                val decorView = activity.window.decorView
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
                shotList.add(it)
            }
        }
        //录屏文件创建/停止录屏时（exists=false）都会回调
        ScreenService.setOnScreenListener { filePath, recoding ->
            if (!recoding) {
                val folderPath = filePath.orEmpty()
                //说明未截图
                if (shotList.size == 0) {
                    onShutter.invoke(folderPath, false)
                } else {
                    //拿到保存的截屏文件夹地址下的所有文件目录，并将录屏源文件路径也添加进其中
                    shotList.add(folderPath)
                    //压缩包输出路径（会以录屏文件的命名方式来命名）
                    val zipPath = File(folderPath).name.replace("mp4", "zip")
                    //开始压包
                    fileHelper.zipJob(shotList, zipPath, { showDialog() }, {
                        hideDialog()
                        filePath.deleteFile()
                    })
                    onShutter.invoke(zipPath, true)
                }
            }
        }
    }

    private fun showDialog() {
        loadingDialog.shown(false)
    }

    private fun hideDialog() {
        loadingDialog.hidden()
    }

    /**
     * 开始录屏
     * 尝试唤起手机录屏弹窗，会在onActivityResult中回调结果
     */
    fun startScreen() = activity.execute {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            "请授权上层显示".shortToast()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        } else {
            val mediaProjectionManager = getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
            activityResultValue.launch(permissionIntent)
        }
    }

    /**
     * 结束录屏
     */
    fun stopScreen() = activity.execute {
        isRecording = false
        stopService(ScreenService::class.java)
    }

    /**
     * isZip->true是zip文件夹，可能包含录制时的截图
     */
    fun setOnScreenListener(onShutter: (filePath: String?, isZip: Boolean) -> Unit) {
        this.onShutter = onShutter
    }

    /**
     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> ShotObserver.instance.register()
            Lifecycle.Event.ON_DESTROY -> {
                hideDialog()
                stopScreen()
                ShotObserver.instance.unregister()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}