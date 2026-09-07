package com.example.framework.utils

import android.util.Log
import com.example.framework.utils.function.value.isDebug

/**
 * 日志输出类
 * 1) isDebug = false，全部日志直接丢弃，不输出
 * 2) 文本超过4000字符自动分片，携带分片标记 (1/n)，规避Android系统日志截断
 * 3) Throwable 对象会完整输出全部堆栈信息，复用同一套分片逻辑
 * 4) logA(ASSERT) 优先级最高，仅用于断言失败，普通业务异常请使用 logE
 */
private const val TAG = "yan" // 默认的 tag
private const val MAX_LOG_LENGTH = 4000

private fun logIfDebug(tag: String, level: Int, message: String) {
    if (!isDebug) return
    if (message.length <= MAX_LOG_LENGTH) {
        Log.println(level, tag, message)
    } else {
        val chunks = message.chunked(MAX_LOG_LENGTH)
        chunks.forEachIndexed { index, chunk ->
            val prefix = if (chunks.size > 1) "(${index + 1}/${chunks.size}) " else ""
            Log.println(level, tag, "$prefix$chunk")
        }
    }
}

/**
 * Verbose级别日志，常量值 = 2
 * 用于输出最详细调试信息；会输出大量日志，生产环境建议关闭
 */
fun String?.logV(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.VERBOSE, this)
}

/**
 * Debug级别日志，常量值 = 3
 * 用于普通调试打印，开发阶段查看业务流程、中间变量
 */
fun String?.logD(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.DEBUG, this)
}

/**
 * Info级别日志，常量值 = 4
 * 输出普通业务流程关键信息，例如页面跳转、接口正常完成
 */
fun String?.logI(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.INFO, this)
}

/**
 * Warn级别日志，常量值 = 5
 * 警告信息，发生非致命异常、不影响主流程的异常情况
 */
fun String?.logW(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.WARN, this)
}

/**
 * Error级别日志，常量值 = 6
 * 错误日志，业务发生异常、接口报错，绝大多数异常捕获使用该级别
 */
fun String?.logE(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.ERROR, this)
}

/**
 * Assert级别日志，常量值 = 7，日志优先级最高
 * 只用于断言失败场景：代码逻辑理论上绝不应该走到的分支；普通业务异常不要使用，请优先使用 logE
 */
fun String?.logA(tag: String = TAG) {
    this ?: return
    logIfDebug(tag, Log.ASSERT, this)
}

/**
 * 打印异常堆栈
 * 绝大多数catch捕获异常统一使用该方法；会拼接【备注标题】+完整堆栈字符串，自动走超长分片逻辑
 * @param tag 日志标签，默认 yan
 * @param msg 业务备注标题，附加在堆栈上方，可以为null
 */
fun Throwable?.logE(tag: String = TAG, msg: String? = null) {
    this ?: return
    if (!isDebug) return
    val output = buildString {
        msg?.let {
            appendLine(it)
        }
        append(stackTraceToString())
    }
    output.logE(tag)
}

/**
 * 断言异常堆栈，仅限断言失败场景：理论绝对不会出现的异常；普通业务异常请调用 logE，不要调用本函数
 * @param tag 日志标签，默认 yan
 * @param msg 业务备注标题，附加在堆栈上方，可以为 null
 */
fun Throwable?.logA(tag: String = TAG, msg: String? = null) {
    this ?: return
    if (!isDebug) return
    val output = buildString {
        msg?.let {
            appendLine(it)
        }
        append(stackTraceToString())
    }
    output.logA(tag)
}