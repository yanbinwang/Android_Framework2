package com.example.mvvm.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.common.network.repository.successful
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.findAndRemove
import com.example.framework.utils.function.value.orZero
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LogExecutors(lifecycleOwner: LifecycleOwner) : CoroutineScope, LifecycleEventObserver {
    private var lastRefreshTime = 0L
    private var postJob: Job? = null
    private var logId = 0
        get() = ++field
    private val list by lazy { ArrayList<ServerLog>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        lifecycleOwner.doOnDestroy { job.cancel() }
    }

    /**
     * 每次操作主动发起提交
     * 间隔小于10秒不做刷新
     */
    fun post(type: Int?) {
        list.add(ServerLog(logId, type))
        if (currentTimeNano - lastRefreshTime < 10000L) return
        lastRefreshTime = currentTimeNano
        post()
    }

    private fun post() {
        postJob?.cancel()
        postJob = launch {
            //用于记录所有的id
            val ids = ArrayList<Int>()
            //串行发起提交
            list.forEach {
                if (logAsync(it).await().successful()) {
                    ids.add(it.id.orZero)
                }
            }
            //删除成功的日志
            ids.forEach { id -> list.findAndRemove { it.id == id } }
        }
    }

    /**
     * 模拟发起一个日志提交
     */
    private suspend fun logAsync(bean: ServerLog): Deferred<ApiResponse<EmptyBean>> {
        return async { CommonSubscribe.getTestApi(mapOf("id" to bean.id.toString(), "type" to bean.type.toString())) }
    }

    /**
     * 回到主页时也会触发一次上传（无10s间隔）
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> post()
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> {}
        }
    }

    /**
     * 提交参数类
     */
    data class ServerLog(
        var id: Int? = null,
        var type: Int? = null
    )

}