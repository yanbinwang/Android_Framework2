package com.example.qiniu.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.qiniu.widget.CameraPreviewFrameView
import com.qiniu.qmedia.component.player.QMediaModelBuilder
import com.qiniu.qmedia.component.player.QPlayerSetting
import com.qiniu.qmedia.component.player.QURLType
import com.qiniu.qmedia.ui.QSurfacePlayerView
import kotlinx.coroutines.CoroutineScope

/**
 * 七牛云播放器帮助类
 * https://developer.qiniu.com/pili/12220/qplayer2-core-quick-start
 * 1.页面实现布局
 * <com.qiniu.qmedia.ui.QSurfacePlayerView
 * android:id="@+id/player_view"
 * android:layout_width="match_parent"
 * android:layout_height="match_parent">
 * </com.qiniu.qmedia.ui.QSurfacePlayerView>
 *  调取bind方法，开启直播
 */
class QMediaHelper (private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var mQSurfacePlayerView:QSurfacePlayerView?=null

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定播放控件
     */
    fun bind(mQSurfacePlayerView: QSurfacePlayerView?) {
        this.mQSurfacePlayerView = mQSurfacePlayerView
        mQSurfacePlayerView?.playerControlHandler?.init(mActivity)
        mQSurfacePlayerView?.playerControlHandler?.setDecodeType(QPlayerSetting.QPlayerDecoder.QPLAYER_DECODER_SETTING_AUTO)
        mQSurfacePlayerView?.playerControlHandler?.setDecodeType(QPlayerSetting.QPlayerDecoder.QPLAYER_DECODER_SETTING_AUTO)
        mQSurfacePlayerView?.playerControlHandler?.setSeekMode(QPlayerSetting.QPlayerSeek.QPLAYER_SEEK_SETTING_NORMAL)
        mQSurfacePlayerView?.playerControlHandler?.setStartAction(QPlayerSetting.QPlayerStart.QPLAYER_START_SETTING_PLAYING)
        mQSurfacePlayerView?.playerControlHandler?.setSpeed(1f)
    }

    /**
     * 开始播放
     */
    fun startPlayLogic(url: String) {
//        url = "http://demo-videos.qnsdk.com/movies/qiniu.mp4"
        val builder = QMediaModelBuilder()
        builder.addElement("", QURLType.QAUDIO_AND_VIDEO, 0, url, true)
        mQSurfacePlayerView?.playerControlHandler?.playMediaModel(builder.build(false), 0)
        mQSurfacePlayerView?.playerControlHandler?.stop()
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> destroy()
            else -> {}
        }
    }

    /**
     * 恢复播放
     */
    private fun resume() {
        mQSurfacePlayerView?.playerControlHandler?.resumeRender()
    }

    /**
     * 暂停播放
     */
    private fun pause() {
        mQSurfacePlayerView?.playerControlHandler?.pauseRender()
    }

    /**
     * 销毁
     */
    private fun destroy() {
        mQSurfacePlayerView?.playerControlHandler?.release()
        mActivity.lifecycle.removeObserver(this)
    }

}