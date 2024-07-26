package com.example.common.utils.manager

import android.util.Patterns
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.BaseApplication
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.Extra
import com.example.common.bean.WebBean
import com.example.common.config.ARouterPath
import com.example.common.network.repository.request
import com.example.common.subscribe.CommonSubscribe
import com.example.common.utils.builder.shortToast
import com.example.common.utils.helper.AccountHelper
import com.example.common.widget.dialog.AppDialog
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 管理App中跳转
 */
class ARouterManager : CoroutineScope {
    private var mView: BaseView? = null
    private var reqJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        private val mContext get() = BaseApplication.instance.applicationContext
        private val mDialog by lazy { AppDialog(mContext) }
        private val mARouter by lazy { ARouter.getInstance() }
        //全局跳转页面url
        const val KOL_VERIFY = "kolVerify"
    }

    /**
     * 推送页面如果需要调取跳转，系统会拉起一个透明的activity，然后做网络请求，但是透明的页面存在时间不能超过3s，不然用户会觉得界面卡顿
     * 故而这种情况下，就不去关联对应页面的job了，页面关闭后如果job不去cancel，让其在网络请求结束后跳转页面
     */
    constructor()

    /**
     * 正常页面点击某个按钮是需要订阅生命周期方便请求销毁的
     */
    constructor(observer: LifecycleOwner) {
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