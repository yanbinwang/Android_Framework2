package com.example.thirdparty.share.wechat.bean

/**
 * 微信分享类
 * 针对WXMediaMessage构建
 */
class WXShareMessage(
    var title: String? = null,//消息标题->限制长度不超过 512Bytes
    var description: String? = null,//消息描述->限制长度不超过 1KB
    var thumbData: ByteArray? = null,//缩略图的二进制数据->限制内容大小不超过 32KB 100*100（BaseApplication.instance.decodeResource(R.mipmap.ic_share)）默认取本地的，如果是动态的需要调用buildThumb生成然后set进WXShareMessage
    var messageExt: String? = null,//微信跳回应用时会带上
)