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
    var invoiceAmount: String? = null,
    var sendBalance: String? = null,//赠送金额(元)
    var status: Int? = null,//0冻结 1正常
    var userId: String? = null,//用户id
    var waitPayCount: Int? = null//等待支付订单
) : Parcelable {

//    /**
//     * 双向绑定tag
//     * : Parcelable, BaseObservable()
//     */
//    @IgnoredOnParcel
//    var tag = fileLabel
//        @Bindable
//        get
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.tag)
//        }

    /**
     * 我们需要规定两个不同的对象在部分值相同的时候让安卓虚拟机视为是相同的对象
     * 每个页面可以调取一下接口，然后用==和本地做对比
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is UserInfoBean) {
            return false
        }
        return balance == other.balance &&
                consumeBalance == other.consumeBalance &&
                consumeSendBalance == other.consumeSendBalance &&
                freeze == other.freeze &&
                freezeSendBalance == other.freezeSendBalance &&
                invoiceAmount == other.invoiceAmount &&
                sendBalance == other.sendBalance &&
                status == other.status &&
                waitPayCount == other.waitPayCount
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + balance.hashCode()
        result = 31 * result + consumeSendBalance.hashCode()
        result = 31 * result + freeze.hashCode()
        result = 31 * result + freezeSendBalance.hashCode()
        result = 31 * result + invoiceAmount.hashCode()
        result = 31 * result + sendBalance.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + waitPayCount.hashCode()
        return result
    }

}