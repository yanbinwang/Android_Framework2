package com.example.mvvm.utils.log

import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.EmptyBean
import com.example.common.subscribe.CommonSubscribe
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.logWTF
import com.example.mvvm.utils.log.bean.ServerLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * 服务器日志提交
 */
class ServerLogProxy : CoroutineScope {
    private var pushJob: Job? = null
    private var lastRecordTime = 0L
    private var serverLogId = 0
        get() = ++field
    private val serverLogMap by lazy { ConcurrentHashMap<Int, ServerLog>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * 记录操作
     * 1.间隔小于10秒不做提交，只做操作记录
     * 2.大于10秒做一次提交
     */
    fun record(type: Int?) {
        serverLogMap[serverLogId] = ServerLog(1, type)
        if (currentTimeNano - lastRecordTime < 10000L) return
        lastRecordTime = currentTimeNano
        push()
    }

    /**
     * 提交本地操作集合
     * 页面onPause时会检测做一次提交
     */
    fun push() {
        //本地集合内已经都提交了，没必要再创建协程了
        if (serverLogMap.isEmpty()) return
        pushJob?.cancel()
        pushJob = launch {
            ArrayList<Deferred<ApiResponse<EmptyBean>>>().apply {
                serverLogMap.toList().toNewList { it.second }.forEach {
                    add(logAsync(it))
                }
            }.awaitAll()
            serverLogMap.clear()
        }
    }

    /**
     * 模拟发起一个日志提交
     */
    private suspend fun logAsync(bean: ServerLog): Deferred<ApiResponse<EmptyBean>> {
        return async { CommonSubscribe.getTestApi(mapOf("id" to bean.id.toString(), "type" to bean.type.toString())) }
    }

    /**
     * 销毁回调
     * 再次做一次判断，是否有提交的日志
     */
    fun destroy() {
        if (serverLogMap.isNotEmpty()) "存在未被提交的日志".logWTF
        pushJob?.cancel()
        job.cancel()
    }

}