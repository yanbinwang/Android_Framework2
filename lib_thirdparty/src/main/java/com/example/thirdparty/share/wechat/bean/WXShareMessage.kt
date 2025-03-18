package com.example.thirdparty.share.wechat.bean

/**
 * 微信分享类
 * 针对WXMediaMessage构建
 */
class WXShareMessage(
    var title: String? = null,//消息标题->限制长度不超过 512Bytes
    var description: String? = null,//消息描述->限制长度不超过 1KB
    var messageExt: String? = null//微信跳回应用时会带上
)