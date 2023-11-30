package com.example.common.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * 管理App中跳转
 */
object ARouterUtil : CoroutineScope {
    private var job: Job? = null
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    fun jump(context: Context, url: String?) {
        jump(context, url?.toUri(), null)
    }

    fun jump(context: Context, uri: Uri?, title: String? = null) {
        uri?.apply {

        }
    }


//    fun toJump(
//        activity: FragmentActivity,
//        skipWay: Int,
//        id: String? = null,
//        view: BaseView? = null,
//        onResult: (Boolean) -> Unit = {}
//    ) {
//        activity.doOnDestroy {
//            view?.hideDialog()
//            job?.cancel()
//        }
//        when (skipWay) {
//            1 -> {
//                (Patterns.WEB_URL.matcher(id.orEmpty()).matches()).apply {
//                    onResult.invoke(this)
////                    if(this) JumpTo.webUrl(activity, url)
//                }
//            }
////            2 ->
//            else -> onResult.invoke(false)
//        }
//    }
//
//    fun checkUserVerify(view: BaseView? = null, onResult: (Boolean) -> Unit = {}) {
//        view?.showDialog()
//        job?.cancel()
//        job = launch {
////            request(
////                { coroutineScope() },
////                { resp(it) },
////                { err(it) },
////                {
////                    view?.hideDialog()
////                    end()
////                },
////                isShowToast
////            )
//        }
//    }

}