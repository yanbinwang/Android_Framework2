package com.alibaba.android.arouter.utils

import android.net.Uri
import android.text.TextUtils
import java.util.Collections

/**
 * Text utils
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/9/9 14:40
 */
object TextUtils {

    @JvmStatic
    fun isEmpty(cs: CharSequence?): Boolean {
        return cs == null || cs.isEmpty()
    }

    /**
     * Print thread stack
     */
    @JvmStatic
    fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }

    /**
     * Split query parameters
     * @param rawUri raw uri
     * @return map with params
     */
    @JvmStatic
    fun splitQueryParameters(rawUri: Uri): MutableMap<String?, String?> {
        val query = rawUri.encodedQuery
        if (query == null) {
            return mutableMapOf()
        }
        val paramMap: MutableMap<String?, String?> = LinkedHashMap()
        var start = 0
        do {
            val next = query.indexOf('&', start)
            val end = if (next == -1) query.length else next
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            val name = query.substring(start, separator)
            if (!TextUtils.isEmpty(name)) {
                val value = (if (separator == end) "" else query.substring(separator + 1, end))
                paramMap.put(Uri.decode(name), Uri.decode(value))
            }
            // Move start to end of name.
            start = end + 1
        } while (start < query.length)
        return Collections.unmodifiableMap<String?, String?>(paramMap)
    }

    /**
     * Split key with |
     *
     * @param key raw key
     * @return left key
     */
    @JvmStatic
    fun getLeft(key: String): String {
        return if (key.contains("|") && !key.endsWith("|")) {
            key.substring(0, key.indexOf("|"))
        } else {
            key
        }
    }

    /**
     * Split key with |
     *
     * @param key raw key
     * @return right key
     */
    @JvmStatic
    fun getRight(key: String): String {
        return if (key.contains("|") && !key.startsWith("|")) {
            key.substring(key.indexOf("|") + 1)
        } else {
            key
        }
    }

}