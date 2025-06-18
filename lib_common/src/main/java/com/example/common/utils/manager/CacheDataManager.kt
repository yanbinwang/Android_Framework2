package com.example.common.utils.manager

import com.example.common.utils.DataStringCache
import com.example.common.utils.helper.ConfigHelper.getAppVersionCode
import com.example.common.utils.toList
import com.example.common.utils.toObj
import com.example.framework.utils.function.value.toSafeLong
import java.util.concurrent.ConcurrentHashMap

/**
 * 主页的页面部分数据是需要本地缓存的
 */
object CacheDataManager {
    /**
     * 首页-广告
     */
    const val MODULE_HOME_BANNER = "module_home_banner"

    /**
     * 市场-购物车
     */
    const val MODULE_MARKET_SHOPPING_CART = "module_market_shopping_cart"

    /**
     * 所有的花村key标签
     */
    internal val moduleTag by lazy { listOf(MODULE_HOME_BANNER, MODULE_MARKET_SHOPPING_CART) }

    /**
     * 所有的缓存的map
     */
    internal val moduleBuffer by lazy { ConcurrentHashMap<String, DataStringCache>() }

    /**
     * 获取或创建缓存实例
     */
    private fun getOrCreateCache(key: String): DataStringCache {
        return moduleBuffer.getOrPut(key) { DataStringCache(key) }
    }


    /**
     * 1.传入对应的key(MODULE_HOME_BANNER,MODULE_MARKET_SHOPPING_CART)
     * 2.传入转换成string的json
     * 3.拼接当前的版本
     */
    fun set(key: String, json: String) {
        getOrCreateCache(key).set("${json}::${getAppVersionCode()}")
    }

    /**
     * 获取缓存中的对象
     */
    inline fun <reified T> getObj(key: String): T? {
        return parseCache(key) { it.toObj(T::class.java) }
    }

    /**
     * 获取缓存中的集合
     */
    inline fun <reified T> getList(key: String): List<T>? {
        return parseCache(key) { it.toList(T::class.java) }
    }

    /**
     * 解析缓存数据的公共逻辑
     */
    fun <T> parseCache(key: String, parser: (String) -> T?): T? {
        val cache = getOrCreateCache(key)
        val (json, versionCode) = cache.get()?.split("::") ?: return null
        return if (versionCode.toSafeLong() == getAppVersionCode()) {
            parser(json)
        } else {
            del()
            null
        }
    }

    /**
     * 清除所有本地缓存(每次版本更新时检测)
     */
    @Synchronized
    fun del() {
        moduleTag.forEach { getOrCreateCache(it).del() }
    }

}