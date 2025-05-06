package com.example.common.utils.manager

import androidx.fragment.app.FragmentActivity
import com.example.common.base.bridge.BaseView
import com.example.common.widget.dialog.AppDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import java.lang.ref.WeakReference

/**
 * 管理App中跳转
 */
class ARouterManager(activity: FragmentActivity?, private val view: BaseView? = null) {
    private var weakActivity: WeakReference<FragmentActivity?>? = null
    //保证获取到一个兜底的activity
    private val mActivity: FragmentActivity?
        get() {
            val weakActivity = weakActivity?.get()
            if (weakActivity != null &&!weakActivity.isFinishing &&!weakActivity.isDestroyed) {
                return weakActivity
            }
            val currentActivity = AppManager.currentActivity()
            return if (currentActivity is FragmentActivity &&!currentActivity.isFinishing &&!currentActivity.isDestroyed) {
                currentActivity
            } else {
                null
            }
        }
    private val mDialog by lazy { mActivity?.let { AppDialog(it) } }

    companion object {
        //全局跳转页面url
        const val KOL_VERIFY = "kolVerify"

        /**
         * 共享的协程作用域（使用 SupervisorJob 避免子协程异常影响全局）
         */
        private val managerScope by lazy { CoroutineScope(SupervisorJob() + Main) }

        /**
         * 共享的 Job，确保任务串行执行
         */
        private var builderJob: Job? = null

        /**
         * 全局取消所有任务,MainActivity的OnDestory调取
         */
        fun cancelAll() {
            managerScope.coroutineContext.cancelChildren()
        }
    }

    init {
        this.weakActivity = WeakReference(activity)
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

}