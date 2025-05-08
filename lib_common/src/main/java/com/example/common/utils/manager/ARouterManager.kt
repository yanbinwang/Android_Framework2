package com.example.common.utils.manager

import com.example.common.base.bridge.BaseView

/**
 * 全局跳转（广告/消息中心/推送/部分页面跳转）
 * view如果不需要点击有转圈动画可不传
 * 部分跳转需要网络请求，有需要动画拦截的可以传
 */
class ARouterManager(private val view: BaseView? = null) {
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
//        @JvmStatic
//        fun cancelAll() {
//            builderJob?.cancel()
//            managerScope.coroutineContext.cancelChildren()
//        }
//
//        /**
//         * 全局处理页面跳转
//         */
//        @JvmStatic
//        fun navigationHandler() {
//            //通过application获取到首页作为底座
//            val mContext = BaseApplication.instance.applicationContext
//            val homeClass = mContext.getPostcardClass(ARouterPath.MainActivity)
//            //公共首页跳转
//            val mainBuildAction = { isExist: Boolean ->
//                //确保任务栈的干净
//                if (isExist) {
//                    //存在，只保留首页，这样就不会重启首页再访问接口了
//                    AppManager.finishNotTargetActivity(homeClass)
//                } else {
//                    //不存在，关闭所有页面
//                    AppManager.finishAll()
//                }
//                //将任务栈内application的initListener()监听里BaseActivity.onFinishListener的检测复位
//                needOpenHome = false
//                //强制拉起首页
//                ARouter.getInstance().build(ARouterPath.MainActivity)
//                    //FLAG_ACTIVITY_REORDER_TO_FRONT：若首页已存在，提到栈顶而非新建实例/FLAG_ACTIVITY_NEW_TASK：确保在非 Activity 上下文（如 Service）中安全启动
//                    .withFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    .navigation()
//            }
//            //任务栈内是否存在首页,以检测按下后为准，有可能用户长期不点通知，故而每次都要有个栈内总数的判断
//            if (AppManager.isExistActivity(homeClass)) {
//                //长度只有1且任务栈内存在首页，强制拉起
//                if (AppManager.stackCount <= 1) {
//                    mainBuildAction(true)
//                } else {
//                    /**
//                     * 1.在检测的时候任务栈内具备首页且不止首页被打开，此时无需调取跳转首页方法
//                     * 2.多个页面存在，为避免被盖住页面属于三级/四级页面关闭时强制跳转到首页，做一个还原
//                     * 3.可能存在拉起弹框页面的形式，此时我们需要关闭透明的全局弹框页面
//                     */
//                    needOpenHome = false
//                    val linkHandlerClass = mContext.getPostcardClass(ARouterPath.LinkHandlerActivity)
//                    AppManager.finishTargetActivity(linkHandlerClass)
//                }
//            } else {
//                //任务栈内不存在首页
//                mainBuildAction(false)
//            }
//        }
//    }
//
//    fun jump(url: String?, id: String? = null) {
//        jump(2, url, id)
//    }
//
//    fun jump(skipWay: Int, url: String?, id: String? = null) {
//        url ?: return
//        when (skipWay) {
//            1 -> {
//                if (Patterns.WEB_URL.matcher(url).matches()) {
//                    ARouter.getInstance().build(ARouterPath.WebActivity).apply {
//                        withSerializable(Extra.BUNDLE_BEAN, webUrl(url))
//                        navigation()
//                    }
//                } else {
//                    R.string.linkError.shortToast()
//                    navigationHandler()
//                }
//            }
//            2 -> {
//                if (!isLogin()) {
//                    ARouter.getInstance().build(ARouterPath.LoginActivity).navigation()
//                } else {
//                    when (url) {
//                        CHARGE_COIN, WITHDRAW_COIN -> getPlatform(url, id)
//                        ADD_ADS -> getAdsPlatform(url, id)
//                        MARKET_INDEX -> EVENT_MARKET_SELECT.post()
//                        INVITE_FRIENDS -> ARouter.getInstance().build(ARouterPath.InviteFriendActivity).navigation()
//                        METHOD_PAYMENT -> ARouter.getInstance().build(ARouterPath.PaymentActivity).navigation()
//                        PERSONAL_CENTER -> ARouter.getInstance().build(ARouterPath.UserInfoActivity).navigation()
//                        KYC_AUTHENTICATION -> ARouter.getInstance().build(ARouterPath.CertifiedActivity).navigation()
//                        DEAL_DETAIL -> getDealDetail(id)
//                        ADS_DETAILS -> getAdsDetail(id)
//                        DEPOSIT_DETAILS -> getDepositDetail(id)
//                        WITHDRAWAL_DETAILS -> getWithdrawalDetail(id)
//                        NOVICE_ACTIVITY -> ARouter.getInstance().build(ARouterPath.WebActivity).withSerializable(Extra.BUNDLE_BEAN, WebBean.novice()).navigation()
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 发起任何充币/提币之前都需要获取一次权限，拿到此刻准确的值后再决定是否跳转
//     * currency: 币名
//     */
//    private fun getPlatform(url: String, currency: String?) {
//        builderJob?.cancel()
//        builderJob = managerScope.launch {
//            flow {
//                val bean = request({ CommonApi.instance.getPlatformApi() })
//                val value = if (url == CHARGE_COIN) bean?.chargeMoneySwitch == 1 else bean?.mentionMoneySwitch == 1
//                emit(bean to value)
//            }.withHandling(view, {
//                navigationHandler()
//            }, isShowToast = true).collect { (bean, value) ->
//                if (value) {
//                    linkHandler(url, "", Extra.ID to bean?.chargeMoneySwitch)
//                } else {
//                    val aRouterPath = if (url == CHARGE_COIN) ARouterPath.RechargeActivity else ARouterPath.WithdrawActivity
//                    ARouter.getInstance().build(aRouterPath)
//                        .withString(Extra.ID, currency)
//                        .navigation()
//                }
//            }
//        }
//    }
//
//    private fun getAdsPlatform(url: String, releaseJson: String?) {
//        builderJob?.cancel()
//        builderJob = managerScope.launch {
//            flow {
//                //查询用户是否有币种
//                val coinListAsync = async { request({ CommonApi.instance.getCoinListApi() }) }
//                //刷新个人信息，保证字段的真实性
//                val userInfoAsync = async {
//                    request({ CommonApi.instance.getUserInfoApi(hashMapOf("id" to AccountHelper.getUserId())) })?.apply {
//                        AccountHelper.refresh(this)
//                    }
//                }
//                //获取用户具备的权限
//                val platformAsync = async { request({ CommonApi.instance.getPlatformApi() }) }
//                //查詢用户所有收款方式
//                val paymentWayListAsync = async { request({ CommonApi.instance.getPaymentWayListApi(reqBodyOf()) }) }
//                //并行发起网络请求
//                emit(awaitAll(coinListAsync, userInfoAsync, platformAsync, paymentWayListAsync))
//            }.withHandling(view, {
//                navigationHandler()
//            }, isShowToast = true).collect {
//                val coinList = it.safeAs<List<CoinBean>>(0)
//                if (coinList.safeSize <= 0) {
//                    R.string.coinEmpty.shortToast()
//                    navigationHandler()
//                } else {
//                    //处理当前用户结果
//                    if (AccountHelper.getUserLevel() > 0) {
//                        val platformBean = it.safeAs<PlatformBean>(2)
//                        if (platformBean?.openAdvertise == 1) {
//                            linkHandler(url, "0")
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
//                                    linkHandler(url, "1")
//                                } else {
//                                    val tempBean = releaseJson.toObj(CoinBean::class.java)
//                                    ARouter.getInstance()
//                                        .build(ARouterPath.AdvertiseReleaseActivity)
//                                        .withInt(Extra.ID, platformBean?.c2CType.orZero)
//                                        .withParcelable(Extra.BUNDLE_BEAN, tempBean)
//                                        .navigation()
//                                }
//                            } else {
//                                linkHandler(url, "2", Extra.BUNDLE_BOOLEAN to hasPayWayAccount)
//                            }
//                        }
//                    } else {
//                        linkHandler(url, "3")
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
//        ARouter.getInstance().build(ARouterPath.FundsHistoryDetailActivity).withString(Extra.SOURCE, "0::${id}").withBoolean(Extra.BUNDLE_BEAN, true).navigation()
//    }
//
//    private fun getWithdrawalDetail(id: String?) {
//        ARouter.getInstance().build(ARouterPath.FundsHistoryDetailActivity).withString(Extra.SOURCE, "1::${id}").withBoolean(Extra.BUNDLE_BEAN, true).navigation()
//    }
//
//    /**
//     * 栈内拉起一个透明的页面用于显示弹框
//     * 1.Dialog必须关联页面的activity/context，manager所有页面都必须遵守这条系统规则
//     * 2.进到此处的逻辑都是需要弹框的（推送的消息本身应该不需要弹框而是直接拉页面，但是部分页面有进入的权限配置，所以如需弹框的也会进该逻辑）
//     *
//     * override fun onNewIntent(intent: Intent?) {
//     *         super.onNewIntent(intent)
//     *         handleIntent(intent)
//     *     }
//     *
//     * private fun handleIntent(intent: Intent?){
//     *     intent?.getStringExtra(Extra.SOURCE)?.let { source ->
//     *         val list = source.split("::")
//     *         val url = list.safeGet(0)
//     *         val id = list.safeGet(1)
//     *         if (url != null && id != null) {
//     *             manager.jump(url, id)
//     *         }
//     *     }
//     * }
//     */
//    private fun linkHandler(url: String, id: String? = null, vararg params: Pair<String, Any?>?) {
//        ARouter.getInstance().build(ARouterPath.LinkHandlerActivity)
//            .withFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
//            .withParcelable(Extra.BUNDLE_BEAN, LinkBundle(url, id, params.filterNotNull().toBundle { this }))
//            .navigation()
//    }

}