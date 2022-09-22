package com.example.base.utils.function

import android.os.Bundle
import org.json.JSONArray
import kotlin.collections.set

//------------------------------------全局用自定义方法 List部分------------------------------------
/**
 * 将Map转换为ArrayList
 */
fun <T, K, P> Map<P, T>?.toList(func: (Map.Entry<P, T>) -> K?): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        func(it)?.apply {
            list.add(this)
        }
    }
    return list
}

/**
 * 将旧list转换为新list
 * */
fun <T, K> List<T>?.toNewList(func: (T) -> K?): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        func(it)?.let { result ->
            list.add(result)
        }
    }
    return list
}

/**
 * 安全获取制定长度的集合
 */
fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> {
    if (from !in indices) return emptyList()
    if (to < from) return emptyList()
    return try {
        if (to > size) subList(from, size) else subList(from, to)
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * 将Map转换为Array
 */
inline fun <T, reified K, P> Map<P, T>?.toArray(func: (Map.Entry<P, T>) -> K?): Array<K> {
    if (this == null) return arrayOf()
    val list = arrayListOf<K>()
    forEach {
        func(it)?.apply {
            list.add(this)
        }
    }
    return list.toTypedArray()
}

/**
 * 将List转换为ArrayList
 */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

/**
 * 将Bundle转换为Map
 */
fun Bundle?.toMap(): Map<String, String> {
    this ?: return mapOf()
    val map = HashMap<String, String>()
    val ks = keySet()
    val iterator: Iterator<String> = ks.iterator()
    while (iterator.hasNext()) {
        val key = iterator.next()
        getString(key)?.let {
            map[key] = it
        }
    }
    return map
}

/**
 * 将Collection转换为Map
 */
fun <T, K> Collection<T>?.toMap(func: (T) -> Pair<String, K>?): HashMap<String, K> {
    if (this == null) return hashMapOf()
    val map = hashMapOf<String, K>()
    forEach {
        func(it)?.apply {
            map[first] = second
        }
    }
    return map
}

/**
 * 寻找符合条件的第一个item的index
 */
fun <T> Collection<T>.findIndexOf(func: ((T) -> Boolean)): Int {
    forEachIndexed { index, t ->
        if (func(t)) return index
    }
    return -1
}

/**
 * 寻找符合条件的第一个item的index和item自身的pair
 */
fun <T> Collection<T>.findIndexed(func: ((T) -> Boolean)): Pair<Int, T>? {
    forEachIndexed { index, t ->
        if (func(t)) return index to t
    }
    return null
}

/**
 * 返回第一个item，无法返回则返回null
 */
fun <T> Collection<T>?.safeFirst(): T? {
    if (isNullOrEmpty()) return null
    return try {
        first()
    } catch (e: Exception) {
        null
    }
}

/**
 * 移除符合条件的item
 */
fun <T> MutableList<T>.findAndRemove(func: ((T) -> Boolean)) {
    try {
        remove(find { func(it) })
    } catch (e: Exception) {
    }
}

/**
 * 返回最后一个item，无法返回则返回null
 */
fun <T> List<T>?.safeLast(): T? {
    if (isNullOrEmpty()) return null
    return try {
        get(size - 1)
    } catch (e: Exception) {
        null
    }
}

/**
 * 返回最后一个item，无法返回则返回null
 */
fun <T> MutableList<T>?.setSafeLast(t: T) {
    if (isNullOrEmpty()) return
    try {
        this[lastIndex] = t
    } catch (e: Exception) {
        null
    }
}

fun List<*>?.toJsonArray(): JSONArray? {
    this ?: return null
    return JSONArray(this)
}