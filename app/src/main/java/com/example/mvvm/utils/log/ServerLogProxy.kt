package com.example.mvvm.utils.log

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.value.toNewList
import com.example.mvvm.utils.log.bean.ServerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * 服务器日志提交
 */
class ServerLogProxy(observer: LifecycleOwner) : CoroutineScope, LifecycleEventObserver {
    private var postJob: Job? = null
    private var serverLogId = 0
        get() = ++field
    private val map by lazy { ConcurrentHashMap<Int, ServerLog>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        observer.lifecycle.addObserver(this)
    }

    /**
     * 记录操作，间隔小于10秒不做提交，只做操作记录
     */
    fun record(type: Int?) {
        map[serverLogId] = ServerLog(1, type)
    }

    /**
     * 提交本地操作集合
     * 可在BaseActivity的onDestroy中调用
     */
    fun push() {
        //本地集合内已经都提交了，没必要再创建协程了
        if (map.isEmpty()) return
        postJob?.cancel()
        postJob = launch {
            ArrayList<Deferred<ApiResponse<EmptyBean>>>().apply {
                map.toList().toNewList { it.second }.forEach {
                    add(logAsync(it))
                }
            }.awaitAll()
            map.clear()
        }
    }

    /**
     * 模拟发起一个日志提交
     */
    private suspend fun logAsync(bean: ServerLog): Deferred<ApiResponse<EmptyBean>> {
        return async { CommonSubscribe.getTestApi(mapOf("id" to bean.id.toString(), "type" to bean.type.toString())) }
    }

    /**
     * 生命周期回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> push()
            Lifecycle.Event.ON_DESTROY -> {
                push()
                postJob?.cancel()
                job.cancel()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}