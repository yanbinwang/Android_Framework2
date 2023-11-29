package com.example.common.utils.helper

import androidx.fragment.app.FragmentActivity
import com.example.common.BaseApplication
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * 跳转帮助类
 */
object NavigationHelper : CoroutineScope {
    private val loadingDialog by lazy { LoadingDialog(BaseApplication.instance.applicationContext) }
    private var job: Job? = null
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    fun toJump(act: FragmentActivity? = null, skipWay: Int, url: String, id: String? = null) {
        act?.doOnDestroy {
            hideDialog()
            job?.cancel()
        }
        when (skipWay) {

        }
    }

    private fun showDialog() {
        loadingDialog.shown(false)
    }

    private fun hideDialog() {
        loadingDialog.hidden()
    }

}