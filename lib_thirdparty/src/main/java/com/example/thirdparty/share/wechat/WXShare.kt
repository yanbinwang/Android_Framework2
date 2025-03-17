package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.BaseApplication
import com.example.common.utils.function.decodeResource
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.currentTimeNano
import com.example.thirdparty.R
import com.example.thirdparty.share.wechat.WXShareUtil.bmpToByteArray
import com.example.thirdparty.share.wechat.bean.WXShareMessage
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
    //分享缩略图byte
    private var thumbByte: ByteArray? = null
    //分享信息
    private var config: WXShareMessage? = null
    //通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXManager.instance.regToWx(owner) }

    /**
     * 设置分享基础信息
     * 每一个分享之前先进行config配置，确定一下本次分享的基础值
     */
    fun setConfiguration(config: WXShareMessage?, bitmap: Bitmap? = null, block: () -> Unit = {}) {
        this.config = config
        val targetBitmap = bitmap ?: thumbByte?.let { return@setConfiguration } ?: BaseApplication.instance.decodeResource(R.mipmap.ic_share)
        owner.lifecycleScope.launch {
            thumbByte = buildThumb(targetBitmap)
            block.invoke()
        }
    }

    /**
     * 获取分享需要的100*100的缩略图（摆在左侧）BaseApplication.instance.decodeResource(R.mipmap.ic_share)
     */
    private suspend fun buildThumb(bmp: Bitmap?, THUMB_SIZE: Int = 100): ByteArray {
        return withContext(IO) {
            //设置缩略图
            val thumbBmp = bmp?.scale(THUMB_SIZE, THUMB_SIZE)
            bmp?.recycle()
            bmpToByteArray(thumbBmp, true)
        }
    }

    /**
     * 文字类型分享
     * //初始化一个 WXTextObject 对象，填写分享的文本内容
     * val textObj = WXTextObject()
     * textObj.text = result?.text
     * //用 WXTextObject 对象初始化一个 WXMediaMessage 对象
     * val msg = WXMediaMessage(textObj)
     * msg.description = message?.description
     * share(msg, "text", mTargetScene)
     */
    fun shareText(text: String) {
        //初始化一个 WXTextObject 对象，填写分享的文本内容
        val textObj = WXTextObject()
        textObj.text = text
        share(WXMediaMessage(textObj).rebuild(), "text")
    }

    /**
     * 图片类型分享
     * imageData	byte[]	图片的二进制数据	内容大小不超过 1MB
     * imagePath	String	图片的本地路径	对应图片内容大小不超过 25MB
     * 初始化 WXImageObject 和 WXMediaMessage 对象
     * val imgObj = WXImageObject(bmp)//调用的其实是imageData，我们可以下载完要分享的bitmap后进行压缩，然后调用bmpToByteArray(thumbBmp, true)生成bmp
     * val msg = WXMediaMessage(imgObj)
     * //设置缩略图
     * msg.thumbData = thumbData
     */
    fun shareImage(bmp: Bitmap) {
        //初始化 WXImageObject 和 WXMediaMessage 对象
        val imgObj = WXImageObject(bmp)
        share(WXMediaMessage(imgObj).rebuild(), "img")
    }

    /**
     * 视频类型分享
     * //初始化一个WXVideoObject，填写url
     * val video = WXVideoObject()
     * video.videoUrl = result?.videoUrl
     * //用 WXVideoObject 对象初始化一个 WXMediaMessage 对象
     * val msg = WXMediaMessage(video)
     * msg.title = message?.title
     * msg.description = message?.description
     * //设置缩略图
     * msg.thumbData = thumbData
     * share(msg, "video", mTargetScene)
     */
    fun shareVideo(videoUrl: String) {
        //初始化一个WXVideoObject，填写url
        val video = WXVideoObject()
        video.videoUrl = videoUrl
        share(WXMediaMessage(video).rebuild(), "video")
    }

    /**
     * 网页类型分享
     * //初始化一个WXWebpageObject，填写url
     * val webpage = WXWebpageObject()
     * webpage.webpageUrl = result?.webpageUrl
     * //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
     * val msg = WXMediaMessage(webpage)
     * msg.title = message?.title
     * msg.description = message?.description
     * //设置缩略图
     * msg.thumbData = thumbData
     * share(msg, "webpage", mTargetScene)
     */
    fun shareWebPage(webpageUrl: String) {
        //初始化一个WXWebpageObject，填写url
        val webpage = WXWebpageObject()
        webpage.webpageUrl = webpageUrl
        share(WXMediaMessage(webpage).rebuild(), "webpage")
    }

    /**
     * 小程序类型分享
     * val miniProgramObj = WXMiniProgramObject()
     * miniProgramObj.webpageUrl = result?.webpageUrl//兼容低版本的网页链接
     * miniProgramObj.miniprogramType = result?.miniprogramType.orZero // 正式版:0，测试版:1，体验版:2
     * miniProgramObj.userName = result?.userName//小程序原始id
     * miniProgramObj.path = result?.path //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     * val msg = WXMediaMessage(miniProgramObj)
     * msg.title = message?.title//小程序消息title
     * msg.description = message?.description// 小程序消息desc
     * // 小程序消息封面图片，小于128k
     * msg.thumbData = thumbData
     * //小程序目前只支持会话
     * share(msg, "miniProgram", SendMessageToWX.Req.WXSceneSession)
     */
    fun shareMiniProgram(webpageUrl: String, miniprogramType: Int, userName: String, path: String) {
        val miniProgramObj = WXMiniProgramObject()
        miniProgramObj.webpageUrl = webpageUrl//兼容低版本的网页链接
        miniProgramObj.miniprogramType = miniprogramType//正式版:0，测试版:1，体验版:2
        miniProgramObj.userName = userName//小程序原始id
        miniProgramObj.path = path//小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
        //小程序目前只支持会话
        share(WXMediaMessage(miniProgramObj).rebuild(), "miniProgram")
    }

    /**
     * 音乐视频类型分享
     * val musicVideo = WXMusicVideoObject()
     * musicVideo.musicUrl = result?.musicUrl // 音乐url
     * musicVideo.musicDataUrl = result?.musicDataUrl // 音乐音频url
     * //以下内容不做定制
     * musicVideo.songLyric = "xxx" // 歌词
     * musicVideo.hdAlbumThumbFilePath = "xxx" // 专辑图本地文件路径
     * musicVideo.singerName = "xxx"
     * musicVideo.albumName = "album_xxx"
     * musicVideo.musicGenre = "流行歌曲"
     * musicVideo.issueDate = 1610713585
     * musicVideo.identification = "sample_identification"
     * musicVideo.duration = 120000 // 单位为毫秒
     * val msg = WXMediaMessage(musicVideo)
     * msg.title = message?.title // 必填，不能为空
     * msg.description = message?.description // 选填，建议与歌手名字段 singerName 保持一致
     * msg.messageExt = "额外信息" // 微信跳回应用时会带上
     * // 音乐卡片缩略图，不超过64KB
     * msg.thumbData = thumbData
     * share(msg, "musicVideo", mTargetScene)
     */
    private fun shareMusic(musicUrl: String, musicDataUrl: String) {
        val musicVideo = WXMusicVideoObject()
        musicVideo.musicUrl = musicUrl//音乐url
        musicVideo.musicDataUrl = musicDataUrl// 音乐音频url
//        //以下内容不做定制
//        musicVideo.songLyric = "xxx" // 歌词
//        musicVideo.hdAlbumThumbFilePath = "xxx" // 专辑图本地文件路径
//        musicVideo.singerName = "xxx"
//        musicVideo.albumName = "album_xxx"
//        musicVideo.musicGenre = "流行歌曲"
//        musicVideo.issueDate = 1610713585
//        musicVideo.identification = "sample_identification"
//        musicVideo.duration = 120000 // 单位为毫秒
        share(WXMediaMessage(musicVideo).rebuild(), "musicVideo")
    }

    /**
     * 根据传入的WXShareMessage重新构造一下给微信的分享类
     */
    private fun WXMediaMessage.rebuild(): WXMediaMessage {
        title = config?.title
        description = config?.description
        thumbData = thumbByte
        messageExt = config?.messageExt
        return this
    }

    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html
     *
     * SendMessageToWX.Req.WXSceneSession->朋友
     * SendMessageToWX.Req.WXSceneTimeline->朋友圈
     */
    private fun share(message: WXMediaMessage, transaction: String) {
        //构造一个Req
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction(transaction)
        req.message = message
        req.scene = config?.scene ?: SendMessageToWX.Req.WXSceneSession // 支持会话、朋友圈、收藏
        //调用api接口，发送数据到微信
        wxApi?.sendReq(req)
    }

    /**
     * 声明一个唯一的请求值
     */
    private fun buildTransaction(transaction: String): String {
        return "${AccountHelper.getUserId()}::${transaction}::$currentTimeNano::${UUID.randomUUID()}"
    }

}