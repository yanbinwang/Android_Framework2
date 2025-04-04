package com.example.framework.utils.function.value

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
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
    } catch (e: Exception) {
        e.printStackTrace()
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

/**
 * 将Collection转换为Bundle
 */
@Suppress("UNCHECKED_CAST")
fun <T> Collection<T>.toBundle(func: (T.() -> Pair<String, Any?>)): Bundle {
    val bundle = Bundle()
    forEach {
        val pair = it.func()
        val key = pair.first
        val value = pair.second ?: return@forEach
        when (value) {
            is Char -> bundle.putChar(key, value)
            is Byte -> bundle.putByte(key, value)
            is Bundle -> bundle.putBundle(key, value)
            is ByteArray -> bundle.putByteArray(key, value)
            is CharArray -> bundle.putCharArray(key, value)
            is CharSequence -> bundle.putCharSequence(key, value)
            is Float -> bundle.putFloat(key, value)
            is FloatArray -> bundle.putFloatArray(key, value)
            is Int -> bundle.putInt(key, value)
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            is Short -> bundle.putShort(key, value)
            is ShortArray -> bundle.putShortArray(key, value)
            is String -> bundle.putString(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is BooleanArray -> bundle.putBooleanArray(key, value)
            is Double -> bundle.putDouble(key, value)
            is DoubleArray -> bundle.putDoubleArray(key, value)
            is IntArray -> bundle.putIntArray(key, value)
            is Long -> bundle.putLong(key, value)
            is LongArray -> bundle.putLongArray(key, value)
            is SparseArray<*> -> if (value.size() != 0) when (value[0]) {
                is Parcelable -> bundle.putSparseParcelableArray(key, value as SparseArray<out Parcelable>)
            }
            is Array<*> -> if (value.isNotEmpty()) when (value[0]) {
                is CharSequence -> bundle.putCharSequenceArray(key, value as Array<out CharSequence>)
                is Parcelable -> bundle.putParcelableArray(key, value as Array<out Parcelable>)
                is String -> bundle.putStringArray(key, value as Array<out String>)
            }
            is List<*> -> if (value.isNotEmpty()) when (value[0]) {
                is CharSequence -> bundle.putCharSequenceArrayList(key, value as ArrayList<CharSequence>)
                is Int -> bundle.putIntegerArrayList(key, value as ArrayList<Int>)
                is Parcelable -> bundle.putParcelableArrayList(key, value as ArrayList<out Parcelable>)
                is String -> bundle.putStringArrayList(key, value as ArrayList<String>)
            }
        }
    }
    return bundle
}

/**
 * 将Array转换为Bundle
 */
fun <T> Array<T>.toBundle(func: (T.() -> Pair<String, Any?>)): Bundle {
    return this.toList().toBundle(func)
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
    } catch (e: Exception) {
        e.printStackTrace()
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
 * 取得async异步协程集合后，拿取对应的值强转
 * reified:保留类型参数 T 的具体类型信息
 */
inline fun <reified T> List<Any?>?.safeAs(position: Int): T? {
    if (this == null || position < 0 || position >= size) return null
    val value = get(position)
    return if (value is T) value else null
}

inline fun <reified T> Any?.safeAs(): T? {
    if (this == null) return null
    return if (this is T) this else null
}

/**
 * list1为服务器中数据
 * list2为本地存储数据
 * isRepeated:是否返回重复的或不重复的数据
 * 正向查为服务器新增数据
 * 反向查为本地删除数据
 * 需重写equals和hasCode方法
 * data class User(val id: Int, val name: String) {
 *     override fun equals(other: Any?): Boolean {
 *         if (this === other) return true
 *         if (other is User) return id == other.id && name == other.name
 *         return false
 *     }
 *
 *     override fun hashCode(): Int {
 *         return Objects.hash(id, name)
 *     }
 * }
 */
fun <T> List<T>?.extract(list: List<T>, isRepeated : Boolean = false): List<T>? {
    this ?: return null
    // 1. 生成重复集合（在两个列表中都存在的用户）
    val repeated = toSet().intersect(list.toSet())
    // 2. 生成不重复集合（只存在于一个列表中的用户）
    val allUsers = toSet().union(list.toSet())
    val unique = allUsers.subtract(repeated)
    return if (isRepeated) repeated.toList() else unique.toList()
}

/**
 * 获取一串拼接的json
 */
fun <T> ArrayList<T>?.requestParams(): String {
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
 * string集合合并成一个string->某些id提取出来后需要‘,’号分隔拼接成string参数传递给后端，可以使用当前方法
 * val list = listOf("1111","2222","3333")
 * list.join(",")
 * 1111,2222,3333
 *
 * ArrayList<String>().apply {
 * Triple(
 *     mBinding?.ckDeal?.isChecked.orFalse,
 *     mBinding?.ckTraded?.isChecked.orFalse,
 *     mBinding?.ckBlock?.isChecked.orFalse
 * ).toList().forEachIndexed { index, boolean ->
 *     if (boolean) {
 *         add((index + 1).toString())
 *     }
 * }
 * }.join(",")
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
fun List<Pair<String, Boolean>>?.joinFilter(separator: String): String {
    if (isNullOrEmpty()) return ""
    return filter { it.second }.toNewList { it.first }.join(separator)
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

/**
 * 集合转jsonarray
 */
fun List<*>?.toJsonArray(): JSONArray? {
    this ?: return null
    return JSONArray(this)
}

/**
 * pair处理（如果都不为空，则返回true）
 */
fun Pair<String?, String?>?.isNotEmpty(): Boolean {
    this ?: return false
    return !first.isNullOrEmpty() && !second.isNullOrEmpty()
}