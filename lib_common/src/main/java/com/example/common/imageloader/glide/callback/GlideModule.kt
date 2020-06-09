package com.example.common.imageloader.glide.callback

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
import com.example.common.http.factory.OkHttpFactory
import java.io.InputStream

/**
 * author: wyb
 * date: 2017/11/15.
 */
@GlideModule
open class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        //        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20mb
        //        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        //        //使用ActivityManager获取当前设备的内存情况，如果是处于lowMemory的时候，将图片的DecodeFormat设置为 RGB_565 ，
        //        //RGB_565 和默认的 ARGB_8888 比，每个像素会少 2 个byte，这样，等于一张同样的图片，加载到内存中会少一半内存的占用
        //        //（ARGB_8888 每个像素占 4 byte）
        //        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //        if(null != activityManager){
        //            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //            activityManager.getMemoryInfo(memoryInfo);
        //            builder.setDefaultRequestOptions((new RequestOptions()).format(memoryInfo.lowMemory? DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888));
        //        }

        val calculator = MemorySizeCalculator.Builder(context).setMemoryCacheScreens(2f).build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
    }

    //禁止解析Manifest文件,提升初始化速度，避免一些潜在错误
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    //注册自定义组件
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(OkHttpFactory.instance.okHttpClient))
    }

}