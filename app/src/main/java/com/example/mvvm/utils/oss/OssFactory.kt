package com.example.mvvm.utils.oss

import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.OSSConstants
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.ResumableUploadResult
import com.example.common.BaseApplication
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.successful
import com.example.common.utils.function.byServerUrl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * 阿里oss文件上传
 */
class OssFactory private constructor() : CoroutineScope {
    private var isAuthorize = false
    private var oss: OSS? = null
    private var job: Job? = null
    private var bean: OssSts? = null
    private val ossMap by lazy { ConcurrentHashMap<String, OSSAsyncTask<ResumableUploadResult?>?>() }
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    companion object {
        @JvmStatic
        val instance by lazy { OssFactory() }
    }

    /**
     * oss初始化
     * 1.服务器将过期时间调成最大，
     * 2.app启动时初始化，保证app启动期间获取的授权时间是最长的
     * 3.接口失败或者上传失败时再次调取initialize（）
     */
    @Synchronized
    fun initialize() {
        isAuthorize = false
        job?.cancel()
        job = launch(Dispatchers.IO) {
            oss = OSSClient(BaseApplication.instance.applicationContext, "https://oss-cn-shenzhen.aliyuncs.com", object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken? {
                    return try {
                        isAuthorize = true
                        val stsUrl = URL("swallow/sts/aliyun/oss".byServerUrl)
                        val conn = stsUrl.openConnection() as HttpURLConnection
                        val input = conn.inputStream
                        val json = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME)
//                        log("暂无", "服务器json:\n${jsonText}")
                        Gson().fromJson<ApiResponse<OssSts>>(json, object : TypeToken<ApiResponse<OssSts>>() {}.type).apply { bean = if(successful()) data else null }
                        OSSFederationToken(bean?.accessKeyId.orEmpty(), bean?.accessKeySecret.orEmpty(), bean?.securityToken.orEmpty(), bean?.expiration.orEmpty())
                    } catch (e: Exception) {
                        //失败为空
                        isAuthorize = false
                        null
                    }
                    //配置类如果不设置，会有默认配置
                }}, ClientConfiguration().apply {
                connectionTimeout = 3600 * 1000//连接超时，默认15秒。
                socketTimeout = 3600 * 1000//socket超时，默认15秒。
                maxConcurrentRequest = Int.MAX_VALUE//最大并发请求数，默认5个。
                maxErrorRetry = 0//失败后最大重试次数，默认2次。
            })
        }
    }

    /**
     * 销毁方法
     * mainactivity中调取
     */
    fun onClear() {
        isAuthorize = false
        val iterator = ossMap.iterator()
        while (iterator.hasNext()) {
            try {
                //在调用了cancel()后还会继续走几次progress
                val value = iterator.next()
                (value as OSSAsyncTask<*>?)?.cancel()
            } catch (_: Exception) {
            }
        }
        ossMap.clear()
        coroutineContext.cancel()
    }



}