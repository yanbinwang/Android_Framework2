package com.example.common.http.interceptor

import com.example.common.BuildConfig
import com.example.framework.utils.LogUtil
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset

/**
 * author: wyb
 * date: 2019/7/9.
 */
internal class LoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var queryParameter: String? = null
        var result: String? = null
        val headerValues: String

        val request = chain.request()
        headerValues = request.headers().toString()
        //不包含User-Agent不是公司的请求链接，不做拦截
        if (!headerValues.contains("User-Agent")) {
            return chain.proceed(request)
        }

        val requestBody = request.body()
        val hasRequestBody = requestBody != null

        if (hasRequestBody && !bodyEncoded(request.headers())) {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)

            var charset: Charset? =
                UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            if (isPlaintext(buffer)) {
                queryParameter = buffer.readString(charset!!)
            }
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }

        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()

        if (HttpHeaders.hasBody(response) && !bodyEncoded(response.headers())) {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            var charset: Charset? =
                UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            if (!isPlaintext(buffer)) {
                interceptLogging(headerValues, queryParameter, null)
                return response
            }

            if (contentLength != 0L) {
                result = buffer.clone().readString(charset!!)
            }
        }

        interceptLogging(headerValues, queryParameter, result)
        return response
    }

    private fun interceptLogging(headerValues: String, queryParameter: String?, result: String?) {
        LogUtil.e("LoggingInterceptor", " " +
                "\n————————————————————————请求开始————————————————————————" +
                "\n请求头:\n" + headerValues.trim { it <= ' ' } +
                "\n请求地址:\n" + BuildConfig.LOCALHOST +
                "\n请求参数:\n" + queryParameter +
                "\n返回参数:\n" + result +
                "\n————————————————————————请求结束————————————————————————")
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
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

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

}
