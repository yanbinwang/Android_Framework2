package com.example.common.utils.manager

import com.example.common.utils.DataLongCache
import com.example.common.utils.DataStringCache
import com.example.common.utils.helper.ConfigHelper.getAppVersionCode
import com.example.common.utils.toList
import com.example.common.utils.toObj
import java.util.concurrent.ConcurrentHashMap

/**
 * 主页的页面部分数据是需要本地缓存的
 */
object CacheDataManager {
    /**
     * 首页-广告
     */
    const val KEY_HOME_BANNER = "home_banner"

    /**
     * 市场-购物车
     */
    const val KEY_MARKET_SHOPPING_CART = "market_shopping_cart"

    /**
     * 历史版本号
     */
    internal val versionCode by lazy { DataLongCache("version_code") }

    /**
     * 所有的缓存的map
     */
    internal val moduleBuffer by lazy { ConcurrentHashMap<String, DataStringCache>() }

    /**
     * application里调用
     */
    fun init() {
        versionCode.apply {
            val mVersionCode = getAppVersionCode()
            if (get() != mVersionCode) {
                //清除所有本地缓存(每次版本更新时检测)
                getAllCacheKeys().forEach { getOrCreateCache(it).del() }
                set(mVersionCode)
            }
        }
    }

    /**
     * 获取所有缓存键
     */
    private fun getAllCacheKeys(): List<String> {
        return listOf(KEY_HOME_BANNER, KEY_MARKET_SHOPPING_CART)
    }

    /**
     * 1.传入对应的key(MODULE_HOME_BANNER,MODULE_MARKET_SHOPPING_CART)
     * 2.传入转换成string的json
     */
    fun set(key: String, json: String) {
        getOrCreateCache(key).set(json)
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
        val json = cache.get() ?: return null
        return parser(json)
    }

    /**
     * 获取或创建缓存实例
     */
    private fun getOrCreateCache(key: String): DataStringCache {
        return moduleBuffer.getOrPut(key) { DataStringCache(key) }
    }

}