package com.example.common.network.interceptor

import com.example.common.config.ServerConfig
import com.example.common.utils.function.orNoData
import com.example.framework.utils.LogUtil
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

/**
 * author: wyb
 * date: 2019/7/9.
 * 日志输出类
 * 需要注意的是文件流上传不能拦截，会造成闪退（已处理）
 * 返回日志过长的话，也会打印不完整
 */
internal class LoggingInterceptor : Interceptor {
    private val UTF8 = Charset.forName("UTF-8")
    private val excludedUrls = arrayOf("user/uploadImg")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestHeaders = request.headers
        val requestUrl = request.url.toString()
        //不包含服务器地址的属于下载地址或图片加载地址，不做拦截
        if (!requestUrl.contains(ServerConfig.serverUrl())) return chain.proceed(request)
        //上传文件接口文本量过大，请求参数不做拦截
        val queryParams = if (excludedUrls.any { requestUrl.contains(it) }) {
            "文件上传"
        } else {
            getRequestBody(request)
        }
        //获取响应体
        val response = chain.proceed(request)
        val responseResult = if (response.promisesBody() && !bodyEncoded(response.headers)) {
            getResponseBody(response)
        } else null
        //输出日志
        log(requestHeaders, requestUrl, queryParams, responseResult)
        return response
    }

    private fun getRequestBody(request: Request): String? {
        val requestBody = request.body
        return if (requestBody != null && !bodyEncoded(request.headers)) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val charset = requestBody.contentType()?.charset(UTF8) ?: UTF8
            if (isPlaintext(buffer)) buffer.readString(charset) else null
        } else null
    }

    private fun getResponseBody(response: Response): String? {
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            val charset = responseBody.contentType()?.charset(UTF8) ?: UTF8
            if (isPlaintext(buffer) && responseBody.contentLength() != 0L) {
                return buffer.clone().readString(charset)
            }
        }
        return null
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && contentEncoding.equals("identity", ignoreCase = false).not()
    }

    private fun isPlaintext(buffer: Buffer?): Boolean {
        buffer ?: return false
        return try {
            val prefix = Buffer()
            val byteCount = buffer.size.coerceAtMost(64)
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) break
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun log(headers: Headers, requestUrl: String, queryParams: String?, responseResult: String?) {
        LogUtil.e("LoggingInterceptor", " " +
                "\n————————————————————————请求开始————————————————————————" +
                "\n请求头:\n" + headers.toString().trimEnd { it == '\n' } +
                "\n请求地址:\n" + requestUrl +
                "\n请求参数:\n" + queryParams.orNoData() +
                "\n返回参数:\n" + decode(responseResult) +
                "\n————————————————————————请求结束————————————————————————\n"
                + " ")
    }

    private fun decode(unicodeStr: String?): String {
        if (unicodeStr == null) return ""
        val retBuf = StringBuilder()
        var i = 0
        while (i < unicodeStr.length) {
            if (unicodeStr[i] == '\\') {
                if (i < unicodeStr.length - 5 && (unicodeStr[i + 1] == 'u' || unicodeStr[i + 1] == 'U')) {
                    try {
                        retBuf.append(Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16).toChar())
                        i += 5
                    } catch (e: NumberFormatException) {
                        retBuf.append(unicodeStr[i])
                    }
                } else {
                    retBuf.append(unicodeStr[i])
                }
            } else {
                retBuf.append(unicodeStr[i])
            }
            i++
        }
        return retBuf.toString()
    }

}