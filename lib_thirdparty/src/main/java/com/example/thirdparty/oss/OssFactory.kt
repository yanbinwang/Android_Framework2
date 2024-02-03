package com.example.thirdparty.oss

import androidx.lifecycle.LifecycleOwner
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSConstants
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest
import com.alibaba.sdk.android.oss.model.ResumableUploadResult
import com.example.common.BaseApplication
import com.example.common.event.EventCode.EVENT_EVIDENCE_UPDATE
import com.example.common.network.repository.ApiResponse
import com.example.common.network.repository.reqBodyOf
import com.example.common.network.repository.request
import com.example.common.network.repository.successful
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.deleteDir
import com.example.common.utils.file.deleteFile
import com.example.common.utils.function.byServerUrl
import com.example.common.utils.function.toJsonString
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.logWTF
import com.example.thirdparty.oss.bean.OssDB
import com.example.thirdparty.oss.bean.OssSts
import com.example.thirdparty.oss.subscribe.OssSubscribe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * 阿里oss文件上传
 */
class OssFactory private constructor() : CoroutineScope {
    private var oss: OSS? = null
    private var job: Job? = null
    private var bean: OssSts? = null
    private val ossMap by lazy { ConcurrentHashMap<String, OSSAsyncTask<ResumableUploadResult?>?>() }
    private val implMap by lazy { ConcurrentHashMap<String, WeakReference<OssImpl>>() }//传入页面的classname以及页面实现EvidenceImpl
    override val coroutineContext: CoroutineContext
        get() = (Dispatchers.Main)

    companion object {
        @JvmStatic
        val instance by lazy { OssFactory() }

        private var statePair = false to false

        /**
         * 是否正在请求
         */
        val isRequest get() = statePair.first

        /**
         * 是否获取授权
         */
        val isAuthorize get() = statePair.second
    }

    /**
     * oss初始化
     * 1.服务器将过期时间调成最大，
     * 2.app启动时初始化，保证app启动期间获取的授权时间是最长的
     * 3.接口失败或者上传失败时再次调取initialize（）
     */
    @Synchronized
    fun initialize() {
        statePair = true to false
        job?.cancel()
        job = launch(Dispatchers.IO) {
            oss = OSSClient(BaseApplication.instance.applicationContext, "https://oss-cn-shenzhen.aliyuncs.com", object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken? {
                    return try {
                        val stsUrl = URL("swallow/sts/aliyun/oss".byServerUrl)
                        val conn = stsUrl.openConnection() as HttpURLConnection
                        val input = conn.inputStream
                        val json = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME)
                        log("暂无", "服务器json:\n${json}")
                        Gson().fromJson<ApiResponse<OssSts>>(json, object : TypeToken<ApiResponse<OssSts>>() {}.type).apply { bean = if (successful()) data else null }
                        if (null == bean) {
                            statePair = false to true
                            OSSFederationToken(bean?.accessKeyId.orEmpty(), bean?.accessKeySecret.orEmpty(), bean?.securityToken.orEmpty(), bean?.expiration.orEmpty())
                        } else {
                            statePair = false to false
                            null
                        }
                    } catch (e: Exception) {
                        //失败为空
                        statePair = false to false
                        null
                    }
                    //配置类如果不设置，会有默认配置
                }}, ClientConfiguration().apply {
                    connectionTimeout = 3600 * 1000//连接超时，默认15秒。
                    socketTimeout = 3600 * 1000//socket超时，默认15秒。
                    maxConcurrentRequest = Int.MAX_VALUE//最大并发请求数，默认5个。
                    maxErrorRetry = 0 })//失败后最大重试次数，默认2次。
        }
    }

    /**
     * 销毁方法
     * mainactivity中调取
     */
    private fun onClear() {
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

    /**
     * 校验oss是否初始化
     */
    private fun isLaunch(): Boolean {
        if (!isAuthorize) {
            if (!isRequest) {
                "oss初始化失败，请稍后再试".shortToast()
                initialize()
            }
            return false
        }
        return true
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    fun addObserver(observer: LifecycleOwner) {
        observer.doOnDestroy {
            onClear()
            implMap.clear()
            job?.cancel()
        }
    }

    /**
     * 断点续传
     * sourcePath->本地文件完整路径，例如/storage/emulated/0/oss/examplefile.txt(调取OssHelper可获取)
     * baoquan->保全号（创建和查找数据库的指定值）
     * fileType->1.拍照取证，2.录像取证，3.录音取证，4.录屏取证（同时也用于文件完成后刷新对应列表）
     *
     * bucketName->Bucket名称，例如examplebucket
     * objectName->Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称
     */
    fun asyncResumableUpload(sourcePath: String, baoquan: String, fileType: String) {
        if (isLaunch()) {
            //设置对应文件的断点文件存放路径
            val file = File(sourcePath)
            val fileName = file.name.split(".")[0]
            //本地文件存储路径，例如/storage/emulated/0/oss/文件名_record
            val recordDirectory = "${file.parent}/${fileName}_record"
            //查询或获取存储的值
            val query = query(sourcePath, baoquan, fileType)
            if (OssHelper.isComplete(baoquan)) {
                success(query, fileType, recordDirectory)
            } else {
                val recordDir = File(recordDirectory)
                //确保断点记录的保存路径已存在，如果不存在则新建断点记录的保存路径
                if (!recordDir.exists()) recordDir.mkdirs()
                //创建断点上传请求，并指定断点记录文件的保存路径，保存路径为断点记录文件的绝对路径。
                val request = ResumableUploadRequest(bean?.bucketName(), query.objectName, sourcePath, recordDirectory)
                //调用OSSAsyncTask cancel()方法时，设置DeleteUploadOnCancelling为false，则不删除断点记录文件
                //如果不设置此参数，则默认值为true，表示删除断点记录文件，下次再上传同一个文件时则重新上传
                request.setDeleteUploadOnCancelling(false)
                //设置上传过程回调(进度条)
                var percentage = 0
                request.progressCallback = OSSProgressCallback<ResumableUploadRequest?> { _, currentSize, totalSize ->
                    percentage = ((currentSize.toDouble() / totalSize.toDouble()) * 100).toInt()
                    post(1, baoquan, percentage)
                    log(sourcePath, "上传中\n保全号：${baoquan}\n已上传大小（currentSize）:${currentSize}\n总大小（totalSize）:${totalSize}\n上传百分比（percentage）:${percentage}%")
                }
                val resumableTask = oss?.asyncResumableUpload(request, object : OSSCompletedCallback<ResumableUploadRequest?, ResumableUploadResult?> {
                    override fun onSuccess(request: ResumableUploadRequest?, result: ResumableUploadResult?) {
                        //此处每次一个片成功都会回调，所以在监听时写通知服务器
                        log(sourcePath, "上传成功\n保全号：${baoquan}\n${result.toJsonString()}")
                        //记录oss的值
                        query.objectKey = result?.objectKey
                        response(true, query, fileType, recordDirectory, percentage)
                    }

                    override fun onFailure(request: ResumableUploadRequest?, clientExcepion: ClientException?, serviceException: ServiceException?) {
                        var result = "上传失败\n保全号：${baoquan}"
                        //请求异常
                        if (clientExcepion != null) {
                            result += "\n本地异常：\n${clientExcepion.message}"
                            //本地异常诱发的原因有很多，擅自修改手机本机时间，oss断点数据库出错都有可能导致，此时直接清空断点续传的记录文件，让用户从头来过
                            if (NetWorkUtil.isNetworkAvailable()) recordDirectory.deleteDir()
                        }
                        if (serviceException != null) result += "\n服务异常：\n${serviceException.message}"
                        log(sourcePath, result)
                        //异常处理->刷新列表
                        response(false, query, fileType, recordDirectory, errorMessage = "传输状态：${result}")
                    }
                })
                //等待完成断点上传任务
//                resumableTask?.waitUntilFinished()
                ossMap[baoquan] = resumableTask
            }
        }
    }

    /**
     * 断点续传开启
     */
    private fun query(sourcePath: String, baoquan: String, fileType: String, isUpload: Boolean = true): OssDB {
        //查询本地存储的数据，不存在则添加一条
        var query = OssHelper.query(sourcePath)
        if (null == query) {
            query = OssDB().also {
                it.sourcePath = sourcePath
                it.userId = getUserId()
                it.baoquan = baoquan
                it.objectName = bean?.objectName(fileType)
                it.extras = ""
            }
            OssHelper.insert(query)
        }
        OssHelper.updateUpload(baoquan, isUpload)
//        EVENT_EVIDENCE_UPLOAD_HAND.post()
        post(0, baoquan)
        return query
    }

    /**
     * 完成时候的回调
     */
    private fun response(isSuccess: Boolean, query: OssDB, fileType: String, recordDirectory: String?, percentage: Int = 0, errorMessage: String? = "") {
        val baoquan = query.baoquan
        if (isSuccess) {
            //全部传完停止服务器
            if (percentage == 100) {
                //优先保证本地数据库记录成功
                OssHelper.updateComplete(baoquan, true)
                success(query, fileType, recordDirectory.orEmpty())
            }
        } else {
            //即刻停止当前请求，刷新列表，并通知服务器错误信息
            val value = ossMap[baoquan]
            (value as? OSSAsyncTask<*>?)?.cancel()
            OssHelper.updateUpload(baoquan, false)
            failure(baoquan, errorMessage.orEmpty())
        }
        ossMap.remove(baoquan)
    }

    /**
     * 告知服务器此次成功的链接地址
     */
    private fun success(query: OssDB, fileType: String, recordDirectory: String) {
        launch {
            val baoquan = query.baoquan
            request({ OssSubscribe.getOssEditApi(baoquan, reqBodyOf("fileUrl" to query.objectKey)) }, {
                //删除对应断点续传的文件夹和源文件
                query.sourcePath.deleteDir()
                recordDirectory.deleteFile()
                OssHelper.delete(query)
                post(2, baoquan, success = true)
                EVENT_EVIDENCE_UPDATE.post(fileType)
            }, {
                OssHelper.updateUpload(baoquan, false)
                post(2, baoquan, success = false)
            })
        }
    }

    /**
     * 告知服务器此次失败的链接地址-》只做通知
     */
    private fun failure(baoquan: String, errorMessage: String) {
        launch {
            post(2, baoquan, success = false)
            request({ OssSubscribe.getOssEditApi(baoquan, reqBodyOf("errorMessage" to errorMessage)) })
        }
    }

    /**
     * 接口回调方法
     */
    private fun post(type: Int, baoquan_no: String, progress: Int = 0, success: Boolean = true) {
        for ((_, value) in implMap) {
            value.get()?.apply {
                when (type) {
                    0 -> onStart(baoquan_no)
                    1 -> onLoading(baoquan_no, progress)
                    2 -> onComplete(baoquan_no, success)
                }
            }
        }
    }

    private fun log(localPath: String, state: String) = " \n————————————————————————文件上传————————————————————————\n文件路径：${localPath}\n上传状态：${state}\n————————————————————————文件上传————————————————————————".logWTF

    /**
     * 回调接口
     */
    interface OssImpl {
        /**
         * 更新某个item的状态（开始上传）
         */
        fun onStart(baoquan: String)

        /**
         * 更新某个item的进度
         */
        fun onLoading(baoquan: String, progress: Int)

        /**
         * 更新某个item的状态（完成）
         */
        fun onComplete(baoquan: String, success: Boolean)
    }

}