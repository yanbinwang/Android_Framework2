package com.example.common.utils.manager

import android.content.Intent
import android.util.Patterns
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.R
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.Extra
import com.example.common.bean.WebBean
import com.example.common.config.ARouterPath
import com.example.common.network.CommonApi
import com.example.common.network.repository.reqBodyOf
import com.example.common.network.repository.request
import com.example.common.utils.helper.AccountHelper
import com.example.common.utils.helper.AccountHelper.isLogin
import com.example.common.utils.toObj
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.value.orFalse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * 管理App中跳转
 * 如果是推送页面，这去activity里再做一次逻辑
 */
class ARouterManager(activity: FragmentActivity?, private val view: BaseView? = null) {
//    private var weakActivity: WeakReference<FragmentActivity?>? = null
//    private val mActivity: FragmentActivity? get() = weakActivity?.get()
//    private val mDialog by lazy { mActivity?.let { AppDialog(it) } }
//    private val mCertified by lazy { mActivity?.let { CertifiedDialog(it) } }
//
//    /**
//     * 充币chargecoin
//     * 提币withdrawcoin
//     * 新增广告addads
//     * 市场marketindex
//     * 邀请好友invitefriends
//     * 收款方式methodpayment
//     * 个人中心personalcenter
//     * kyc认证kycauthentication
//     * 訂單詳情dealdetail
//     * 廣告詳情adsdetails
//     * 充币详情depositdetails
//     * 提币详情withdrawaldetails
//     * 新手活动noviceactivity
//     */
//    companion object {
//        const val CHARGE_COIN = "chargecoin"
//        const val WITHDRAW_COIN = "withdrawcoin"
//        const val ADD_ADS = "addads"
//        const val MARKET_INDEX = "marketindex"
//        const val INVITE_FRIENDS = "invitefriends"
//        const val METHOD_PAYMENT = "methodpayment"
//        const val PERSONAL_CENTER = "personalcenter"
//        const val KYC_AUTHENTICATION = "kycauthentication"
//        const val DEAL_DETAIL = "dealdetail"
//        const val ADS_DETAILS = "adsdetails"
//        const val DEPOSIT_DETAILS = "depositdetails"
//        const val WITHDRAWAL_DETAILS = "withdrawaldetails"
//        const val NOVICE_ACTIVITY = "noviceactivity"
//
//        /**
//         * 共享的协程作用域（使用 SupervisorJob 避免子协程异常影响全局）
//         */
//        private val managerScope by lazy { CoroutineScope(SupervisorJob() + Main) }
//
//        /**
//         * 共享的 Job，确保任务串行执行
//         */
//        private var builderJob: Job? = null
//
//        /**
//         * 全局取消所有任务,MainActivity的OnDestory调取
//         */
//        fun cancelAll() {
//            managerScope.coroutineContext.cancelChildren()
//        }
//    }
//
//    init {
//        this.weakActivity = WeakReference(activity)
//    }
//
//
//    fun jump(url: String?, id: String? = null, isPush: Boolean = false) {
//        jump(2, url, id, isPush)
//    }
//
//    fun jump(skipWay: Int, url: String?, id: String? = null, isPush: Boolean = false) {
//        url ?: return
//        when (skipWay) {
//            1 -> {
//                if (Patterns.WEB_URL.matcher(url).matches()) {
//                    ARouter.getInstance().build(ARouterPath.WebActivity).apply {
//                        withSerializable(Extra.BUNDLE_BEAN, webUrl(url))
//                        navigation()
//                    }
//                }
//            }
//            2 -> {
//                if (!isLogin()) {
//                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
//                } else {
//                    when (url) {
//                        CHARGE_COIN -> getPlatform(0, url, id, isPush)
//                        WITHDRAW_COIN -> getPlatform(1, url, id, isPush)
//                        ADD_ADS -> getAdsPlatform(url, id, isPush)
//                        MARKET_INDEX -> EVENT_MARKET_SELECT.post()
//                        INVITE_FRIENDS -> ARouter.getInstance().build(ARouterPath.InviteFriendActivity).navigation()
//                        METHOD_PAYMENT -> ARouter.getInstance().build(ARouterPath.PaymentActivity).navigation()
//                        PERSONAL_CENTER -> ARouter.getInstance().build(ARouterPath.UserInfoActivity).navigation()
//                        KYC_AUTHENTICATION -> ARouter.getInstance().build(ARouterPath.CertifiedActivity).navigation()
//                        DEAL_DETAIL -> getDealDetail(id)
//                        ADS_DETAILS -> getAdsDetail(id)
//                        DEPOSIT_DETAILS -> getDepositDetail(id)
//                        WITHDRAWAL_DETAILS -> getWithdrawalDetail(id)
//                        NOVICE_ACTIVITY -> ARouter.getInstance().build(ARouterPath.WebActivity).withSerializable(
//                            Extra.BUNDLE_BEAN, WebBean.novice()).navigation()
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 发起任何充币/提币之前都需要获取一次权限，拿到此刻准确的值后再决定是否跳转
//     * type: 0-》充币 1-》提币
//     * currency: 币名
//     */
//    private fun getPlatform(type: Int, url: String?, currency: String?, isPush: Boolean) {
//        builderJob?.cancel()
//        builderJob = managerScope.launch {
//            flow {
//                val bean = request({ CommonApi.instance.getPlatformApi() })
//                val value = if (0 == type) bean?.chargeMoneySwitch == 1 else bean?.mentionMoneySwitch == 1
//                emit(bean to value)
//            }.withHandling(view, isShowToast = true).collect { (bean, value) ->
//                if (value) {
//                    showDialogOrLaunchMainActivity(url, currency, isPush) {
//                        val content = string(
//                            R.string.platformError, if (1 == bean?.chargeMoneySwitch) string(
//                                R.string.platformRecharge) else string(R.string.platformWithdraw))
//                        mDialog
//                            ?.setPositive(message = content, positiveText = string(R.string.iKnow))
//                            ?.setDialogListener({})
//                            ?.show()
//                    }
//                } else {
//                    val aRouterPath = if (0 == type) ARouterPath.RechargeActivity else ARouterPath.WithdrawActivity
//                    ARouter.getInstance().build(aRouterPath)
//                        .withString(Extra.ID, currency)
//                        .navigation()
//                }
//            }
//        }
//    }
//
//    private fun getAdsPlatform(url: String?, releaseJson: String?, isPush: Boolean) {
//        builderJob?.cancel()
//        builderJob = managerScope.launch {
//            flow {
//                //查询用户是否有币种
//                val coinListAsync = async { request({ CommonApi.instance.getCoinListApi() }) }
//                //刷新个人信息，保证字段的真实性
//                val userInfoAsync = async { request({ CommonApi.instance.getUserInfoApi(hashMapOf("id" to AccountHelper.getUserId())) })?.apply { AccountHelper.refresh(this) } }
//                //获取用户具备的权限
//                val platformAsync = async { request({ CommonApi.instance.getPlatformApi() }) }
//                //查詢用户所有收款方式
//                val paymentWayListAsync = async { request({ CommonApi.instance.getPaymentWayListApi(
//                    reqBodyOf()
//                ) }) }
//                //并行发起网络请求
//                emit(awaitAll(coinListAsync, userInfoAsync, platformAsync, paymentWayListAsync))
//            }.withHandling(view, isShowToast = true).collect {
//                val coinList = it.safeAs<List<CoinBean>>(0)
//                if (coinList.safeSize <= 0) {
//                    R.string.coinEmpty.shortToast()
//                } else {
//                    //处理当前用户结果
//                    if (AccountHelper.getUserLevel() > 0) {
//                        val platformBean = it.safeAs<PlatformBean>(2)
//                        if (platformBean?.openAdvertise == 1) {
//                            showDialogOrLaunchMainActivity(url, releaseJson, isPush) {
//                                mDialog
//                                    ?.setParams(message = string(R.string.certifiedReleaseError))
//                                    ?.setDialogListener({ ARouter.getInstance().build(ARouterPath.CertifiedActivity).navigation() })
//                                    ?.show()
//                            }
//                        } else {
//                            val paymentWayList = it.safeAs<List<PaymentWayBean>>(3)
//                            //确认是否具备收款方式
//                            val hasPayWayAccount = paymentWayList?.safeSize.orZero > 0
//                            //篩出其中已啟用的数量
//                            val enableNum = paymentWayList?.filter { it.enableStatus == "1" }?.safeSize.orZero
//                            //是否启用
//                            val enablePayWayAccount = enableNum > 0
//                            //如果具备收款方式并且有启用的
//                            if (hasPayWayAccount && enablePayWayAccount) {
//                                //做用户交易的权限判断
//                                if (platformBean?.c2cBuySwitch == 1 && platformBean.c2cSellSwitch == 1) {
//                                    showDialogOrLaunchMainActivity(url, releaseJson, isPush) {
//                                        mDialog
//                                            ?.setPositive(message = string(R.string.businessError), positiveText = string(
//                                                R.string.iKnow))
//                                            ?.setDialogListener({})
//                                            ?.show()
//                                    }
//                                } else {
//                                    val tempBean = releaseJson.toObj(CoinBean::class.java)
//                                    ARouter.getInstance().build(ARouterPath.AdvertiseReleaseActivity)
//                                        .withInt(Extra.ID, platformBean?.c2CType.orZero)
//                                        .withParcelable(Extra.BUNDLE_BEAN, tempBean)
//                                        .navigation()
//                                }
//                            } else {
//                                showDialogOrLaunchMainActivity(url, releaseJson, isPush) {
//                                    mDialog
//                                        ?.setParams(message = string(R.string.payWayGoSetting))
//                                        ?.setDialogListener({
//                                            ARouter.getInstance().build(if (hasPayWayAccount) ARouterPath.PaymentActivity else ARouterPath.PaymentAdditionActivity)
//                                                .withBoolean(Extra.BUNDLE_BOOLEAN, true)
//                                                .navigation()
//                                        })
//                                        ?.show()
//                                }
//                            }
//                        }
//                    } else {
//                        showDialogOrLaunchMainActivity(url, releaseJson, isPush) {
//                            mCertified?.show()
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getDealDetail(orderId: String?) {
//        ARouter.getInstance().build(ARouterPath.DealActivity).withString(Extra.ID, orderId).navigation()
//    }
//
//    private fun getAdsDetail(entrustOrderId: String?) {
//        ARouter.getInstance().build(ARouterPath.AdvertiseDetailActivity).withString(Extra.ID, entrustOrderId).navigation()
//    }
//
//    private fun getDepositDetail(id: String?) {
//        ARouter.getInstance().build(ARouterPath.FundsHistoryDetailActivity).withString(Extra.SOURCE, "0::${id}").withBoolean(
//            Extra.BUNDLE_BEAN, true).navigation()
//    }
//
//    private fun getWithdrawalDetail(id: String?) {
//        ARouter.getInstance().build(ARouterPath.FundsHistoryDetailActivity).withString(Extra.SOURCE, "1::${id}").withBoolean(
//            Extra.BUNDLE_BEAN, true).navigation()
//    }
//
//    /**
//     * 所有的弹窗都是关联到activity的生命周期的，正常情况下不需要弹框的话，都是通过arouter拉起的
//     * 需要弹窗在正常情况下，activity和view也是具备的，可以正常走流程
//     * 只有推送是需要拉起一个透明页面（存在3s）然后去走接口的，这会儿就有可能页面已经被销毁，弹框也拉不起来，
//     * 所以如果是推送并且需要弹框的情况，让首页再去做一次请求（首页new ARouterManager）类持有首页的activity
//     * 首页添加
//     * override fun onNewIntent(intent: Intent?) {
//     *         super.onNewIntent(intent)
//     *         handleIntent(intent)
//     *     }
//     *
//     *     private fun handleIntent(intent: Intent?){
//     *         intent?.getStringExtra(Extra.SOURCE)?.let { source ->
//     *             val list = source.split("::")
//     *             val url = list.safeGet(0)
//     *             val id = list.safeGet(1)
//     *             if (url != null && id != null) {
//     *                 manager.jump(url, id)
//     *             }
//     *         }
//     *     }
//     */
//    private fun showDialogOrLaunchMainActivity(url: String?, id: String? = null, isPush: Boolean = false, dialogAction: () -> Unit) {
//        if (isPush) {
//            //构建arouter跳转
//            ARouter.getInstance().build(ARouterPath.MainActivity)
//                .withFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                .withString(Extra.SOURCE, "${url.orEmpty()}::${id.orEmpty()}")
//                .navigation()
//        } else {
//            dialogAction.invoke()
//        }
//    }

}