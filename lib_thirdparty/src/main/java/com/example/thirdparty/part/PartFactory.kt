package com.example.thirdparty.part

import androidx.lifecycle.LifecycleOwner
import com.example.common.event.EventCode.EVENT_EVIDENCE_UPDATE
import com.example.common.network.repository.successful
import com.example.common.utils.file.deleteDir
import com.example.common.utils.file.deleteFile
import com.example.common.utils.file.getSizeFormat
import com.example.common.utils.file.mb
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.multiply
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.removeEndZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import com.example.greendao.bean.PartDB
import com.example.thirdparty.oss.OssDBHelper
import com.example.thirdparty.part.subscribe.PartSubscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * 文件分片上传
 */
class PartFactory private constructor() : CoroutineScope {
    //key->保全号（服务器唯一id）value->对应part的协程类
    private val partMap by lazy { ConcurrentHashMap<String, Job>() }
    //传入页面的lifecycle以及页面实现的OssImpl
    private val implList by lazy { ArrayList<WeakReference<PartImpl>>() }
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        /**
         * 单例初始化
         */
        @JvmStatic
        val instance by lazy { PartFactory() }
    }

    // <editor-fold defaultstate="collapsed" desc="基础方法">
    /**
     * 部分页面实现回调
     * owner: LifecycleOwner
     * impl: OssImpl
     */
    fun bind(owner: LifecycleOwner, impl: PartImpl) {
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
        val iterator = partMap.iterator()
        while (iterator.hasNext()) {
            try {
                //在调用了cancel()后还会继续走几次progress
                val value = iterator.next()
                (value as? Job)?.cancel()
            } catch (_: Exception) {
            }
        }
        partMap.clear()
        implList.clear()
        //取消页面协程
//        job.cancel()
        coroutineContext.cancelChildren()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="证据文件上传">
    /**
     * sourcePath->文件路径
     * baoquan_no->保全号本地数据库主键值，即数据库id
     * fileType-文件类型
     * isZip-是否是压缩包
     */
    @Synchronized
    fun asyncResumableUpload(sourcePath: String, baoquan: String, fileType: String, isZip: Boolean = false) {
        if (!PartDBHelper.isUpload(baoquan)) {
            log(sourcePath, "开始上传")
            when (fileType) {
                "3", "4" -> {
                    if (File(sourcePath).length() >= 100.mb) {
                        toPartUpload(sourcePath, fileType, baoquan, isZip)
                    } else {
                        toUpload(sourcePath, fileType, baoquan, isZip)
                    }
                }
                else -> toUpload(sourcePath, fileType, baoquan, isZip)
            }
        } else {
            log(sourcePath, "正在上传")
        }
    }

    /**
     * 文件分片上传
     * 1.分片与断点的区别在于，它是固定顺序一个个传的，而不是随机某个拓片打乱的形式上传
     * 2.生成拓片的时候会根据源文件路径创建一个${fileName}_record命名的文件夹用于存放拓片
     * 3.每次上传之前可以清空一次拓片文件夹，避免造成文件冗余
     */
    private fun toPartUpload(sourcePath: String, fileType: String, baoquan: String, isZip: Boolean = false) {
        partMap[baoquan] = launch {
            val query = query(sourcePath, baoquan)
            //尝试清空一下本地拓片文件夹，分片上传存放拓片的文件内只会有一个tmp文件
            val file = File(sourcePath)
            val fileName = file.name.split(".")[0]
            val recordDirectory = "${file.parent}/${fileName}_record"
            recordDirectory.deleteDir()
            //获取分片信息并开始分片
            val tmp = PartDBHelper.split(query)
            query.filePointer = tmp.filePointer.orZero
            //方法内部调用方法，循环传输
            suspendingPartUpload(query, tmp.filePath.orEmpty(), recordDirectory, fileType, baoquan, isZip)
        }
    }

    private suspend fun suspendingPartUpload(query: PartDB, tmpPath: String, recordDirectory: String, fileType: String, baoquan: String, isZip: Boolean = false) {
        val paramsFile = File(tmpPath)
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("baoquan", baoquan)
        builder.addFormDataPart("totalNum", query.total.toString())
        builder.addFormDataPart("file", paramsFile.name, paramsFile.asRequestBody((if (isZip) "zip" else "video").toMediaTypeOrNull()))
        PartSubscribe.getPartUploadApi(builder.build().parts).apply {
            //不管此次请求结果如何，都将本次切片文件删除，失败后会从未上传的地方重新生成切片，成功则直接在此处删除
            tmpPath.deleteFile()
            if (successful()) {
                log("${query.sourcePath}\n分片路径：${tmpPath}\n分片数量：${query.total}\n当前下标：${query.index}\n当片大小：${File(tmpPath).getSizeFormat()}", "成功")
                //此次分片服务器已经收到了，本地记录一下
                PartDBHelper.updateUpload(query.sourcePath, query.filePointer, query.index)
                //重新获取当前数据库中存储的值
                val reQuery = PartDBHelper.query(baoquan)
                if (null != reQuery) {
                    //下标+1，开始切下一块
                    reQuery.index += 1
                    if (reQuery.index < query.total) {
                        //获取下一块分片,并且记录
                        val nextTmp = PartDBHelper.split(reQuery)
                        reQuery.filePointer = nextTmp.filePointer.orZero
                        val progress = query.index.toString().divide(reQuery.total.toString(), 2).multiply("100").removeEndZero().toSafeInt()
                        callback(1, query.baoquan_no, progress, true)
                        //再开启下一次传输
                        suspendingPartUpload(reQuery, nextTmp.filePath.orEmpty(), recordDirectory, fileType, baoquan, isZip)
                    } else if (reQuery.index >= query.total) {
                        //此时即便通知服务器接口并未调取成功，也已经将最后一个分片传输成功了，故而调取后直接执行成功和刷新
                        PartSubscribe.getPartCombineApi(mapOf("baoquan_no" to baoquan))
                        recordDirectory.deleteFile()
                        success(query, fileType)
                        end(baoquan)
                    }
                } else {
                    //没查到，说明之前应该已经完成了请求交互
                    recordDirectory.deleteFile()
                    success(query, fileType)
                    end(baoquan)
                }
            } else {
                val errMsg = msg.orEmpty()
                log(query.sourcePath, "失败\n失败原因：${errMsg}")
                //后端坑，可能已经插入成功了，但是此时请求回调了
                if (errMsg == "该保全号信息有误") {
                    recordDirectory.deleteFile()
                    success(query, fileType)
                } else {
                    failure(baoquan)
                }
                end(baoquan)
            }
        }
    }

    /**
     * 文件整体上传
     */
    private fun toUpload(sourcePath: String, fileType: String, baoquan: String, isZip: Boolean = false) {
        partMap[baoquan] = launch {
            val query = query(sourcePath, baoquan)
            val mediaType = when (fileType) {
                "1" -> "image"
                "2" -> "audio"
                else -> if (isZip) "zip" else "video"
            }
            val paramsFile = File(sourcePath)
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("baoquan", baoquan)
            builder.addFormDataPart("file", paramsFile.name, paramsFile.asRequestBody(mediaType.toMediaTypeOrNull()))
            PartSubscribe.getUploadApi(builder.build().parts).apply {
                if (successful()) {
                    log(sourcePath, "成功")
                    //优先保证本地数据库记录成功
                    OssDBHelper.updateComplete(baoquan, true)
                    success(query, fileType)
                } else {
                    log(sourcePath, "失败\n失败原因：${msg}")
                    //即刻停止当前请求，刷新列表，并通知服务器错误信息
                    failure(baoquan)
                }
                log(sourcePath, "非分片上传->执行完毕")
                //在调用cancel()之后，协程不会立即停止，后面的代码仍然会执行，除非遇到了挂起函数或者主动检查取消状态导致抛出异常
                end(baoquan)
            }
        }
    }

    /**
     * 某个分片上传成功，取消和删除对应标记的map
     */
    private fun end(baoquan: String) {
        val value = partMap[baoquan]
        value?.cancel()
        partMap.remove(baoquan)
    }

    /**
     * 初始化相关参数
     */
    private suspend fun query(sourcePath: String, baoquan: String): PartDB {
        //查询本地存储的数据，不存在则添加一条
        var query = PartDBHelper.query(baoquan)
        if (null == query) {
            query = PartDB().also {
                it.baoquan_no = baoquan
                it.sourcePath = sourcePath
                it.userId = getUserId()
                it.index = 0
                it.filePointer = 0
                it.extras = ""
            }
            PartDBHelper.insert(query)
        }
        PartDBHelper.updateUpload(baoquan, true)
        callback(0, baoquan)
        return query
    }

    /**
     * 告知服务器此次成功的链接地址
     */
    private suspend fun success(query: PartDB, fileType: String) {
        query.sourcePath.deleteFile()
        PartDBHelper.delete(query)
        callback(2, query.baoquan_no, success = true)
        EVENT_EVIDENCE_UPDATE.post(fileType)
    }

    /**
     * 告知服务器此次失败的链接地址-》只做通知
     */
    private suspend fun failure(baoquan: String) {
        OssDBHelper.updateUpload(baoquan, false)
        callback(2, baoquan, success = false)
    }

    /**
     * 接口回调方法
     */
    private suspend fun callback(type: Int, baoquan: String, progress: Int = 0, success: Boolean = true) {
        withContext(Main) {
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
    }

    /**
     * 回调接口
     */
    interface PartImpl {
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

    /**
     * log日志查看
     */
    private fun log(localPath: String, state: String) = " \n————————————————————————文件上传————————————————————————\n文件路径：${localPath}\n上传状态：${state}\n————————————————————————文件上传————————————————————————".logWTF

}