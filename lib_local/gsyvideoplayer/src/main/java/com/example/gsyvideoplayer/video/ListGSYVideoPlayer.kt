package com.example.gsyvideoplayer.video

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.example.framework.utils.function.value.orZero
import com.shuyu.gsyvideoplayer.model.GSYVideoModel
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import moe.codeest.enviews.ENDownloadView
import java.io.File

/**
 * 列表播放支持
 * Created by shuyu on 2016/12/20.
 */
open class ListGSYVideoPlayer : StandardGSYVideoPlayer {
    protected var mUriList: List<GSYVideoModel> = ArrayList()

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cacheWithPlay 是否边播边缓存
     * @return
     */
    protected open fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int): Boolean {
        return setUp(url, cacheWithPlay, position, null, HashMap())
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    protected open fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?): Boolean {
        return setUp(url, cacheWithPlay, position, cachePath, HashMap())
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @return
     */
    protected open fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?, mapHeadData: Map<String, String>): Boolean {
        return setUp(url, cacheWithPlay, position, cachePath, mapHeadData, true)
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    protected open fun setUp(url: List<GSYVideoModel>, cacheWithPlay: Boolean, position: Int, cachePath: File?, mapHeadData: Map<String, String>, changeState: Boolean): Boolean {
        mUriList = url
        mPlayPosition = position
        mMapHeadData = mapHeadData
        val gsyVideoModel = url[position]
        val set = setUp(gsyVideoModel.url, cacheWithPlay, cachePath, gsyVideoModel.title, changeState)
        if (!TextUtils.isEmpty(gsyVideoModel.title) && mTitleTextView != null) {
            mTitleTextView.text = gsyVideoModel.title
        }
        return set
    }

    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        val sf = from as? ListGSYVideoPlayer
        val st = to as? ListGSYVideoPlayer
        st?.mPlayPosition = sf?.mPlayPosition.orZero
        st?.mUriList = sf?.mUriList.orEmpty()
    }

    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer? {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        if (gsyBaseVideoPlayer != null) {
            val listGSYVideoPlayer = gsyBaseVideoPlayer as? ListGSYVideoPlayer
            val gsyVideoModel = mUriList[mPlayPosition]
            if (!TextUtils.isEmpty(gsyVideoModel.title) && mTitleTextView != null) {
                listGSYVideoPlayer?.mTitleTextView?.text = gsyVideoModel.title
            }
        }
        return gsyBaseVideoPlayer
    }

    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        if (gsyVideoPlayer != null) {
            val gsyVideoModel = mUriList[mPlayPosition]
            if (!TextUtils.isEmpty(gsyVideoModel.title) && mTitleTextView != null) {
                mTitleTextView.text = gsyVideoModel.title
            }
        }
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
    }

    override fun onCompletion() {
        releaseNetWorkState()
        if (mPlayPosition < (mUriList.size)) {
            return
        }
        super.onCompletion()
    }

    override fun onAutoCompletion() {
        if (playNext()) {
            return
        }
        super.onAutoCompletion()
    }

    override fun releaseVideos() {
        /// fix https://github.com/CarGuo/GSYVideoPlayer/issues/3892
        super.onCompletion()
        super.releaseVideos()
    }

    /**
     * 开始状态视频播放，prepare时不执行  addTextureView();
     */
    override fun prepareVideo() {
        super.prepareVideo()
        if (mHadPlay && mPlayPosition < (mUriList.size)) {
            setViewShowState(mLoadingProgressBar, VISIBLE)
            if (mLoadingProgressBar is ENDownloadView) {
                (mLoadingProgressBar as? ENDownloadView)?.start()
            }
        }
    }

    override fun onPrepared() {
        super.onPrepared()
    }

    override fun changeUiToNormal() {
        super.changeUiToNormal()
        if (mHadPlay && mPlayPosition < (mUriList.size)) {
            setViewShowState(mThumbImageViewLayout, GONE)
            setViewShowState(mTopContainer, INVISIBLE)
            setViewShowState(mBottomContainer, INVISIBLE)
            setViewShowState(mStartButton, GONE)
            setViewShowState(mLoadingProgressBar, VISIBLE)
            setViewShowState(mBottomProgressBar, INVISIBLE)
            setViewShowState(mLockScreen, GONE)
            if (mLoadingProgressBar is ENDownloadView) {
                (mLoadingProgressBar as? ENDownloadView)?.start()
            }
        }
    }

    /**
     * 播放下一集
     *
     * @return true表示还有下一集
     */
    fun playNext(): Boolean {
        if (mPlayPosition < (mUriList.size - 1)) {
            mPlayPosition += 1
            val gsyVideoModel = mUriList[mPlayPosition]
            mSaveChangeViewTIme = 0
            setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false)
            if (!TextUtils.isEmpty(gsyVideoModel.title) && mTitleTextView != null) {
                mTitleTextView.text = gsyVideoModel.title
            }
            startPlayLogic()
            return true
        }
        return false
    }

}