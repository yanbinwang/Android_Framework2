package com.example.common.utils

import android.util.Patterns
import androidx.fragment.app.FragmentActivity
import com.example.common.base.bridge.BaseView
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.intentString
import com.example.framework.utils.function.value.execute
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.unicodeDecode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 管理App中跳转
 */
object ARouterUtil : CoroutineScope {
    private var job: Job? = null
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    fun tryJump(activity: FragmentActivity, onResult: (Boolean) -> Unit = {}) = activity.execute {
        //获取intent中是否包含对应跳转的值
        if (intent.extras?.size().orZero <= 0) {
            onResult(false)
            return@execute
        }
        //查找是否是firebase的深度推送
        val isDynamicLink = intent.extras?.keySet()?.find {
            it.contains("DYNAMIC_LINK", true) || it.contains("dynamiclink", true)
        } != null
        if (isDynamicLink) {
            handleDeepLink(activity, onResult)
        } else {
            handlePush(activity, onResult)
        }
    }

    private fun handleDeepLink(activity: FragmentActivity, onResult: (Boolean) -> Unit = {}) =
        activity.execute {

        }

    private fun handlePush(activity: FragmentActivity, onResult: (Boolean) -> Unit = {}) =
        activity.execute {
            // 推送来源
            val linkType = intentString("linkType").unicodeDecode()
//        val linkInfo = intentString("linkInfo").unicodeDecode()
            val businessId = intentString("businessId").unicodeDecode()
            toJump(activity, linkType.toSafeInt(), businessId, onResult = onResult)
        }

    fun toJump(
        activity: FragmentActivity,
        skipWay: Int,
        id: String? = null,
        view: BaseView? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        activity.doOnDestroy {
            view?.hideDialog()
            job?.cancel()
        }
        when (skipWay) {
            1 -> {
                if (!Patterns.WEB_URL.matcher(id.orEmpty()).matches()) {
                    onResult.invoke(false)
                } else {
//                    JumpTo.webUrl(activity, url)
                    onResult.invoke(true)
                }
            }
//            2 ->
            else -> onResult.invoke(false)
        }
    }

    fun checkUserVerify(view: BaseView? = null, onResult: (Boolean) -> Unit = {}) {
        view?.showDialog()
        job?.cancel()
        job = launch {
//            request(
//                { coroutineScope() },
//                { resp(it) },
//                { err(it) },
//                {
//                    view?.hideDialog()
//                    end()
//                },
//                isShowToast
//            )
        }
    }

}