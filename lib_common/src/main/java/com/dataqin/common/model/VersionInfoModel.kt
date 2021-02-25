package com.dataqin.common.model

/**
 *  Created by wangyanbin
 *  版本信息类
 */
class VersionInfoModel(
    var id: String? = null,
    var update_time: String? = null,//更新时间
    var current_version: String? = null,//当前版本
    var upper_version: String? = null,//上一个版本
    var update_content: String? = null,//更新内容
    var update_desc: String? = null,//更新描述
    var address: String? = null,//下载地址
    var create_time: String? = null,
)