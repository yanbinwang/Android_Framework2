package com.example.thirdparty.share.wechat.bean

import android.graphics.Bitmap
import com.example.common.BaseApplication
import com.example.common.utils.function.decodeResource
import com.example.thirdparty.R
import java.lang.ref.SoftReference

/**
 * 微信分享类
 */
class WXShareResult(
    var text: String? = null,//文字分享文案
    var videoUrl: String? = null,//视频url
    var title: String? = null,//消息标题——>限制长度不超过 512Bytes
    var description: String? = null,//消息描述——>限制长度不超过 1KB
    var webpageUrl: String? = null,//消息标题——>限制长度不超过 512Bytes
    var bmp: SoftReference<Bitmap>? = null,//缩略图的二进制数据——>限制内容大小不超过 32KB
    var miniprogramType: Int? = null,//WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;// 正式版:0，测试版:1，体验版:2
    var userName: String? = null,//小程序原始id
    var path: String? = null,//小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
    var musicUrl: String? = null,//音乐url
    var musicDataUrl: String? = null,//音乐音频url
    var obj: Obj? = null//分享类型
) {
    /**
     * 分享类型枚举
     */
    enum class Obj {
        TEXT, IMAGE, VIDEO, WEB_PAGE, MINI_PROGRAM, MUSIC
    }

    /**
     * 部分分享声明方法
     */
    companion object {
        /**
         * 文字类型分享
         */
        @JvmStatic
        fun text(text: String?, title: String?): WXShareResult {
            return WXShareResult(text = text, title = title, obj = Obj.TEXT)
        }

        /**
         * 图片类型分享
         */
        @JvmStatic
        fun image(bmp: Bitmap? = BaseApplication.instance.decodeResource(R.mipmap.ic_share)): WXShareResult {
            return WXShareResult(bmp = SoftReference(bmp), obj = Obj.IMAGE)
        }

        /**
         * 视频类型分享
         */
        @JvmStatic
        fun video(videoUrl: String?, title: String?, description: String?, bmp: Bitmap? = BaseApplication.instance.decodeResource(R.mipmap.ic_share)): WXShareResult {
            return WXShareResult(videoUrl = videoUrl, title = title, description = description, bmp = SoftReference(bmp), obj = Obj.VIDEO)
        }

        /**
         * 网页类型分享
         */
        @JvmStatic
        fun webPage(webpageUrl: String?, title: String?, description: String?, bmp: Bitmap? = BaseApplication.instance.decodeResource(R.mipmap.ic_share)): WXShareResult {
            return WXShareResult(webpageUrl = webpageUrl, title = title, description = description, bmp = SoftReference(bmp), obj = Obj.WEB_PAGE)
        }

        /**
         * 小程序类型分享
         */
        @JvmStatic
        fun miniProgram(webpageUrl: String?, miniprogramType: Int?, userName: String?, path: String?, title: String?, description: String?, bmp: Bitmap? = BaseApplication.instance.decodeResource(R.mipmap.ic_share)): WXShareResult {
            return WXShareResult(webpageUrl = webpageUrl, miniprogramType = miniprogramType, userName = userName, path = path, title = title, description = description, bmp = SoftReference(bmp), obj = Obj.MINI_PROGRAM)
        }

        /**
         * 音乐视频类型分享
         */
        @JvmStatic
        fun music(musicUrl: String?, musicDataUrl: String?, title: String?, description: String?, bmp: Bitmap? = BaseApplication.instance.decodeResource(R.mipmap.ic_share)): WXShareResult {
            return WXShareResult(musicUrl = musicUrl, musicDataUrl = musicDataUrl, title = title, description = description, bmp = SoftReference(bmp), obj = Obj.MUSIC)
        }
    }

}