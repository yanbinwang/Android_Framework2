package com.example.home.activity

/**
 * 处理dialog需要弹框但是页面不存在的情况,此时的needOpenHome是true
 * CHARGE_COIN：充币
 * WITHDRAW_COIN：提币
 * 1.权限不够
 * ADD_ADS：新增广告
 * 1.v1未认证
 * 2.买卖状态关闭
 * 3.未设置收款方式
 * 4.未进行实名认证
 */
//@SuppressLint("MissingSuperCall")
//@TransparentOwner
//@Route(path = ARouterPath.LinkHandlerActivity)
//class LinkHandlerActivity : BaseActivity<Nothing>() {
//    private val certified by lazy { CertifiedDialog(this) }
//
//    override fun isBindingEnabled() = false
//
//    override fun initView(savedInstanceState: Bundle?) {
//        super.initView(savedInstanceState)
//        initImmersionBar(false)
//        handleIntent(intent)
//    }
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        handleIntent(intent)
//    }
//
//    private fun handleIntent(intent: Intent?) {
//        (intent?.getParcelableExtra(Extra.BUNDLE_BEAN) as? LinkBundle).let { bean ->
//            //處理推送透傳信息
//            if (null != bean) {
//                when (bean.url) {
//                        CHARGE_COIN, WITHDRAW_COIN -> {
//                            val chargeMoneySwitch = bean.params?.getInt(Extra.ID).orZero
//                            val message = string(R.string.platformError, if (1 == chargeMoneySwitch) string(R.string.platformRecharge) else string(R.string.platformWithdraw))
//                            mDialog
//                                .setPositive(message = message, positiveText = string(R.string.iKnow))
//                                .setDialogListener({
//                                    navigationHandler()
//                                })
//                                .show()
//                        }
//                        ADD_ADS -> {
//                            when (bean.id) {
//                                "0" -> {
//                                    mDialog
//                                        .setParams(message = string(R.string.certifiedReleaseError))
//                                        .setDialogListener({ navigation(ARouterPath.CertifiedActivity)?.finish() }, { navigationHandler() })
//                                        .show()
//                                }
//                                "1" -> {
//                                    mDialog
//                                        .setPositive(message = string(R.string.businessError), positiveText = string(R.string.iKnow))
//                                        .setDialogListener({
//                                            navigationHandler()
//                                        })
//                                        .show()
//                                }
//                                "2" -> {
//                                    val hasPayWayAccount = bean.params?.getBoolean(Extra.BUNDLE_BOOLEAN).orFalse
//                                    mDialog
//                                        .setParams(message = string(R.string.payWayGoSetting))
//                                        .setDialogListener({
//                                            navigation(if (hasPayWayAccount) ARouterPath.PaymentActivity else ARouterPath.PaymentAdditionActivity, Extra.BUNDLE_BOOLEAN to true)?.finish()
//                                        })
//                                        .show()
//                                }
//                                else -> {
//                                    certified.show()
//                                }
//                            }
//                        }
//                        else -> navigationHandler()
//                    }
//            } else {
//                navigationHandler()
//            }
//        }
//    }
//
//    override fun initEvent() {
//        super.initEvent()
//        certified.setDialogListener {
//            navigationHandler()
//        }
//    }
//
//}