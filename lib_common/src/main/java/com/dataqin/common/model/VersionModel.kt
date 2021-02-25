package com.dataqin.common.model

/**
 *  Created by wangyanbin
 *  版本信息类
 */
class VersionModel(
    var nowInfo: VersionInfoModel? = VersionInfoModel(),//当前版本
    var newInfo: VersionInfoModel? = VersionInfoModel()//最新版本
)