package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.BaseApplication
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.decodeResource
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.orFalse
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
 * // 初始化分享配置
 * wxShare.config(
 *     message = WXShareMessage(
 *         title = "分享标题",
 *         description = "分享描述",
 *         messageExt = "额外参数"
 *     ),
 *     bitmap = yourBitmap
 * ) {
 *     // 配置完成后调用分享
 *     shareWebPage("https://example.com")
 * }
 */
class WXShare(private val owner: LifecycleOwner) {
    //分享缩略图byte
    private var mThumbByte: ByteArray? = null
    //分享信息
    private var mShareMessage: WXShareMessage? = null
    //通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXManager.instance.regToWx(owner) }

    /**
     * 设置分享基础信息
     * 每一个分享之前先进行config配置，确定一下本次分享的基础值
     */
    fun config(message: WXShareMessage? = null, bitmap: Bitmap? = null, block: (builder: WXShare) -> Unit = {}) {
        mShareMessage = message ?: WXShareMessage()
        val bmp = bitmap ?: if (mThumbByte == null) {
            BaseApplication.instance.decodeResource(R.mipmap.ic_share)
        } else {
            null
        }
        if (bmp != null) {
            owner.lifecycleScope.launch {
                mThumbByte = buildThumb(bmp)
                block.invoke(this@WXShare)
            }
        } else {
            block.invoke(this)
        }
    }

    /**
     * 获取分享需要的100*100的缩略图（摆在左侧）BaseApplication.instance.decodeResource(R.mipmap.ic_share)
     */
    private suspend fun buildThumb(bmp: Bitmap?, THUMB_SIZE: Int = 100) = withContext(IO) {
        bmp?.scale(THUMB_SIZE, THUMB_SIZE)?.let { thumbBmp ->
            bmp.recycle()
            bmpToByteArray(thumbBmp, true)
        } ?: ByteArray(0)
    }

    /**
     * 文字类型分享
     * val textObj = WXTextObject()
     * textObj.text = result?.text
     *
     * val msg = WXMediaMessage(textObj)
     * msg.description = message?.description
     */
    fun shareText(text: String, scene: Int = SendMessageToWX.Req.WXSceneSession) {
        share(WXTextObject().also { it.text = text }, "text", scene)
    }

    /**
     * 图片类型分享
     * imageData	byte[]	图片的二进制数据	内容大小不超过 1MB
     * imagePath	String	图片的本地路径	对应图片内容大小不超过 25MB
     * val imgObj = WXImageObject(bmp)//调用的其实是imageData，我们可以下载完要分享的bitmap后进行压缩，然后调用bmpToByteArray(thumbBmp, true)生成bmp
     *
     * val msg = WXMediaMessage(imgObj)
     * msg.thumbData = thumbData
     */
    fun shareImage(bmp: Bitmap, scene: Int = SendMessageToWX.Req.WXSceneSession) {
        share(WXImageObject(bmp), "img", scene)
    }

    /**
     * 视频类型分享
     * val video = WXVideoObject()
     * video.videoUrl = result?.videoUrl
     *
     * val msg = WXMediaMessage(video)
     * msg.title = message?.title
     * msg.description = message?.description
     * msg.thumbData = thumbData
     */
    fun shareVideo(videoUrl: String, scene: Int = SendMessageToWX.Req.WXSceneSession) {
        share(WXVideoObject().also { it.videoUrl = videoUrl }, "video", scene)
    }

    /**
     * 网页类型分享
     * val webpage = WXWebpageObject()
     * webpage.webpageUrl = result?.webpageUrl
     *
     * val msg = WXMediaMessage(webpage)
     * msg.title = message?.title
     * msg.description = message?.description
     * msg.thumbData = thumbData
     */
    fun shareWebPage(webpageUrl: String, scene: Int = SendMessageToWX.Req.WXSceneSession) {
        share(WXWebpageObject().also { it.webpageUrl = webpageUrl }, "webpage", scene)
    }

    /**
     * 小程序类型分享(小程序目前只支持会话->SendMessageToWX.Req.WXSceneSession)
     * val miniProgramObj = WXMiniProgramObject()
     * miniProgramObj.webpageUrl = result?.webpageUrl//兼容低版本的网页链接
     * miniProgramObj.miniprogramType = result?.miniprogramType.orZero // 正式版:0，测试版:1，体验版:2
     * miniProgramObj.userName = result?.userName//小程序原始id
     * miniProgramObj.path = result?.path //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     *
     * val msg = WXMediaMessage(miniProgramObj)
     * msg.title = message?.title//小程序消息title
     * msg.description = message?.description// 小程序消息desc
     * msg.thumbData = thumbData// 小程序消息封面图片，小于128k
     */
    fun shareMiniProgram(webpageUrl: String, miniprogramType: Int, userName: String, path: String) {
        //小程序目前只支持会话
        share(WXMiniProgramObject().also {
            it.webpageUrl = webpageUrl//兼容低版本的网页链接
            it.miniprogramType = miniprogramType//正式版:0，测试版:1，体验版:2
            it.userName = userName//小程序原始id
            it.path = path//小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
        }, "miniProgram", SendMessageToWX.Req.WXSceneSession)
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
     *
     * val msg = WXMediaMessage(musicVideo)
     * msg.title = message?.title // 必填，不能为空
     * msg.description = message?.description // 选填，建议与歌手名字段 singerName 保持一致
     * msg.messageExt = "额外信息" // 微信跳回应用时会带上
     * msg.thumbData = thumbData// 音乐卡片缩略图，不超过64KB
     */
    private fun shareMusic(musicUrl: String, musicDataUrl: String, scene: Int = SendMessageToWX.Req.WXSceneSession) {
        share(WXMusicVideoObject().also {
            it.musicUrl = musicUrl//音乐url
            it.musicDataUrl = musicDataUrl// 音乐音频url
//            //以下内容不做定制
//            it.songLyric = "xxx"
//            it.hdAlbumThumbFilePath = "xxx"
//            it.singerName = "xxx"
//            it.albumName = "album_xxx"
//            it.musicGenre = "流行歌曲"
//            it.issueDate = 1610713585
//            it.identification = "sample_identification"
//            it.duration = 120000
        }, "musicVideo", scene)
    }

    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html
     *
     * SendMessageToWX.Req.WXSceneSession->朋友
     * SendMessageToWX.Req.WXSceneTimeline->朋友圈
     */
    private fun <T : WXMediaMessage.IMediaObject> share(mediaObject: T, transaction: String, scene: Int) {
        //未安装
        if (!wxApi?.isWXAppInstalled.orFalse) {
            R.string.wechatUnInstalled.shortToast()
            return
        }
        //版本不支持
        if (!(try {
                wxApi?.wxAppSupportAPI != 0
            } catch (e: Exception) {
                false
            })) {
            R.string.wechatSupportError.shortToast()
        }
        val message = WXMediaMessage(mediaObject).apply {
            mShareMessage?.let {
                title = it.title
                description = it.description
                thumbData = mThumbByte
                messageExt = it.messageExt
            }
        }
        owner.lifecycleScope.launch {
            try {
                wxApi?.sendReq(
                    SendMessageToWX.Req().apply {
                        this.transaction = buildTransaction(transaction)
                        this.message = message
                        this.scene = scene
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 声明一个唯一的请求值
     */
    private fun buildTransaction(transaction: String): String {
        return "${AccountHelper.getUserId()}::${transaction}::$currentTimeNano::${UUID.randomUUID()}"
    }

}