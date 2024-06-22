package com.example.qiniu.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 七牛云推流帮助类
 * https://developer.qiniu.com/pili/3718/PLDroidMediaStreaming-quick-start
 * 1.application里初始化StreamingEnv.init(getApplicationContext(), Util.getUserId(getApplicationContext()));
 * 2.页面实现布局
 *  <com.qiniu.pili.droid.streaming.demo.ui.CameraPreviewFrameView
 *         android:id="@+id/cameraPreview_surfaceView"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         android:layout_gravity="center" />
 *  调取bind方法
 */
class StreamingHelper(owner: LifecycleOwner) : CoroutineScope, LifecycleEventObserver {
    private var publishURLFromServer:String?=null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        owner.lifecycle.addObserver(this)
    }



    /**
     * 设置推流地址
     */
    fun setParams(publishURLFromServer :String){
        this.publishURLFromServer = publishURLFromServer
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
//            Lifecycle.Event.ON_RESUME -> resume()
//            Lifecycle.Event.ON_PAUSE -> pause()
//            Lifecycle.Event.ON_DESTROY -> destroy()
            else -> {}
        }
    }
}