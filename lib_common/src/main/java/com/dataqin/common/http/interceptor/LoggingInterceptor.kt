package com.dataqin.common.http.interceptor

import com.dataqin.base.utils.LogUtil
import com.dataqin.common.BuildConfig
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset

/**
 * author: wyb
 * date: 2019/7/9.
 */
internal class LoggingInterceptor : Interceptor {

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var queryParameter: String? = null
        var result: String? = null

        val request = chain.request()
        val headerValues = request.headers.toString()
        val url = request.url.toString()
        when {
            //不包含服务器地址的属于下载地址或图片加载地址，不做拦截
            !url.contains(BuildConfig.LOCALHOST) -> return chain.proceed(request)
            //上传文件接口文本量过大，请求参数不做拦截
            url.contains("user/uploadImg") || url.contains("evidences/saveNew")
                    || url.contains("evidences/partUpload") || url.contains("evidences/upload") -> {
                queryParameter = "文件上传"
            }
            else -> {
                val requestBody = request.body
                val hasRequestBody = requestBody != null
                if (hasRequestBody && !bodyEncoded(request.headers)) {
                    val buffer = Buffer()
                    requestBody!!.writeTo(buffer)

                    var charset: Charset? = UTF8
                    val contentType = requestBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(UTF8)
                    }

                    if (isPlaintext(buffer)) {
                        queryParameter = buffer.readString(charset!!)
                    }
                }
            }
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        if (response.promisesBody() && !bodyEncoded(response.headers)) {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer

            var charset: Charset? = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            if (!isPlaintext(buffer)) {
                interceptLogging(headerValues, url, queryParameter, null)
                return response
            }

            if (contentLength != 0L) {
                result = buffer.clone().readString(charset!!)
            }
        }

        interceptLogging(headerValues, url, queryParameter, result)
        return response
//        var queryParameter: String? = null
//        var result: String? = null
//
//        val request = chain.request()
//        val headerValues = request.headers.toString()
//        val url = request.url.toString()
//        //上传文件接口文本量过大，不做拦截
//        if (url.contains("user/uploadImg") ||
//            url.contains("evidences/saveNew") ||
//            url.contains("evidences/partUpload") ||
//            url.contains("evidences/upload") ||
//            !url.contains(BuildConfig.LOCALHOST)) {
//            return chain.proceed(request)
//        }
//
//        val requestBody = request.body
//        val hasRequestBody = requestBody != null
//        if (hasRequestBody && !bodyEncoded(request.headers)) {
//            val buffer = Buffer()
//            requestBody!!.writeTo(buffer)
//
//            var charset: Charset? = UTF8
//            val contentType = requestBody.contentType()
//            if (contentType != null) {
//                charset = contentType.charset(UTF8)
//            }
//
//            if (isPlaintext(buffer)) {
//                queryParameter = buffer.readString(charset!!)
//            }
//        }
//
//        val response: Response
//        try {
//            response = chain.proceed(request)
//        } catch (e: Exception) {
//            throw e
//        }
//
//        val responseBody = response.body
//        val contentLength = responseBody!!.contentLength()
//        if (response.promisesBody() && !bodyEncoded(response.headers)) {
//            val source = responseBody.source()
//            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
//            val buffer = source.buffer
//
//            var charset: Charset? = UTF8
//            val contentType = responseBody.contentType()
//            if (contentType != null) {
//                charset = contentType.charset(UTF8)
//            }
//
//            if (!isPlaintext(buffer)) {
//                interceptLogging(headerValues, url, queryParameter, null)
//                return response
//            }
//
//            if (contentLength != 0L) {
//                result = buffer.clone().readString(charset!!)
//            }
//        }
//
//        interceptLogging(headerValues, url, queryParameter, result)
//        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun interceptLogging(headerValues: String, url: String, queryParameter: String?, result: String?) {
        LogUtil.e("LoggingInterceptor", " " +
                "\n————————————————————————请求开始————————————————————————" +
                "\n请求头:\n" + headerValues.trim { it <= ' ' } +
                "\n请求地址:\n" + url +
                "\n请求参数:\n" + queryParameter +
                "\n返回参数:\n" + decode(result) +
                "\n————————————————————————请求结束————————————————————————")
    }

    private fun decode(unicodeStr: String?): String {
        if (unicodeStr == null) {
            return ""
        }
        val retBuf = StringBuffer()
        val maxLoop = unicodeStr.length
        var i = 0
        while (i < maxLoop) {
            if (unicodeStr.get(i) === '\\') {
                if (i < maxLoop - 5 && (unicodeStr.get(i + 1) === 'u' || unicodeStr.get(i + 1) === 'U')) try {
                    retBuf.append(Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16).toChar())
                    i += 5
                } catch (localNumberFormatException: NumberFormatException) {
                    retBuf.append(unicodeStr.get(i))
                }
                else retBuf.append(unicodeStr.get(i))
            } else {
                retBuf.append(unicodeStr.get(i))
            }
            i++
        }
        return retBuf.toString()
    }

}