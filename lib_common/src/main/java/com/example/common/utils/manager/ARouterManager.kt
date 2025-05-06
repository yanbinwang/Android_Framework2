package com.example.common.utils.manager

import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.BaseApplication
import com.example.common.base.bridge.BaseView
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 管理App中跳转
 */
class ARouterManager(observer: LifecycleOwner) : CoroutineScope {
    private var mView: BaseView? = null
    private var reqJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        private val mContext get() = BaseApplication.instance.applicationContext
//        private val mDialog by lazy { AppDialog(mContext) }
        private val mARouter by lazy { ARouter.getInstance() }
        //全局跳转页面url
        const val KOL_VERIFY = "kolVerify"
    }

    init {
        observer.doOnDestroy {
            reqJob?.cancel()
            job.cancel()
        }
    }

    fun jump(url: String, id: String? = null) {
        jump(2, url, id)
    }

    fun jump(skipWay: Int, url: String, id: String? = null) {
//        when (skipWay) {
//            1 -> {
//                if (Patterns.WEB_URL.matcher(url).matches()) {
//                    mARouter.build(ARouterPath.WebActivity).apply {
//                        withSerializable(Extra.BUNDLE_BEAN, WebBean.webUrl(id, url))
//                        navigation()
//                    }
//                }
//            }
//            2 -> {
//                when (url) {
//                    KOL_VERIFY -> getKolVerify()
//                }
//            }
//        }
    }

//    private fun getKolVerify() {
//        reqJob?.cancel()
//        reqJob = launch {
//            mView?.showDialog()
//            request({ CommonSubscribe.getUserAuthApi() }, {
//                AccountHelper.refresh(it)
//                if ("0" == it?.userType) {
//                    if (!AccountHelper.getIsReal()) {
//                        if (it.realStatus == 1) {
//                            "用户实名审核中".shortToast()
//                        } else {
//                            mDialog
//                                .setPositive("实名认证", "本次活动需实名认证后才可参与哦～", "去实名")
//                                .setDialogListener({
//                                    when (it.realStatus) {
//                                        0 -> mARouter.build(ARouterPath.PersonSelectActivity)
//                                        1 -> "用户实名审核中".shortToast()
//                                        2 -> mARouter.build(ARouterPath.PersonSelectActivity)
//                                    }
//                                })
//                                .show()
//                        }
//                    } else {
//                        when (it.kolStatus) {
//                            0, 3 -> {
//                                mDialog
//                                    .setPositive("法律KOL推广分佣", "该权限暂时仅面向法律工作者提供，提交相关资料即可获得分佣权限。", "好的")
//                                    .setDialogListener({
//                                        mARouter.build(ARouterPath.WebActivity).apply {
//                                            withSerializable(Extra.BUNDLE_BEAN, WebBean.kol())
//                                            navigation()
//                                        }
//                                    })
//                                    .show()
//                            }
//
//                            1 -> "您已申请成为KOL，认证审核中".shortToast()
//                            2 -> {
//                                mARouter.build(ARouterPath.KolActivity).apply {
//                                    withParcelable(Extra.BUNDLE_BEAN, it)
//                                    navigation()
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    mDialog
//                        .setPositive("企业KOL推广分佣", "该功能暂时仅面向个人用户开放。若您的企业有推广分佣需要，请联系客服进行合作沟通。。", "好的")
//                        .show()
//                }
//            }, end = { mView?.hideDialog() })
//        }
//    }

    /**
     * 如果不传view，做请求的时候不会有转圈弹框
     */
    fun setView(mView: BaseView?) {
        this.mView = mView
    }

}