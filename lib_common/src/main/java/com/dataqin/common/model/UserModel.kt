package com.dataqin.common.model

/**
 * 用户登录类
 */
class UserModel {
    var token: String? = null//登录token
    var status: String? = null//用户类型 0未确定 1 个人 2企业
    var userStatus: String? = null//用户状态1未实名、2审核中、3审核通过 4、审核失败
    var userId: String? = null
}
