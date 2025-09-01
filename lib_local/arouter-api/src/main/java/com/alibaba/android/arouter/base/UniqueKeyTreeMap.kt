package com.alibaba.android.arouter.base

import java.util.TreeMap

/**
 * TreeMap with unique key.
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/22 下午5:01
 */
class UniqueKeyTreeMap<K, V>(private val tipText: String) : TreeMap<K, V>() {

    override fun put(key: K, value: V): V? {
        if (containsKey(key)) {
            throw RuntimeException(String.format(tipText, key))
        } else {
            return super.put(key, value)
        }
    }

}