package com.example.thirdparty.media.utils

import android.app.Activity.RESULT_OK
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.FileBuilder
import com.example.common.utils.file.deleteFile
import com.example.common.utils.file.isExists
import com.example.common.utils.function.pullUpOverlay
import com.example.common.utils.function.pullUpScreen
import com.example.common.utils.function.registerResult
import com.example.framework.utils.function.startService
import com.example.framework.utils.function.stopService
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeSize
import com.example.thirdparty.R
import com.example.thirdparty.media.service.DisplayService
import com.example.thirdparty.media.service.ShotObserver
import java.io.File

/**
 * @description 录屏工具类
 * @author yan
 */
class DisplayHelper(private val mActivity: FragmentActivity, private val isZip: Boolean = false) : LifecycleEventObserver {
    private var isDestroy = false
    private var lastRefreshTime = 0L
    private var mView: BaseView? = null
    private var listener: OnDisplayListener? = null
    private val list by lazy { ArrayList<String>() }
    private val builder by lazy { FileBuilder(mActivity) }
    private val observer by lazy { ShotObserver(mActivity) }

    /**
     * 处理录屏的回调
     */
    private val result = mActivity.registerResult {
        list.clear()
        if (it.resultCode == RESULT_OK) {
            waitingTime = currentTimeNano - lastRefreshTime
            R.string.screenStart.shortToast()
            isRecording = true
            mActivity.apply {
                startService(DisplayService::class.java, Extra.RESULT_CODE to it.resultCode, Extra.BUNDLE_BEAN to it.data)
                moveTaskToBack(true)
            }
        } else {
            R.string.screenCancel.shortToast()
            isRecording = false
            listener?.onCancel()
        }
    }

    companion object {
        /**
         * 用于计算系统弹出弹框到正式开始录屏花费了多少时间（毫秒）
         */
        var waitingTime = 0L

        /**
         * 安全区间内的屏幕录制宽高
         */
        var previewWidth = screenWidth
        var previewHeight = screenHeight

        /**
         * 是否正在进行录制，便于区分截图捕获到的图片路径
         */
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
        if (isZip) {
            //只要在录屏中，截一张图就copy一张到目标目录，但是需要及时清空
            observer.setOnShotListener {
                it ?: return@setOnShotListener
                if (isRecording) {
                    if (!it.isExists()) return@setOnShotListener
                    list.add(it)
                }
            }
        }
        //录屏文件创建/停止录屏时（exists=false）都会回调
        DisplayService.setOnDisplayListener { folderPath, isRecoding ->
            if (isDestroy) return@setOnDisplayListener
            if (!isRecoding) {
                folderPath ?: return@setOnDisplayListener
                if (isZip) {
                    //说明未截图
                    if (list.safeSize == 0) {
                        listener?.onResult(folderPath, false)
                    } else {
                        //拿到保存的截屏文件夹地址下的所有文件目录，并将录屏源文件路径也添加进其中
                        list.add(folderPath)
                        //压缩包输出路径（会以录屏文件的命名方式来命名）
                        val zipPath = File(folderPath).name.replace("mp4", "zip")
                        //开始压包
                        builder.zipJob(list, zipPath, { mView?.showDialog() }, {
                            mView?.hideDialog()
                            folderPath.deleteFile()
                        })
                        listener?.onResult(zipPath, true)
                    }
                } else {
                    listener?.onResult(folderPath, false)
                }
            } else {
                listener?.onStart(folderPath)
            }
        }
    }

    /**
     * 设置加载参数
     */
    fun setBundle(mView: BaseView) {
        this.mView = mView
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
        stopService(DisplayService::class.java)
    }

    /**
     * 录屏监听
     */
    fun setOnDisplayListener(listener: OnDisplayListener) {
        this.listener = listener
    }

    /**
     * 回调监听
     */
    interface OnDisplayListener {
        /**
         * 正式开始录屏
         */
        fun onStart(filePath: String?)

        /**
         * 取消
         */
        fun onCancel()

        /**
         * isZip->true是zip文件夹，可能包含录制时的截图
         */
        fun onResult(filePath: String?, isZip: Boolean)
    }

    /**
     * 生命周期监听，不管录屏是否停止，页面销毁时都调取一次停止防止内存泄漏
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                isDestroy = true
                mView?.hideDialog()
                stopScreen()
                result?.unregister()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}