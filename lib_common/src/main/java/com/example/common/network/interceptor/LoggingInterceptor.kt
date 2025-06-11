package com.example.common.network.interceptor

import com.example.common.config.ServerConfig
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.value.limitLength
import com.example.framework.utils.logE
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
class LoggingInterceptor : Interceptor {
    private val UTF8 by lazy { Charset.forName("UTF-8") }
    private val uploadFileUrls by lazy { arrayOf("user/uploadImg") }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = request.headers
        val method = request.method
        val url = request.url.toString()
        //不包含服务器地址的属于下载地址或图片加载地址，不做拦截
        if (!url.contains(ServerConfig.serverUrl())) return chain.proceed(request)
        //上传文件接口文本量过大，请求参数不做拦截
        val params = if (uploadFileUrls.any { url.contains(it) }) {
            "文件上传"
        } else {
            getRequestBody(request)
        }
        //获取响应体
        val response = chain.proceed(request)
        val code = response.code
        val body = if (response.promisesBody() && !bodyEncoded(response.headers)) {
            getResponseBody(response)
        } else {
            null
        }
        //输出日志
        log(headers, method, url, params, code, body)
        return response
    }

    private fun getRequestBody(request: Request): String? {
        val requestBody = request.body
        return if (requestBody != null && !bodyEncoded(request.headers)) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val charset = requestBody.contentType()?.charset(UTF8) ?: UTF8
            if (isPlaintext(buffer)) buffer.readString(charset) else null
        } else {
            null
        }
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

    private fun log(headers: Headers, method: String, url: String, params: String?, code: Int, body: String?) {
        ("————————————————————————请求开始————————————————————————" +
                "\n请求头:\n" + headers.toString().trimEnd { it == '\n' } +
                "\n请求方式:\n" + method +
                "\n请求地址:\n" + url +
                "\n请求参数:\n" + params.orNoData() +
                "\n响应编码:\n" + code +
                "\n响应体:\n" + decode(body).limitLength() +
                "\n————————————————————————请求结束————————————————————————\n" +
                " ").logE("LoggingInterceptor")
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