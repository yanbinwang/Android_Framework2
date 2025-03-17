package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.orZero
import com.example.thirdparty.share.wechat.WXShareUtil.bmpToByteArray
import com.example.thirdparty.share.wechat.bean.WXShareResult
import com.example.thirdparty.utils.wechat.WXManager
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.modelmsg.WXMusicVideoObject
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.modelmsg.WXVideoObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * 微信分享构建
 */
class WXShare(private val owner: LifecycleOwner) {
    //分享信息
    private var result: WXShareResult? = null
    //通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXManager.instance.regToWx(owner) }

    /**
     * 设置分享信息
     */
    fun setResult(result: WXShareResult?) {
        this.result = result
    }

    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html
     *
     * SendMessageToWX.Req.WXSceneSession->朋友
     * SendMessageToWX.Req.WXSceneTimeline->朋友圈
     */
    fun shareToWx(mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        owner.lifecycleScope.launch {
            if (result?.obj == WXShareResult.Obj.TEXT) {
                shareText(mTargetScene)
            } else {
                val bmp = result?.bmp?.get() ?: return@launch
                val thumbData = buildThumb(bmp)
                when (result?.obj) {
                    WXShareResult.Obj.IMAGE -> shareImage(bmp, thumbData, mTargetScene)
                    WXShareResult.Obj.VIDEO -> shareVideo(thumbData, mTargetScene)
                    WXShareResult.Obj.WEB_PAGE -> shareWebPage(thumbData, mTargetScene)
                    WXShareResult.Obj.MINI_PROGRAM -> shareMiniProgram(thumbData)
                    else -> shareMusic(thumbData, mTargetScene)
                }
            }
        }
    }

    private suspend fun buildThumb(bmp: Bitmap, THUMB_SIZE: Int = 100): ByteArray {
        return withContext(IO) {
            //设置缩略图
            val thumbBmp = bmp.scale(THUMB_SIZE, THUMB_SIZE)
            bmp.recycle()
            bmpToByteArray(thumbBmp, true)
        }
    }

    private fun shareText(mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        //初始化一个 WXTextObject 对象，填写分享的文本内容
        val textObj = WXTextObject()
        textObj.text = result?.text
        //用 WXTextObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.description = result?.description
        share(msg, "text", mTargetScene)
    }

    private fun shareImage(bmp: Bitmap, thumbData: ByteArray, mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        //初始化 WXImageObject 和 WXMediaMessage 对象
        val imgObj = WXImageObject(bmp)
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        //设置缩略图
        msg.thumbData = thumbData
        share(msg, "img", mTargetScene)
    }

    private fun shareVideo(thumbData: ByteArray, mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        //初始化一个WXVideoObject，填写url
        val video = WXVideoObject()
        video.videoUrl = result?.videoUrl
        //用 WXVideoObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(video)
        msg.title = result?.title
        msg.description = result?.description
        //设置缩略图
        msg.thumbData = thumbData
        share(msg, "video", mTargetScene)
    }

    private fun shareWebPage(thumbData: ByteArray, mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        //初始化一个WXWebpageObject，填写url
        val webpage = WXWebpageObject()
        webpage.webpageUrl = result?.webpageUrl
        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(webpage)
        msg.title = result?.title
        msg.description = result?.description
        //设置缩略图
        msg.thumbData = thumbData
        share(msg, "webpage", mTargetScene)
    }

    private fun shareMiniProgram(thumbData: ByteArray) {
        val miniProgramObj = WXMiniProgramObject()
        miniProgramObj.webpageUrl = result?.webpageUrl//兼容低版本的网页链接
        miniProgramObj.miniprogramType = result?.miniprogramType.orZero // 正式版:0，测试版:1，体验版:2
        miniProgramObj.userName = result?.userName//小程序原始id
        miniProgramObj.path = result?.path //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
        val msg = WXMediaMessage(miniProgramObj)
        msg.title = result?.title//小程序消息title
        msg.description = result?.description// 小程序消息desc
        // 小程序消息封面图片，小于128k
        msg.thumbData = thumbData
        //小程序目前只支持会话
        share(msg, "miniProgram", SendMessageToWX.Req.WXSceneSession)
    }

    private fun shareMusic(thumbData: ByteArray, mTargetScene: Int = SendMessageToWX.Req.WXSceneSession) {
        val musicVideo = WXMusicVideoObject()
        musicVideo.musicUrl = result?.musicUrl // 音乐url
        musicVideo.musicDataUrl = result?.musicDataUrl // 音乐音频url
        //以下内容不做定制
        musicVideo.songLyric = "xxx" // 歌词
        musicVideo.hdAlbumThumbFilePath = "xxx" // 专辑图本地文件路径
        musicVideo.singerName = "xxx"
        musicVideo.albumName = "album_xxx"
        musicVideo.musicGenre = "流行歌曲"
        musicVideo.issueDate = 1610713585
        musicVideo.identification = "sample_identification"
        musicVideo.duration = 120000 // 单位为毫秒
        val msg = WXMediaMessage(musicVideo)
        msg.title = result?.title // 必填，不能为空
        msg.description = result?.description // 选填，建议与歌手名字段 singerName 保持一致
        msg.messageExt = "额外信息" // 微信跳回应用时会带上
        // 音乐卡片缩略图，不超过64KB
        msg.thumbData = thumbData
        share(msg, "musicVideo", mTargetScene)
    }

    private fun share(msg: WXMediaMessage, transaction: String, scene: Int) {
        //构造一个Req
        val req = SendMessageToWX.Req()
        req.transaction = "${AccountHelper.getUserId()}::${transaction}::${currentTimeNano}::${UUID.randomUUID()}"
        req.message = msg
        req.scene = scene // 支持会话、朋友圈、收藏
        //调用api接口，发送数据到微信
        wxApi?.sendReq(req)
    }

}