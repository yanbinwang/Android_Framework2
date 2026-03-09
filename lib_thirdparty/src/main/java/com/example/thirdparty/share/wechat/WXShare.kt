package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.base.bridge.BaseView
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.decodeResource
import com.example.common.utils.function.safeRecycle
import com.example.common.utils.function.string
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.orFalse
import com.example.thirdparty.R
import com.example.thirdparty.share.wechat.WXShareUtil.bitmapToByteArray
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
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
class WXShare(private val mActivity: FragmentActivity) {
    // 事务协程
    private var configJob: Job? = null
    private var shareJob: Job? = null
    // 分享缩略图byte
    private var mThumbByte: ByteArray? = null
    // 分享信息
    private var mShareMessage: WXShareMessage? = null
    // 获取页面协程上下文
    private val mScope get() = mActivity.lifecycleScope
    // 通过WXAPIFactory工厂，获取IWXAPI的实例
    private val wxApi by lazy { WXManager.instance.regToWx(mActivity) }

    companion object {
        // 微信分享缩略图尺寸要求（左侧 -> 100×100）
        private const val THUMB_SIZE = 100

        // 微信缩略图大小限制（官方要求≤128KB）
        private const val MAX_THUMB_SIZE_KB = 128

        /**
         * 【公开工具方法】生成微信分享专用缩略图
         * @param targetBmp 原始Bitmap（非空）
         * @return 100×100、≤128KB的缩略图字节数组
         * @throws RuntimeException 生成失败时抛出（如Bitmap解码失败）
         */
        suspend fun suspendingBuildThumb(targetBmp: Bitmap): ByteArray {
            return withContext(IO) {
                // 获取图片的字节数组
                val thumbByte = targetBmp.scale(THUMB_SIZE, THUMB_SIZE).let { thumbBmp ->
                    targetBmp.safeRecycle()
                    bitmapToByteArray(thumbBmp, true)
                } ?: throw RuntimeException(string(R.string.shareFailure))
                // 校验缩略图大小，避免超过微信限制 (压缩到符合要求的大小)
                if (thumbByte.size / 1024 <= MAX_THUMB_SIZE_KB) {
                    thumbByte
                } else {
                    suspendingCompressByteArray(thumbByte)
                }
            }
        }

        /**
         * 【内部工具方法】压缩字节数组到指定大小
         * @param byteArray 待压缩的字节数组（非空）
         * @return 压缩后的字节数组（≤128KB）
         */
        private suspend fun suspendingCompressByteArray(byteArray: ByteArray): ByteArray {
            // 如果超过大小，直接截取
            val maxSize = MAX_THUMB_SIZE_KB * 1024
            // 判断大小，符合要求直接返回（避免无意义的解码/压缩）
            if (byteArray.size <= maxSize) {
                return byteArray
            }
            // 把所有耗时操作（解码+压缩）都放到IO线程
            return withContext(IO) {
                // 字节数组转回Bitmap（解码失败直接抛异常，由withHandling处理）
                val bitmap = try {
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                } catch (e: Exception) {
                    throw RuntimeException("缩略图解码失败：${e.message}", e)
                } ?: throw RuntimeException("缩略图解码结果为空")
                // 质量压缩（JPEG/PNG自适应，兼容透明图）
                val compressFormat = if (bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                val baos = ByteArrayOutputStream()
                var quality = 100
                bitmap.compress(compressFormat, quality, baos)
                // 循环压缩直到符合大小（最低保留10%质量，避免过度压缩）
                while (baos.toByteArray().size > maxSize && quality > 10) {
                    quality -= 10
                    baos.reset()
                    bitmap.compress(compressFormat, quality, baos)
                }
                // 安全回收Bitmap（避免内存泄漏）
                bitmap.safeRecycle()
                // 最终校验 (极端情况仍超大小则截取)
                baos.toByteArray().let { result ->
                    if (result.size > maxSize) {
                        result.copyOf(maxSize)
                    } else {
                        result
                    }
                }
            }
        }
    }

    init {
        mActivity.doOnDestroy {
            configJob?.cancel()
            shareJob?.cancel()
            // 清空缩略图数据，避免内存泄漏
            mThumbByte = null
            mShareMessage = null
        }
    }

    /**
     * 设置分享基础信息 : 自动配置 (生成缩略图 + 执行分享回调)
     * @mView 加载框控制
     * @message 分享基础信息
     * @bitmap 原始图片（可为空，为空则用默认图）
     * @needRecycle 是否强制重置已有缩略图
     * @block 配置完成后的分享回调
     */
    fun config(mView: BaseView? = null, message: WXShareMessage? = null, bitmap: Bitmap? = null, needRecycle: Boolean = false, block: (builder: WXShare) -> Unit = {}) {
        mShareMessage = message ?: WXShareMessage()
        // 获取分享消息体的左侧图标
        if (needRecycle) mThumbByte = null
        val targetBmp = if (mThumbByte != null) {
            // 已有有效缩略图，无需重新生成，直接走回调
            null
        } else {
            // 外部传入了bitmap，优先用 / 无外部图 + 无有效缩略图 → 加载默认图
            bitmap ?: mActivity.decodeResource(R.mipmap.ic_share)
        }
        if (targetBmp != null) {
            configJob?.cancel()
            configJob = mScope.launch(Main.immediate) {
                flow {
                    emit(requestAffair { suspendingBuildThumb(targetBmp) })
                }.withHandling(mView, end = {
                    block.invoke(this@WXShare)
                }, isShowToast = true).collect { thumbByte ->
                    mThumbByte = thumbByte
                }
            }
        } else {
            block.invoke(this)
        }
    }

    /**
     * 设置分享基础信息 : 手动配置（直接传入预生成的缩略图，无需异步处理）
     * @message -> 分享基础信息
     * @thumbByte -> 预生成的缩略图字节数组
     */
    fun config(message: WXShareMessage, thumbByte: ByteArray) {
        mShareMessage = message
        mThumbByte = thumbByte
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
     * 小程序类型分享 (小程序目前只支持会话->SendMessageToWX.Req.WXSceneSession)
     * val miniProgramObj = WXMiniProgramObject()
     * miniProgramObj.webpageUrl = result?.webpageUrl // 兼容低版本的网页链接
     * miniProgramObj.miniprogramType = result?.miniprogramType.orZero // 正式版:0，测试版:1，体验版:2
     * miniProgramObj.userName = result?.userName //小程序原始id
     * miniProgramObj.path = result?.path // 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     *
     * val msg = WXMediaMessage(miniProgramObj)
     * msg.title = message?.title // 小程序消息title
     * msg.description = message?.description // 小程序消息desc
     * msg.thumbData = thumbData // 小程序消息封面图片，小于128k
     */
    fun shareMiniProgram(webpageUrl: String, miniprogramType: Int, userName: String, path: String) {
        // 小程序目前只支持会话
        share(WXMiniProgramObject().also {
            it.webpageUrl = webpageUrl // 兼容低版本的网页链接
            it.miniprogramType = miniprogramType // 正式版:0，测试版:1，体验版:2
            it.userName = userName // 小程序原始id
            it.path = path // 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
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
     * msg.thumbData = thumbData // 音乐卡片缩略图，不超过64KB
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
        // 未安装
        if (!wxApi?.isWXAppInstalled.orFalse) {
            R.string.wechatUnInstalled.shortToast()
            return
        }
        // 版本不支持
        val isWxSupport = try {
            wxApi?.wxAppSupportAPI != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        if (!isWxSupport) {
            R.string.wechatSupportError.shortToast()
            return
        }
        // 发起分享
        val message = WXMediaMessage(mediaObject).apply {
            mShareMessage?.let {
                title = it.title
                description = it.description
                thumbData = mThumbByte
                messageExt = it.messageExt
            }
        }
        shareJob?.cancel()
        shareJob = mScope.launch(Main.immediate) {
            try {
                wxApi?.sendReq(SendMessageToWX.Req().also {
                    it.transaction = buildTransaction(transaction)
                    it.message = message
                    it.scene = scene
                })
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