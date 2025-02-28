package com.example.common.network.interceptor

import android.os.Debug
import com.example.framework.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.abs

/**
 * 监听网络请求内存状况拦截器
 *
 * 通过 初始值 - 最终值 计算内存净变化量，反映本次网络请求的实际内存消耗
 * 如果差值为正，表示内存泄漏或临时分配未回收。
 * 如果差值为负，可能是垃圾回收（GC）释放了内存。
 *
 * Java Heap：由 JVM 管理，常见于对象创建（如 JSON 解析、数据模型）
 * Native Heap：由操作系统直接管理，常见于 Bitmap 解码、音视频处理、第三方库（如 Glide）
 */
class MemoryTrackingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        //初始内存状态
        val initialJavaHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val initialNativeHeap = Debug.getNativeHeapAllocatedSize()
        //执行请求
        val request = chain.request()
        val response = chain.proceed(request)
        //最终内存状态
        val finalJavaHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val finalNativeHeap = Debug.getNativeHeapAllocatedSize()
        log(initialJavaHeap, initialNativeHeap, finalJavaHeap, finalNativeHeap)
        return response
    }

    /**
     * Java Heap	JVM 管理的堆内存（Java对象、字符串等）
     * initialJavaHeap	请求开始前 Java Heap 的已使用内存量（单位：KB）
     * finalJavaHeap	请求结束后 Java Heap 的已使用内存量（单位：KB）
     * initialJavaHeap - finalJavaHeap	本次请求新增的 Java Heap 内存消耗（正值表示内存增长，负值表示回收）
     * ${finalJavaHeap / 1024} MB	请求结束后的 Java Heap 总内存占用（单位：MB）
     *
     * Native Heap	Android 原生堆内存（C/C++ 对象、bitmap、JNI 数据等）
     * initialNativeHeap	请求开始前 Native Heap 的已使用内存量（单位：KB）
     * finalNativeHeap	请求结束后 Native Heap 的已使用内存量（单位：KB）
     * initialNativeHeap - finalNativeHeap	本次请求新增的 Native Heap 内存消耗（正值表示内存增长，负值表示回收）
     * ${finalNativeHeap / 1024} MB	请求结束后的 Native Heap 总内存占用（单位：MB）
     */
    private fun log(initialJavaHeap: Long, initialNativeHeap: Long, finalJavaHeap: Long, finalNativeHeap: Long) {
        val javaHeap = initialJavaHeap - finalJavaHeap
        val nativeHeap = initialNativeHeap - finalNativeHeap
        LogUtil.e("LoggingInterceptor", " " +
                "\n————————————————————————请求开始————————————————————————" +
                "\nJava Heap(JVM 管理的堆内存): ${initialJavaHeap / 1024} MB → $javaHeap KB${calculate(javaHeap)} → ${finalJavaHeap / 1024} MB" +
                "\nNative Heap(原生堆内存): ${initialNativeHeap / 1024} MB → $nativeHeap KB${calculate(nativeHeap)}→ ${finalNativeHeap / 1024} MB" +
                "\n————————————————————————请求结束————————————————————————"
        )
    }

    private fun calculate(number: Long): String {
        var value = "内存不增不减"
        if (number > 0) {
            value = "内存增长${abs(number) / 1024}"
        } else if (number < 0) {
            value = "内存回收${abs(number) / 1024}"
        }
        return "(${value} MB)"
    }

}