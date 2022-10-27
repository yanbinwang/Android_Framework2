package com.example.common.http.interceptor

import com.example.base.utils.LogUtil
import com.example.common.BuildConfig
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
    private val UTF8 by lazy { Charset.forName("UTF-8") }
    private val dressingUrl = arrayOf("user/uploadImg")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //声明请求参数和返回参数
        var queryParams: String? = null
        var responseResult: String? = null
        //获取此次请求头部参数和请求地址
        val request = chain.request()
        val headerValues = request.headers.toString()
        val requestUrl = request.url.toString()
        //------对此次请求做处理------
        //不包含服务器地址的属于下载地址或图片加载地址，不做拦截
        if (!requestUrl.contains(BuildConfig.LOCALHOST)) return chain.proceed(request)
        //上传文件接口文本量过大，请求参数不做拦截
        if (dressingUrl.any { requestUrl.contains(it) }) {
            queryParams = "文件上传"
        } else {
            val requestBody = request.body
            val hasRequestBody = requestBody != null
            if (hasRequestBody && !bodyEncoded(request.headers)) {
                val buffer = Buffer()
                requestBody?.writeTo(buffer)
                var charset = UTF8
                val contentType = requestBody?.contentType()
                if (contentType != null) charset = contentType.charset(UTF8)
                if (isPlaintext(buffer)) queryParams = buffer.readString(charset)
            }
        }
        //获取响应体
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        val responseBody = response.body
        val contentLength = responseBody?.contentLength()
        if (response.promisesBody() && !bodyEncoded(response.headers)) {
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer
            var charset = UTF8
            val contentType = responseBody?.contentType()
            if (contentType != null) charset = contentType.charset(UTF8)
            if (!isPlaintext(buffer)) {
                //信息量过大会只打印部分
                log(headerValues, requestUrl, queryParams, null)
                return response
            }
            if (contentLength != 0L) responseResult = buffer?.clone()?.readString(charset)
        }
        //打印获取到的全部信息
        log(headerValues, requestUrl, queryParams, responseResult)
        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    private fun isPlaintext(buffer: Buffer?): Boolean {
        if(buffer == null) return false
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) return false
            }
            return true
        } catch (e: EOFException) {
            return false
        }
    }

    private fun log(headerValues: String, requestUrl: String, queryParams: String?, responseResult: String?) {
        LogUtil.e("LoggingInterceptor", " " +
                "\n————————————————————————请求开始————————————————————————" +
                "\n请求头:\n" + headerValues.trim { it <= ' ' } +
                "\n请求地址:\n" + requestUrl +
                "\n请求参数:\n" + queryParams +
                "\n返回参数:\n" + decode(responseResult) +
                "\n————————————————————————请求结束————————————————————————")
    }

    private fun decode(unicodeStr: String?): String {
        if (unicodeStr == null) return ""
        val retBuf = StringBuffer()
        val maxLoop = unicodeStr.length
        var i = 0
        while (i < maxLoop) {
            if (unicodeStr[i] === '\\') {
                if (i < maxLoop - 5 && (unicodeStr[i + 1] === 'u' || unicodeStr[i + 1] === 'U')) try {
                    retBuf.append(Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16).toChar())
                    i += 5
                } catch (localNumberFormatException: NumberFormatException) {
                    retBuf.append(unicodeStr[i])
                }
                else retBuf.append(unicodeStr[i])
            } else {
                retBuf.append(unicodeStr[i])
            }
            i++
        }
        return retBuf.toString()
    }

}