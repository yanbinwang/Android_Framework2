package com.example.multimedia.utils.helper

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
import com.example.common.utils.file.copyFile
import com.example.common.utils.file.deleteDir
import com.example.common.utils.file.deleteFile
import com.example.common.utils.file.isMkdirs
import com.example.common.utils.helper.AccountHelper.storage
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logWTF
import com.example.multimedia.service.ScreenService
import com.example.multimedia.service.ScreenShotObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * @description 录屏工具类
 * @author yan
 */
class ScreenHelper(private val activity: FragmentActivity) : LifecycleEventObserver, CoroutineScope {
    private val loadingDialog by lazy { LoadingDialog(activity) }
    private val shotFile by lazy { File("${storage}录屏/截屏".isMkdirs()) }
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)
    private var job: Job? = null

    /**
     * 处理录屏的回调
     */
    private val activityResultValue = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            isStart = true
            "开始录屏".shortToast()
            clearCache()
            activity.startService(ScreenService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
            activity.moveTaskToBack(true)
        } else {
            isStart = false
            "取消录屏".shortToast()
        }
    }

    companion object {
        var isStart = false
        var previewWidth = 0
        var previewHeight = 0
    }

    init {
        activity.lifecycle.addObserver(this)
        //获取录屏屏幕宽高，高版本进行修正
        previewWidth = screenWidth
        previewHeight = screenHeight
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
        ScreenShotObserver.setOnScreenShotListener {
            if (isStart) {
                it ?: return@setOnScreenShotListener
                File(it).copyFile(shotFile)
            }
        }
        //录屏文件创建/停止录屏时（exists=false）都会回调
        ScreenService.setOnScreenListener { filePath, exists ->
            if (!exists) {
                filePath ?: return@setOnScreenListener
                //拿到保存的截屏文件夹地址下的所有文件目录
                val containList = shotFile.list()?.toMutableList()
                containList?.add(filePath)
                job?.cancel()
                job = launch {
                    showDialog()
                    val zipPath = File(filePath).name.replace("mp4", "zip")
                    withContext(Dispatchers.IO) { zipFolder("${storage}录屏", zipPath, containList) }
                    //打包成功清空录屏源文件和截屏文件夹
                    clearCache(filePath)
                    hideDialog()
                }
            }
        }
    }

    private fun clearCache(filePath: String = "") {
        if (filePath.isNotEmpty()) filePath.deleteFile()
        shotFile.absolutePath.deleteDir()
    }

    /**
     * 将指定路径下的所有文件打成压缩包
     * File fileDir = new File(rootDir + "/DCIM/Screenshots");
     * File zipFile = new File(rootDir + "/" + taskId + ".zip");
     *
     * @param srcFilePath 要压缩的文件或文件夹路径
     * @param zipFilePath 压缩完成的Zip路径
     */
    @Throws(Exception::class)
    private fun zipFolder(srcFilePath: String, zipFilePath: String, containList: MutableList<String>? = null) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFilePath))
        //创建文件
        val file = File(srcFilePath)
        //压缩
        zipFiles(file.parent + File.separator, file.name, outZip, containList)
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    @Throws(Exception::class)
    private fun zipFiles(folderPath: String, fileName: String, zipOutputSteam: ZipOutputStream?, containList: MutableList<String>? = null) {
        " \n压缩路径:$folderPath\n压缩文件名:$fileName".logWTF
        if (zipOutputSteam == null) return
        val file = File(folderPath + fileName)
        //是否需要做排除
        if (containList != null) {
            if (containList.contains(file.absolutePath).orFalse) startZip(folderPath, fileName, zipOutputSteam, containList)
        } else {
            startZip(folderPath, fileName, zipOutputSteam)
        }
    }

    @Throws(Exception::class)
    private fun startZip(folderPath: String, fileName: String, zipOutputSteam: ZipOutputStream?, containList: MutableList<String>? = null) {
        if (zipOutputSteam == null) return
        val file = File(folderPath + fileName)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileName)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            file.list().let {
                //没有子文件和压缩
                if (it.isNullOrEmpty()) {
                    val zipEntry = ZipEntry(fileName + File.separator)
                    zipOutputSteam.putNextEntry(zipEntry)
                    zipOutputSteam.closeEntry()
                } else {
                    //子文件和递归
                    for (i in it.indices) {
                        startZip("$folderPath$fileName/", it[i], zipOutputSteam, containList)
                    }
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
        isStart = false
        stopService(ScreenService::class.java)
    }

    /**
     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> ScreenShotObserver.instance.register()
            Lifecycle.Event.ON_DESTROY -> {
                hideDialog()
                stopScreen()
                ScreenShotObserver.instance.unregister()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}