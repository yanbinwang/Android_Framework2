package com.example.mvvm.utils

import android.util.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.common.network.repository.successful
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.value.currentTimeNano
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object ServerLogExecutors : CoroutineScope, LifecycleEventObserver {
    private var postJob: Job? = null
    private var lastRecordTime = 0L
    private var serverLogId = 0
        get() = ++field
    private val map by lazy { ArrayMap<Int, ServerLog>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * 记录操作，间隔小于10秒不做提交
     */
    fun record(type: Int?) {
        map[serverLogId] = ServerLog(1, type)
        if (currentTimeNano - lastRecordTime < 10000L) return
        lastRecordTime = currentTimeNano
        post()
    }

    /**
     * 提交本地操作集合
     * 可在baseactivity内调用
     */
    fun post() {
        if (map.isEmpty()) return
        postJob?.cancel()
        postJob = launch {
            val it = map.entries.iterator()
            while (it.hasNext()) {
                //串行发起提交
                val entry = it.next()
                //使用迭代器的remove()方法删除元素/删除成功的日志
                if (logAsync(entry.value).await().successful()) {
                    it.remove()
                }
            }
        }
    }

    /**
     * 模拟发起一个日志提交
     */
    private suspend fun logAsync(bean: ServerLog): Deferred<ApiResponse<EmptyBean>> {
        return coroutineScope { async { CommonSubscribe.getTestApi(mapOf("id" to bean.id.toString(), "type" to bean.type.toString())) } }
    }

    /**
     * 绑定MainActivity生命周期
     */
    fun addLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * 回到主页时也会触发一次上传（无10s间隔）
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
//            Lifecycle.Event.ON_RESUME -> post()
            Lifecycle.Event.ON_DESTROY -> {
                job.cancel()
                source.lifecycle.removeObserver(this)
            }
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