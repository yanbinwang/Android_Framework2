package com.example.base.function

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable
import java.math.BigDecimal
import kotlin.collections.set
import kotlin.random.Random

//------------------------------------全局用自定义方法 List部分------------------------------------
val <T : Number> T?.orZero: T
    get() {
        return this ?: (when (this) {
            is Short? -> 0.toShort()
            is Byte? -> 0.toByte()
            is Int? -> 0
            is Long? -> 0L
            is Double? -> 0.0
            is Float? -> 0f
            is BigDecimal? -> BigDecimal.ZERO
            else -> 0
        } as T)
    }

/**
 * 安全的List.size
 * */
val <T : Collection<*>> T?.safeSize: Int
    get() {
        return this?.size.orZero
    }

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
    }
}

/**
 * 安全的Map.size
 * */
val <T : Map<*, *>> T?.safeSize: Int
    get() {
        return this?.size.orZero
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
 * 将旧list转换为新list
 * */
fun <T, K> ArrayList<T>?.toNewList(func: (T) -> K?): ArrayList<K> {
    return (this as? List<T>).toNewList(func)
}

/**
 * 将旧list转换为新list
 * */
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
 * */
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
 * */
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
 * 将Map转换为ArrayList
 * */
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
 * */
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
 * */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

/**
 * 将Bundle转换为Map
 * */
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
 * 将Collection转换为Bundle
 * */
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
 * */
fun <T> Array<T>.toBundle(func: (T.() -> Pair<String, Any?>)): Bundle {
    return this.toList().toBundle(func)
}

/**
 * 寻找符合条件的第一个item的index
 * */
fun <T> Collection<T>.findIndexOf(func: ((T) -> Boolean)): Int {
    forEachIndexed { index, t ->
        if (func(t)) return index
    }
    return -1
}

/**
 * 移除符合条件的item
 * */
fun <T> MutableList<T>.findAndRemove(func: ((T) -> Boolean)) {
    try {
        remove(find { func(it) })
    } catch (e: Exception) {
    }
}

/**
 * 寻找符合条件的第一个item的index和item自身的pair
 * */
fun <T> Collection<T>.findIndexed(func: ((T) -> Boolean)): Pair<Int, T>? {
    forEachIndexed { index, t ->
        if (func(t)) return index to t
    }
    return null
}

/**
 * 返回第一个item，无法返回则返回null
 * */
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
 * */
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
 * */
fun <T> MutableList<T>?.setSafeLast(t: T) {
    if (isNullOrEmpty()) return
    try {
        this[lastIndex] = t
    } catch (e: Exception) {
        null
    }
}

/**
 * 返回List中随机一个值
 * */
val <T> List<T>?.randomItem: T?
    get() = if (isNullOrEmpty())
        null
    else
        this[Random.nextInt(0, size)]

/**
 * 返回Array中随机一个值
 * */
val <T> Array<T>?.randomItem: T?
    get() = if (isNullOrEmpty())
        null
    else
        this[Random.nextInt(0, size)]

/**
 * 返回CharArray中随机一个值
 * */
val CharArray?.randomItem: Char?
    get() = when {
        this == null -> null
        this.isEmpty() -> null
        else -> this[Random.nextInt(0, size)]
    }

/**
 * 生成SparseArray
 * */
fun <T> sparseArrayOf(vararg pairs: Pair<Int, T>): SparseArray<T> {
    val result = SparseArray<T>(pairs.size)
    pairs.forEach {
        result.setValueAt(it.first, it.second)
    }
    return result
}