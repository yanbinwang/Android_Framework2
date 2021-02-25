package com.dataqin.common.model

/**
 * 用户信息类
 */
class UserInfoModel {
    var contactName: String? = null//联系人姓名
    var contactPhone: String? = null//联系人电话
    var userStatus: String? = null//用户状态1未实名、2审核中、3审核通过 4、审核失败
    var mobileReserve: String? = null//预留手机号
    var status: String? = null//1 个人用户  2 企业用户
    var contactIdcard: String? = null//联系人身份证号
    var company: String? = null//公司名
    var address: String? = null//地址
    var license: String? = null//营业执照
    var file: String? = null//授权文件
    var taxNumber: String? = null//企业信用识别码加密
    var taxNumberAll: String? = null//企业信用识别码未加密
    var authorName: String? = null//受托人姓名
    var legalName: String? = null//法人
    var authorPhone: String? = null//受托人电话
    var authorIdcard: String? = null//受托人身份证号
    var authorBrith: String? = null//受托人出生日期
    var authorSex: String? = null//受托人性别
    var authorFront: String? = null//身份证正面
    var authorBack: String? = null//身份证反面
    var points: String? = null//积分
    var userName: String? = null//姓名
    var authorHand: String? = null//手持身份证照片
    var accountName: String? = null//企业开户名称
    var accountBank: String? = null//企业开户银行
    var accountNo: String? = null//企业银行账号
    var authType: Int = 0//认证方式 1身份证 2运营商 3支付宝
    var userId: String? = null
}
