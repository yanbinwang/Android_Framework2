package com.example.common.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @SerializedName("last_sign_date")->服务器的字段名，改为我们定义的
 * 用户bean可以将所有的用户对象类都囊括在内，通过helper进行管理
 */
@Parcelize
data class UserBean(
    var havePassword: Boolean? = null,
    var isMain: Boolean? = null,
    var isReal: Boolean? = null,//是否实名通过，个人通过了即为true，在升级企业认证的时候也应为true
    var phoneNumber: String? = null,
    var sourceId: Int? = null,
    var token: String? = null,//登录token
    var tokenExpTime: String? = null,
    var tokenType: String? = null,
    var userId: String? = null
) : Parcelable

@Parcelize
data class UserInfoBean(
    var balance: String? = null,//余额(元)
    var consumeBalance: String? = null,//总共消耗的充值金额(元)
    var consumeSendBalance: String? = null,//总共消耗的赠送金额(元)
    var freeze: String? = null,//冻结金额(元)
    var freezeSendBalance: String? = null,//冻结赠送金额(元)
    var id: Int? = null,
    var invoiceAmount: Int? = null,
    var sendBalance: String? = null,//赠送金额(元)
    var status: Int? = null,//0冻结 1正常
    var userId: String? = null,//用户id
    var waitPayCount: Int? = null//等待支付订单
) : Parcelable

@Parcelize
data class UserAuthBean(
    var backPhoto: String? = null,//证件反面照片
    var createTime: String? = null,//时间
    var enterpriseAddress: String? = null,//企业地址
    var enterpriseCode: String? = null,//企业统一信用代码
    var enterpriseLetter: String? = null,//企业授权公函
    var enterpriseLicense: String? = null,//企业营业执照
    var enterpriseName: String? = null,//企业名称
    var frontPhoto: String? = null,//证件正面照片
    var handPhoto: String? = null,//手持证件照片
    var id: Int? = null,//id
    var idcard: String? = null,//身份证号
    var job: Int? = null,//职业（0：律师 1：法务 2：摄影师 3：自媒体作者 4：其他）
    var industry: Int? = null,//行业（0：律所 1：内容创作平台 2： 维权代理机构 3：科技金融企业  4：其他）
    var realStatus: Int? = null,//状态：1：待审核 2：审核被拒 3：审核通过
    var realType: Boolean? = null,//认证方式 （false：支付宝认证，true：证件拍照上传）
    var rejectReson: String? = null,//原因
    var sex: Boolean? = null,//性别 （false：男，true：女）
    var userId: String? = null,//userId
    var userName: String? = null,//用户姓名
    var userType: String? = null,//0:个人认证，1:企业认证
    var personalRealStatus: Int? = null,//1：待审核 2：审核被拒 3：审核通过
    var kolStatus: Int? = null,//kol认证状态 0：未申请kol审核 1：kol审核中 2：kol认证通过 3：kol认证失败
    var kolInviteUserId: Int? = null,//邀请的kol用户id
    var kolName: String? = null//kol昵称
) : Parcelable
