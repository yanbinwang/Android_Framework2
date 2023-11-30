package com.example.common.utils.builder

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
 * 跳转帮助类
 */
class ARouterBuilder(private val activity: FragmentActivity) : CoroutineScope {
    private var job: Job? = null
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    init {
        activity.doOnDestroy {
            job?.cancel()
        }
    }

    fun tryJump(onResult: (Boolean) -> Unit = {}) = activity.execute {
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
            handleDeepLink(onResult)
        } else {
            handlePush(onResult)
        }
    }

    private fun handleDeepLink(onResult: (Boolean) -> Unit = {}) = activity.execute {

    }

    private fun handlePush(onResult: (Boolean) -> Unit = {}) = activity.execute {
        // 推送来源
        val linkType = intentString("linkType").unicodeDecode()
//        val linkInfo = intentString("linkInfo").unicodeDecode()
        val businessId = intentString("businessId").unicodeDecode()
        toJump(linkType.toSafeInt(), businessId, onResult = onResult)
    }

    fun toJump(
        skipWay: Int,
        id: String? = null,
        view: BaseView? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
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