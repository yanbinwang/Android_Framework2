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
