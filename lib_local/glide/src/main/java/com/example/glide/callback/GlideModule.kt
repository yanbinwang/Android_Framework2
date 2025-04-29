package com.example.glide.callback

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.example.glide.callback.progress.ProgressInterceptor
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * author: wyb
 * date: 2017/11/15.
 */
@GlideModule
open class GlideModule : AppGlideModule() {
    //加载图片不能做拦截，重新声明请求类
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)//设置连接超时
                .writeTimeout(2, TimeUnit.HOURS)//设置写超时
                .readTimeout(2, TimeUnit.HOURS)//设置读超时
                .retryOnConnectionFailure(true)
                .addInterceptor(ProgressInterceptor())//拦截下请求，监听加载进度
                .build()
    }

    /**
     * MemorySizeCalculator类主要关注设备的内存类型，设备 RAM 大小，以及屏幕分辨率
     * 设置内存缓存应能容纳的屏幕数量。这里设置为 2f 意味着内存缓存的大小要能够容纳大约 2 个屏幕大小的图片数据
     *
     * LruResourceCache 是 Glide 实现的一个基于 LRU（Least Recently Used，最近最少使用）算法的内存缓存。LRU 算法会优先移除最近最少使用的元素，以此保证缓存空间的有效利用
     * calculator.memoryCacheSize.toLong() 获取 MemorySizeCalculator 计算得出的内存缓存大小，并将其转换为 Long 类型
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        val calculator = MemorySizeCalculator.Builder(context).setMemoryCacheScreens(2f).build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
    }

    /**
     * 禁止解析Manifest文件,提升初始化速度，避免一些潜在错误
     */
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    /**
     * 注册自定义组件
     */
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
    }

}