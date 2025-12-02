package com.example.framework.utils.function.value

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
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
 * 设置最后一个item的值，报错不处理
 */
fun <T> MutableList<T>?.safeSetLast(t: T) {
    if (isNullOrEmpty()) return
    try {
        this[lastIndex] = t
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
        e.printStackTrace()
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
        e.printStackTrace()
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
        e.printStackTrace()
        null
    }
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
fun <T> MutableCollection<T>.findAndRemove(func: ((T) -> Boolean)) {
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
 * 将旧list转换为新list
 * List 示例（过滤 null 结果）
 * val strList: List<String>? = listOf("1", "2", null, "3")
 * val intList = strList.toNewList { it?.toIntOrNull() }
 * 结果：[1, 2, 3]（过滤了 null 和无法转 Int 的元素）
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

fun <T, K> ArrayList<T>?.toNewList(func: (T) -> K?): ArrayList<K> {
    return (this as? List<T>).toNewList(func)
}

fun <T, K> Array<T>?.toNewList(func: (T) -> K): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        list.add(func(it))
    }
    return list
}

fun <K> IntArray?.toNewList(func: (Int) -> K): ArrayList<K> {
    if (this == null) return arrayListOf()
    val list = arrayListOf<K>()
    forEach {
        list.add(func(it))
    }
    return list
}

/**
 * 将List转换为ArrayList
 */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

/**
 * 将Map转换为ArrayList
 * val map = mapOf(
 *     "key1" to 1,
 *     "key2" to 2,
 *     "key3" to 3
 * )
 * ----------------------------------------
 * val resultList = map.toList { entry ->
 *     entry.value * 2
 * }
 * ----------------------------------------
 * 转换逻辑：将每个 Map.Entry 的值（value）乘以 2。
 * 结果：将 [1, 2, 3] 转换为 [2, 4, 6]。
 */
fun <T, K, P> Map<P, T>?.toArrayList(func: (Map.Entry<P, T>) -> K?): ArrayList<K> {
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
    val iterator = ks.iterator()
    while (iterator.hasNext()) {
        val key = iterator.next()
        getString(key)?.let {
            map[key] = it
        }
    }
    return map
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
 * 集合转JSONArray
 */
fun <T> Collection<T>?.toJsonArray(): JSONArray? {
    if (this == null) return null
    val jsonArray = JSONArray()
    for (item in this) {
        try {
            val jsonValue = convertToJsonValue(item)
            jsonArray.put(jsonValue)
        } catch (e: Exception) {
            // 可以根据实际需求记录日志或进行其他处理
            e.printStackTrace()
        }
    }
    return jsonArray
}

private fun convertToJsonValue(item: Any?): Any? {
    return when (item) {
        null -> null
        is JSONObject -> item
        is JSONArray -> item
        is Number -> item
        is Boolean -> item
        is String -> item
        is Collection<*> -> item.toJsonArray()
        is Map<*, *> -> convertMapToJsonObject(item)
        else -> convertObjectToJsonObject(item)
    }
}

private fun convertMapToJsonObject(map: Map<*, *>): JSONObject {
    val jsonObject = JSONObject()
    for ((key, value) in map) {
        val jsonKey = key?.toString() ?: continue
        val jsonValue = convertToJsonValue(value)
        jsonObject.put(jsonKey, jsonValue)
    }
    return jsonObject
}

private fun convertObjectToJsonObject(obj: Any): JSONObject {
    val jsonObject = JSONObject()
    val fields = obj.javaClass.declaredFields
    for (field in fields) {
        field.isAccessible = true
        val fieldName = field.name
        val fieldValue = field.get(obj)
        val jsonValue = convertToJsonValue(fieldValue)
        jsonObject.put(fieldName, jsonValue)
    }
    return jsonObject
}

/**
 * 集合转JSONObject
 */
fun <T> Collection<T>?.toJsonObject(key: String): JSONObject? {
    if (this == null) return null
    val jsonObject = JSONObject()
    val jsonArray = this.toJsonArray()
    jsonObject.put(key, jsonArray)
    return jsonObject
}

/**
 * list1为服务器中数据 , list2为本地存储数据
 * isRepeated为是否返回重复的或不重复的数据 ,正向查为服务器新增数据 , 反向查为本地删除数据
 * 需重写equals和hasCode方法
 * data class User(val id: Int, val name: String) {
 *    override fun equals(other: Any?): Boolean {
 *        if (this === other) return true
 *        if (other is User) return id == other.id && name == other.name
 *        return false
 *    }
 *    override fun hashCode(): Int {
 *        return Objects.hash(id, name)
 *    }
 * }
 * val setA = setOf(1, 2, 3, 4)
 * val setB = setOf(3, 4, 5, 6)
 * intersect	交集	只保留「两个集合都有的元素」	                    A ∩ B     输出：[3, 4]（只有 3、4 是两个集合都有的）
 * union	    并集	保留「两个集合的所有元素，去重」	                A ∪ B     输出：[1, 2, 3, 4, 5, 6]（合并后去重，无重复元素）
 * subtract	    差集（相对差）	保留「当前集合有，但目标集合没有的元素」	A - B     输出：[1, 2]（A 有 1、2，B 没有）
 */
fun <T> List<T>?.toExtract(list: List<T>, isRepeated : Boolean = false): List<T>? {
    this ?: return null
    return this.let { source ->
        // 当前集合
        val sourceSet = source.toSet()
        // 目标集合
        val targetSet = list.toSet()
        // 执行筛选
        if (isRepeated) {
            // 重复元素（交集）
            sourceSet intersect targetSet
        } else {
            /**
             * 不重复元素（对称差集）
             * sourceSet union targetSet → 并集 [1,2,3,4,5,6]（所有元素去重）；
             * sourceSet intersect targetSet → 交集 [3,4]（两个集合都有的重复元素）；
             * 并集减交集 → [1,2,3,4,5,6] - [3,4] = [1,2,5,6]。
             */
            (sourceSet union targetSet) subtract (sourceSet intersect targetSet)
        }
    }.toList()
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
 * return reduce { acc, string -> "${acc}${separator}${string}" }
 * val list = listOf("a", "b", "c")
 * val result = list.joinToString(separator = ",")
 * // 结果："a,b,c"
 */
//fun List<String>?.join(separator: String): String {
//    if (isNullOrEmpty()) return ""
//    val sb = StringBuilder()
//    forEachIndexed { index, s ->
//        if (index > 0) sb.append(separator)
//        sb.append(s)
//    }
//    return sb.toString()
//}
//
/**
 *  val list = listOf("1" to true, "2" to true, "3" to true)
 *  部分接口参数需要id逗号拼接或者特殊符号拼接，可以使用当前方式提取出其中选中的值
 */
fun List<Pair<String, Boolean>>?.joinToFilter(separator: String): String {
    if (isNullOrEmpty()) return ""
//    return filter { it.second }.toNewList { it.first }.join(separator)
    return filter { it.second }.toNewList { it.first }.joinToString(separator)
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

///**
// * 获取一串拼接的json
// */
//fun <T> ArrayList<T>?.requestParams(): String {
//    if (isNullOrEmpty()) return ""
//    val builder = StringBuilder("[")
//    for (i in indices) {
//        builder.append(safeGet(i))
//        if (i < lastIndex) {
//            builder.append("],[")
//        }
//    }
//    builder.append("]")
//    return builder.toString()
//    return joinToString(separator = "],[").let { "[$it]" }
//    return fold("[") { acc, t -> "${acc}],[${t}"}.run { "${this}]" }
//}

/**
 * pair处理（如果都不为空，则返回true）
 */
fun Pair<String?, String?>?.isNotEmpty(): Boolean {
    this ?: return false
    return !first.isNullOrEmpty() && !second.isNullOrEmpty()
}