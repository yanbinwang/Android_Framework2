package com.example.thirdparty.media.widget

import android.content.Context
import android.util.AttributeSet
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

/**
 * 1.默认情况下绘制StandardGSYVideoPlayer即可满足需求
 * 2.如需对底部导航栏和拖动手势加载的ui做再深度定制，则使用当前类
 * 3.查看id可导入video_layout_standard.xml
 */
class VideoPlayerLayout : StandardGSYVideoPlayer {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
//        //顶部整体菜单（包含返回，标题头）->自定义
//        mTopContainer
//        //顶部返回/标题
//        backButton
//        titleTextView
        //底部整体（包含当前播放时间，进度条，总时长，全屏按钮）
//        mBottomContainer
//        mCurrentTimeTextView
//        mProgressBar
//        mTotalTimeTextView
//        //设置全屏按钮的展开，收缩图片
//        enlargeImageRes
//        shrinkImageRes
//        //获取全屏按钮->自定义大小
//        fullscreenButton
    }

}