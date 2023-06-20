package com.example.framework.utils.function.value

import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.set
import kotlin.random.Random

//------------------------------------全局用自定义方法 List部分------------------------------------
/**
 * 安全的List.size
 */
val <T : Collection<*>> T?.safeSize: Int
    get() {
        return this?.size.orZero
    }

/**
 * 安全的Map.size
 */
val <T : Map<*, *>> T?.safeSize: Int
    get() {
        return this?.size.orZero
    }

/**
 * 安全获取值
 */
fun <T : List<K>, K> T?.safeGet(position: Int): K? {
    return when {
        isNullOrEmpty() -> null
        position in indices -> get(position)
        else -> null
    }
}

fun <T : MutableList<K>, K> T?.safeSet(position: Int, value: K) {
    this ?: return
    if (position in indices) try {
        set(position, value)
    } catch (_: Exception) {
    }
}

/**
 * 安全获取制定长度的集合
 */
fun <T> List<T>?.safeSubList(from: Int, to: Int): List<T> {
    if (this == null) return emptyList()
    if (from !in indices) return emptyList()
    if (to < from) return emptyList()
    return try {
        if (to > size) subList(from, size) else subList(from, to)
    } catch (e: Exception) {
        emptyList()
    }
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
 * 设置最后一个item的值，报错不处理
 */
fun <T> MutableList<T>?.setSafeLast(t: T) {
    if (isNullOrEmpty()) return
    try {
        this[lastIndex] = t
    } catch (e: Exception) {
        null
    }
}

/**
 * 将旧list转换为新list
 */
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
 * 将旧list转换为新list
 */
fun <T, K> ArrayList<T>?.toNewList(func: (T) -> K?): ArrayList<K> {
    return (this as? List<T>).toNewList(func)
}

/**
 * 将旧list转换为新list
 */
fun <T, K> Array<T>?.toNewList(func: (T) -> K): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        list.add(func(it))
    }
    return list
}

/**
 * 将旧list转换为新list
 */
fun <K> IntArray?.toNewList(func: (Int) -> K): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        list.add(func(it))
    }
    return list
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
 * 将Map转换为Array
 * val map = mapOf("1111" to "一", "2222" to "二", "3333" to "三")
 * map.toArray { it.key to it.value }
 * [{"first":"1111","second":"一"},{"first":"2222","second":"二"},{"first":"3333","second":"三"}]
 * ----------------------------------------
 *  class TestBean(
 *      var key: String? = null,
 *      var value: String? = null
 *  )
 *  val map = mapOf("1111" to "一", "2222" to "二", "3333" to "三")
 *  map.toArray { TestBean(it.key, it.value) }
 *  [{"key":"1111","value":"一"},{"key":"2222","value":"二"},{"key":"3333","value":"三"}]
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

fun <T>ArrayList<T>?.toRequestParams():String{
    if (this == null) return ""
    var result = "["
    for (index in indices) {
        result = if (index + 1 == size) {
            result + safeGet(index) + "]"
        } else {
            result + safeGet(index) + "],["
        }
    }
    return result
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
 * 移除符合条件的item
 */
fun <T> MutableList<T>.findAndRemove(func: ((T) -> Boolean)) {
    try {
        remove(find { func(it) })
    } catch (_: Exception) {
    }
}

/**
 * 返回List中随机一个值
 * 抽奖/取随机
 */
val <T> List<T>?.randomItem: T?
    get() = if (isNullOrEmpty()) {
        null
    } else {
        this[Random.nextInt(0, size)]
    }

/**
 * 返回Array中随机一个值
 */
val <T> Array<T>?.randomItem: T?
    get() = if (isNullOrEmpty()) {
        null
    } else {
        this[Random.nextInt(0, size)]
    }


/**
 * 返回CharArray中随机一个值
 */
val CharArray?.randomItem: Char?
    get() = when {
        this == null -> null
        this.isEmpty() -> null
        else -> this[Random.nextInt(0, size)]
    }

/**
 * string集合合并成一个string->某些id提取出来后需要‘,’号分隔拼接成string参数传递给后端，可以使用当前方法
 * val list = listOf("1111","2222","3333")
 * list.join(",")
 * 1111,2222,3333
 */
fun List<String>?.join(separator: String): String {
    if (isNullOrEmpty()) return ""
    val sb = StringBuilder()
    forEachIndexed { index, s ->
        if (index > 0) sb.append(separator)
        sb.append(s)
    }
    return sb.toString()
}

/**
 *  val list = listOf("1" to true, "2" to true, "3" to true)
 *  部分接口参数需要id逗号拼接或者特殊符号拼接，可以使用当前方式提取出其中选中的值
 */
fun List<Pair<String, Boolean>>?.joinSelect(separator: String): String {
    if (isNullOrEmpty()) return ""
    return filter { it.second }.toNewList { it.first }.join(separator)
}

/**
 * 集合转jsonarray
 */
fun List<*>?.toJsonArray(): JSONArray? {
    this ?: return null
    return JSONArray(this)
}

/**
 * pair转jsonobject
 */
fun jsonOf(vararg pairs: Pair<String, Any?>?): JSONObject {
    val json = JSONObject()
    pairs.forEach {
        if (it?.first != null && it.second != null) {
            it.second.apply {
                when (this) {
                    is List<*> -> json.put(it.first, toJsonArray())
                    is Array<*> -> json.put(it.first, toList().toJsonArray())
                    else -> json.put(it.first, this)
                }
            }
        }
    }
    return json
}