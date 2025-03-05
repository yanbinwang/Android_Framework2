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
import com.example.common.utils.GsonUtil.getType
import com.example.common.utils.NetWorkUtil
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.deleteDir
import com.example.common.utils.file.deleteFile
import com.example.common.utils.function.byServerUrl
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.common.utils.toJson
import com.example.common.utils.toObj
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import com.example.greendao.bean.OssDB
import com.example.thirdparty.oss.bean.OssSts
import com.example.thirdparty.oss.bean.OssSts.Companion.bucketName
import com.example.thirdparty.oss.bean.OssSts.Companion.objectName
import com.example.thirdparty.oss.subscribe.OssSubscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 阿里oss文件上传
 */
class OssFactory private constructor() : CoroutineScope {
    //oss基础类
    private var oss: OSS? = null
    //key->保全号（服务器唯一id）value->对应oss的传输类/协程
    private val ossMap by lazy { ConcurrentHashMap<String, OSSAsyncTask<ResumableUploadResult?>?>() }
    private val ossJobMap by lazy { ConcurrentHashMap<String, Job?>() }
    //传入页面的lifecycle以及页面实现的OssImpl
    private val implList by lazy { ArrayList<WeakReference<OssImpl>>() }
    //内部oss状态 first->是否正在请求 second->是否获取授权
    private var state = false to false
    //协程整体，因全局文件上传都需要调取oss，故而无需考虑cancel问题（方法可补充，main中调取）
    private var initJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        /**
         * 单例初始化
         */
        @JvmStatic
        val instance by lazy { OssFactory() }

        /**
         * 直接执行文件上传-》挂起的形式
         */
        suspend fun suspendingAsyncResumableUpload(sourcePath: String?, onLoading: (progress: Int?) -> Unit = {}, privately: Boolean = false) = suspendCancellableCoroutine {
            if (sourcePath.isNullOrEmpty()) {
                it.resume("")
            } else {
                instance.asyncResumableUpload(sourcePath, onSuccess = { objectKey ->
                    it.resume(objectKey)
                }, onLoading = { progress ->
                    onLoading.invoke(progress)
                }, onFailed = { result ->
                    it.resumeWithException(RuntimeException(result))
                }, privately = privately)
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基础方法">
    /**
     * oss初始化
     * 1.后端服务器将oss过期时间调成最大
     * 2.app启动时application中初始化，保证启动期间先获取一次授权，时间过长会重置（120分钟）
     * 3.接口失败或者上传失败时再次调取initialize（）重新赋值
     */
    @Synchronized
    fun initialize() {
        state = true to false
        initJob?.cancel()
        initJob = launch {
            val value = withContext(IO) {
                suspendingOSSClient()
            }
            state = false to value
        }
    }

    private suspend fun suspendingOSSClient() = suspendCancellableCoroutine {
        try {
            oss = OSSClient(BaseApplication.instance.applicationContext, "https://oss-cn-shenzhen.aliyuncs.com", object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken? {
                    val stsUrl = URL("swallow/sts/aliyun/oss".byServerUrl)
                    val conn = stsUrl.openConnection() as? HttpURLConnection
                    return conn?.inputStream.use { input ->
                        val json = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME)
                        log("暂无", "服务器json:\n${json}")
                        //服务器返回数据体处理
                        val token = json.toObj<ApiResponse<OssSts>>(getType(ApiResponse::class.java, OssSts::class.java)).let { response ->
                            if (response.successful()) {
                                val bean = response?.data
                                //data就算服务器返回成功，如果本身是空，也算失败
                                bean ?: return null
                                OSSFederationToken(bean.accessKeyId.orEmpty(), bean.accessKeySecret.orEmpty(), bean.securityToken.orEmpty(), bean.expiration.orEmpty())
                            } else {
                                null
                            }
                        }
                        //不为null就是
                        it.resume(token != null)
                        token
                    }
                }}, ClientConfiguration().apply {
                    connectionTimeout = 3600 * 1000//连接超时，默认15秒。
                    socketTimeout = 3600 * 1000//socket超时，默认15秒。
                    maxConcurrentRequest = Int.MAX_VALUE//最大并发请求数，默认5个。
                    maxErrorRetry = 0//失败后最大重试次数，默认2次。
                }
            )
        } catch (e: Exception) {
            //一旦被catch到异常。就是失败
            it.resume(false)
        }
    }

    /**
     * 校验oss是否初始化
     * 1.请求正在进行的情况下，只会返回结果
     * 2.请求不在进行，结果失败的情况下，会主动再发起一次初始化，此时可以设置是否有默认提示
     */
    fun isInit(isToast: Boolean = true): Boolean {
        return if (!state.second) {
            if (!state.first) {
                if (isToast) "oss初始化失败，请稍后再试".shortToast()
                initialize()
            }
            false
        } else {
            true
        }
    }

    /**
     * 部分页面实现回调
     * owner: LifecycleOwner
     * impl: OssImpl
     */
    fun bind(owner: LifecycleOwner, impl: OssImpl) {
        if (implList.find { it == WeakReference(impl) } == null) {
            implList.add(WeakReference(impl))
            owner.doOnDestroy {
                implList.remove(WeakReference(impl))
            }
        }
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    fun cancelAllWork(observer: LifecycleOwner) {
        observer.doOnDestroy {
            cancelAllWork()
        }
    }

    /**
     * 销毁方法
     */
    private fun cancelAllWork() {
        //取消所有oss异步
        val taskIterator = ossMap.iterator()
        while (taskIterator.hasNext()) {
            try {
                //在调用了cancel()后还会继续走几次progress
                val value = taskIterator.next()
                (value as? OSSAsyncTask<*>?)?.cancel()
            } catch (_: Exception) {
            }
        }
        //取消所有协程
        val jobIterator = ossJobMap.iterator()
        while (jobIterator.hasNext()) {
            try {
                //在调用了cancel()后还会继续走几次progress
                val value = jobIterator.next()
                (value as? Job)?.cancel()
            } catch (_: Exception) {
            }
        }
        ossMap.clear()
        ossJobMap.clear()
        implList.clear()
        //取消页面协程
        initJob?.cancel()
//        job.cancel()
        coroutineContext.cancelChildren()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="证据文件上传">
    /**
     * 断点续传
     * sourcePath->本地文件完整路径，例如/storage/emulated/0/oss/examplefile.txt(调取OssHelper可获取)
     * baoquan->保全号（创建和查找数据库的指定值）
     * fileType->1.拍照取证，2.录像取证，3.录音取证，4.录屏取证（同时也用于文件完成后刷新对应列表）
     *
     * bucketName->Bucket名称，例如examplebucket
     * objectName->Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称
     */
    @Synchronized
    fun asyncResumableUpload(sourcePath: String?, baoquan: String, fileType: String) {
        sourcePath ?: return
        if (isInit()) {
            val data = init(sourcePath, baoquan, fileType)
            val query = data.first
            val recordDirectory = data.second
            if (OssDBHelper.isComplete(baoquan)) {
                success(query, fileType, recordDirectory)
            } else {
                val recordDir = File(recordDirectory)
                //确保断点记录的保存路径已存在，如果不存在则新建断点记录的保存路径
                if (!recordDir.exists()) recordDir.mkdirs()
                //创建断点上传请求，并指定断点记录文件的保存路径，保存路径为断点记录文件的绝对路径。
                val request = ResumableUploadRequest(bucketName(), query.objectName, sourcePath, recordDirectory)
                //调用OSSAsyncTask cancel()方法时，设置DeleteUploadOnCancelling为false，则不删除断点记录文件
                //如果不设置此参数，则默认值为true，表示删除断点记录文件，下次再上传同一个文件时则重新上传
                request.setDeleteUploadOnCancelling(false)
                //设置上传过程回调(进度条)
                var percentage = 0
                request.progressCallback = OSSProgressCallback<ResumableUploadRequest?> { _, currentSize, totalSize ->
//                    percentage = ((currentSize.toSafeDouble() / totalSize.toSafeDouble()) * 100).toSafeInt()
                    percentage = currentSize.toString().divide(totalSize.toString(), 2).multiply("100").toSafeInt()
                    callback(1, baoquan, percentage)
                    log(sourcePath, "上传中\n保全号：${baoquan}\n已上传大小（currentSize）:${currentSize}\n总大小（totalSize）:${totalSize}\n上传百分比（percentage）:${percentage}%")
                }
                val resumableTask = oss?.asyncResumableUpload(request, object : OSSCompletedCallback<ResumableUploadRequest?, ResumableUploadResult?> {
                    override fun onSuccess(request: ResumableUploadRequest?, result: ResumableUploadResult?) {
                        //此处每次一个片成功都会回调，所以在监听时写通知服务器
                        log(sourcePath, "上传成功\n保全号：${baoquan}\n${result.toJson()}")
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
        } else {
            init(sourcePath, baoquan, fileType)
            failure(baoquan, "oss初始化失败")
        }
    }

    /**
     * 初始化相关参数
     */
    private fun init(sourcePath: String, baoquan: String, fileType: String): Pair<OssDB, String> {
        //设置对应文件的断点文件存放路径
        val file = File(sourcePath)
        val fileName = file.name.split(".")[0]
        //本地文件存储路径，例如/storage/emulated/0/oss/文件名_record
        val recordDirectory = "${file.parent}/${fileName}_record"
        //查询或获取存储的值
        return query(sourcePath, baoquan, fileType) to recordDirectory
    }

    /**
     * 断点续传开启
     */
    private fun query(sourcePath: String, baoquan: String, fileType: String): OssDB {
        //查询本地存储的数据，不存在则添加一条
        var query = OssDBHelper.query(sourcePath)
        if (null == query) {
            query = OssDB().also {
                it.sourcePath = sourcePath
                it.userId = getUserId()
                it.baoquan = baoquan
                it.objectName = objectName(fileType)
                it.extras = ""
            }
            OssDBHelper.insert(query)
        }
        OssDBHelper.updateUpload(baoquan, true)
        callback(0, baoquan)
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
                OssDBHelper.updateComplete(baoquan, true)
                success(query, fileType, recordDirectory.orEmpty())
            }
        } else {
            //即刻停止当前请求，刷新列表，并通知服务器错误信息
            val value = ossMap[baoquan]
            (value as? OSSAsyncTask<*>?)?.cancel()
            failure(baoquan, errorMessage)
        }
        ossMap.remove(baoquan)
    }

    /**
     * 告知服务器此次成功的链接地址
     */
    private fun success(query: OssDB, fileType: String, recordDirectory: String) {
        val baoquan = query.baoquan
        ossJobMap[baoquan] = launch {
            request({ OssSubscribe.getOssEditApi(baoquan, reqBodyOf("fileUrl" to query.objectKey)) }, {
                //删除对应断点续传的文件夹和源文件
                query.sourcePath.deleteDir()
                recordDirectory.deleteFile()
                OssDBHelper.delete(query)
                callback(2, baoquan, success = true)
                EVENT_EVIDENCE_UPDATE.post(fileType)
            }, {
                failure(baoquan, it.errMessage)
            }, {
                end(baoquan)
            })
        }
    }

    /**
     * 告知服务器此次失败的链接地址-》只做通知
     */
    private fun failure(baoquan: String, errorMessage: String?) {
        OssDBHelper.updateUpload(baoquan, false)
        callback(2, baoquan, success = false)
        ossJobMap[baoquan] = launch {
            request({ OssSubscribe.getOssEditApi(baoquan, reqBodyOf("errorMessage" to errorMessage)) }, end = {
                end(baoquan)
            })
        }
    }

    /**
     * 取消/终止
     */
    private fun end(baoquan: String) {
        ossJobMap[baoquan]?.cancel()
        ossJobMap.remove(baoquan)
    }

    /**
     * 接口回调方法
     */
    private fun callback(type: Int, baoquan: String, progress: Int = 0, success: Boolean = true) {
        implList.forEach {
            it.get()?.apply {
                when (type) {
                    0 -> onStart(baoquan)
                    1 -> onLoading(baoquan, progress)
                    2 -> onComplete(baoquan, success)
                }
            }
        }
    }

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="普通文件上传">
    /**
     * 直接执行文件上传
     */
    @Synchronized
    fun asyncResumableUpload(sourcePath: String?, onStart: () -> Unit = {}, onSuccess: (objectKey: String?) -> Unit = {}, onLoading: (progress: Int?) -> Unit = {}, onFailed: (result: String?) -> Unit = {}, onComplete: () -> Unit = {}, privately: Boolean = false) {
        sourcePath ?: return
        if (isInit()) {
            onStart.invoke()
            //设置对应文件的断点文件存放路径
            val file = File(sourcePath)
            val fileName = file.name.split(".")[0]
            //本地文件存储路径，例如/storage/emulated/0/oss/文件名_record
            val storeDir = File(getStoragePath("选择的文件"))
            val recordDirectory = "${storeDir.parent}/${fileName}_record"
            val recordDir = File(recordDirectory)
            if (!recordDir.exists()) recordDir.mkdirs()
            //构建请求
            val request = ResumableUploadRequest(bucketName(false, privately), objectName("5", sourcePath), sourcePath, recordDirectory)
            request.setDeleteUploadOnCancelling(false)
            var progress = 0
            request.progressCallback = OSSProgressCallback<ResumableUploadRequest?> { _, currentSize, totalSize ->
                val percentage = ((currentSize.toDouble() / totalSize.toDouble()) * 100).toSafeInt()
                progress = percentage
                onLoading.invoke(percentage)
            }
            val resumableTask = oss?.asyncResumableUpload(request, object : OSSCompletedCallback<ResumableUploadRequest?, ResumableUploadResult?> {
                override fun onSuccess(request: ResumableUploadRequest?, result: ResumableUploadResult?) {
                    if (progress == 100) {
                        recordDirectory.deleteDir()
                        onComplete(true, result?.objectKey)
                    }
                }

                override fun onFailure(request: ResumableUploadRequest?, clientExcepion: ClientException?, serviceException: ServiceException?) {
                    var result = "上传失败"
                    //请求异常
                    if (clientExcepion != null) {
                        result += "\n本地异常：\n${clientExcepion.message}"
                        //本地异常诱发的原因有很多，擅自修改手机本机时间，oss断点数据库出错都有可能导致，此时直接清空断点续传的记录文件，让用户从头来过
                        if (NetWorkUtil.isNetworkAvailable()) recordDirectory.deleteDir()
                    }
                    if (serviceException != null) result += "\n服务异常：\n${serviceException.message}"
                    onComplete(false, result)
                }

                private fun onComplete(success: Boolean, callbackMessage: String?) {
                    if (success) {
                        onSuccess.invoke(callbackMessage)
                    } else {
                        onFailed.invoke(callbackMessage)
                        val value = ossMap[sourcePath]
                        (value as? OSSAsyncTask<*>?)?.cancel()
                    }
                    onComplete.invoke()
                    ossMap.remove(sourcePath)
                }
            })
            ossMap[sourcePath] = resumableTask
        }
    }
    // </editor-fold>

    /**
     * log日志查看
     */
    private fun log(localPath: String, state: String) = " \n————————————————————————文件上传————————————————————————\n文件路径：${localPath}\n上传状态：${state}\n————————————————————————文件上传————————————————————————".logWTF

}