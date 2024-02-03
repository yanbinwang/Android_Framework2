package com.example.thirdparty.greendao.utils

import androidx.lifecycle.LifecycleOwner
import com.example.common.event.EventCode.EVENT_EVIDENCE_UPDATE
import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.deleteFile
import com.example.common.utils.file.getSizeFormat
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import com.example.thirdparty.greendao.bean.EvidenceDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
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
 * 放在MainActivity中addObserver，绑定全局的文件上传
 */
object EvidenceExecutors : CoroutineScope {
    private var lastRefreshTime = 0L
    private val implMap by lazy { ConcurrentHashMap<String, WeakReference<EvidenceImpl>>() }//传入页面的classname以及页面实现EvidenceImpl
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    @JvmStatic
    fun addObserver(observer: LifecycleOwner) {
        observer.doOnDestroy {
            implMap.clear()
            job.cancel()
        }
    }

    /**
     * 部分页面实现回调
     * className: String
     * impl: WeakReference<EvidenceImpl>
     */
    @JvmStatic
    fun bind(pair: Pair<String, WeakReference<EvidenceImpl>>) {
        implMap[pair.first] = pair.second
    }

    /**
     * 解绑
     */
    @JvmStatic
    fun unbind(className: String) {
        implMap.remove(className)
    }

    /**
     * baoquan_no--数据库id（文件路径）
     * sourcePath--本地数据库主键值
     * fileType--文件类型
     * isZip--是否是压缩包
     */
    @JvmStatic
    fun submit(baoquan_no: String, sourcePath: String, fileType: String, isZip: Boolean = false) {
        if (!EvidenceHelper.isUpload(baoquan_no)) {
            " \n————————————————————————文件上传————————————————————————\n开始上传:${baoquan_no}::${sourcePath}\n————————————————————————文件上传————————————————————————".logWTF
            when (fileType) {
                "3", "4" -> {
                    if (File(sourcePath).length() >= 100 * 1024 * 1024) {
                        partUpload(baoquan_no, sourcePath, fileType, isZip)
                    } else {
                        upload(baoquan_no, sourcePath, fileType, isZip)
                    }
                }
                else -> upload(baoquan_no, sourcePath, fileType, isZip)
            }
        } else {
            " \n————————————————————————文件上传————————————————————————\n正在上传:${baoquan_no}::${sourcePath}\n————————————————————————文件上传————————————————————————".logWTF
        }
    }

    @JvmStatic
    private fun partUpload(baoquan_no: String, sourcePath: String, fileType: String, isZip: Boolean = false) {
        launch {
            suspendingSplit(baoquan_no, sourcePath).apply {
                suspendingUpload(first, second.filePath.orEmpty(), fileType, baoquan_no, isZip)
            }
        }
    }

    @JvmStatic
    private suspend fun suspendingSplit(baoquan_no: String, sourcePath: String): Pair<EvidenceDB, FileUtil.TmpInfo> {
        return withContext(IO) {
            //查询/创建一条用于存表的数据，并重新插入一次
            val queryDB = query(baoquan_no, sourcePath)
            EvidenceHelper.insert(queryDB)
            //先将当前查询/创建的数据在未上传列表内刷出来，文件分片需要一些时间
            EvidenceHelper.update(sourcePath, true)
            post(0, baoquan_no)
            //开始分片，并获取分片信息
            val tmp = EvidenceHelper.split(queryDB)
            queryDB.filePointer = tmp.fileSize
            Pair(queryDB, tmp)
        }
    }

    @JvmStatic
    private suspend fun suspendingUpload(queryDB: EvidenceDB, tmpPath: String, fileType: String, baoquan_no: String, isZip: Boolean = false) {
        withContext(IO) {
            val paramsFile = File(tmpPath)
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("baoquan", baoquan_no)
            builder.addFormDataPart("totalNum", queryDB.total.toString())
            builder.addFormDataPart("file", paramsFile.name, paramsFile.asRequestBody((if (isZip) "zip" else "video").toMediaTypeOrNull()))
            //成功
            " \n————————————————————————文件上传-分片————————————————————————\n文件路径：${baoquan_no}::${queryDB.sourcePath}\n分片数量:${queryDB.total}\n当前下标：${queryDB.index}\n当片路径：${tmpPath}\n当片大小：${File(tmpPath).getSizeFormat()}\n上传状态：成功\n————————————————————————文件上传-分片————————————————————————".logWTF
            //成功记录此次分片，并删除这个切片
            tmpPath.deleteFile()
            //此次分片服务器已经收到了，手机本地记录一下
            EvidenceHelper.update(queryDB.baoquan, queryDB.filePointer, queryDB.index)
            //重新获取当前数据库中存储的值
            val fileDB = EvidenceHelper.query(queryDB.baoquan)
            if (null != fileDB) {
                //下标+1，开始切下一块
                fileDB.index = fileDB.index + 1
                if (fileDB.index < queryDB.total) {
                    //获取下一块分片,并且记录
                    val nextTmp = EvidenceHelper.split(fileDB)
                    fileDB.filePointer = nextTmp.fileSize
                    //刷新间隔大于5秒,不能太过频繁
                    if (currentTimeNano - lastRefreshTime > 5000L) {
                        post(1, baoquan_no, (fileDB.index / queryDB.total).toSafeInt())
                        lastRefreshTime = currentTimeNano
                    }
                    //再开启下一次传输
                    suspendingUpload(fileDB, nextTmp.filePath.orEmpty(), fileType, baoquan_no, isZip)
                } else if (fileDB.index >= queryDB.total) {
//                    loadHttp(request = {
//                        SplitSubscribe.getPartCombineApi(HttpParams().append("baoquan_no", baoquan_no).map)
//                    }, end = {
//                    //删除源文件，清空表
//                    queryDB.sourcePath.deleteFile()
//                    EvidenceHelper.complete(queryDB.sourcePath, true)
//                    EvidenceHelper.delete(queryDB.sourcePath)
//                    EVENT_EVIDENCE_UPDATE.post(fileType)
//                    evidenceImpl?.get()?.onComplete(baoquan_no,true)
//                    })
                }
            }
            //失败
//            if (it?.second.toString() == "该保全号信息有误"){
//                tmpPath.deleteFile()
//                queryDB.sourcePath.deleteFile()
//                queryDB.sourcePath.delete()
////                LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_UPDATE, fileType), LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
//            } else EvidenceHelper.complete(queryDB.sourcePath, false)
            " \n————————————————————————文件上传-分片————————————————————————\n文件路径：${baoquan_no}::${queryDB.sourcePath}\n上传状态：失败\n失败原因：xxx\n————————————————————————文件上传-分片————————————————————————".logWTF
        }
    }

    @JvmStatic
    fun upload(baoquan_no: String, sourcePath: String, fileType: String, isZip: Boolean = false) {
        EvidenceHelper.insert(query(baoquan_no, sourcePath))
        post(0, baoquan_no)
        val mediaType = when (fileType) {
            "1" -> "image"
            "2" -> "audio"
            else -> if (isZip) "zip" else "video"
        }
        val paramsFile = File(sourcePath)
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("baoquan", baoquan_no)
        builder.addFormDataPart("file", paramsFile.name, paramsFile.asRequestBody(mediaType.toMediaTypeOrNull()))
        //成功
        sourcePath.deleteFile()
        EvidenceHelper.complete(baoquan_no, true)
        EvidenceHelper.delete(baoquan_no)
        //对应分类是拆开的，故而此时发送广播更新对应列表
        EVENT_EVIDENCE_UPDATE.post(fileType)
        post(2, baoquan_no, success = true)
        " \n————————————————————————文件上传————————————————————————\n文件路径：${baoquan_no}::${sourcePath}\n上传状态：成功\n————————————————————————文件上传————————————————————————".logWTF
        //失败
        EvidenceHelper.complete(baoquan_no)
        post(2, baoquan_no, success = false)
        " \n————————————————————————文件上传————————————————————————\n文件路径:${baoquan_no}::${sourcePath}\n上传状态：失败\n失败原因：xxxx\n————————————————————————文件上传————————————————————————".logWTF
        //完成....
        " \n————————————————————————文件上传————————————————————————\n上传完毕:${baoquan_no}::${sourcePath}\n————————————————————————文件上传————————————————————————".logWTF
    }

    /**
     * 查询数据，如果未查到或未创建，则自己创建一条
     */
    private fun query(baoquan_no: String, sourcePath: String): EvidenceDB {
        return EvidenceHelper.query(baoquan_no) ?: EvidenceDB(baoquan_no, sourcePath, getUserId(), 0, 0, true, false)
    }

    /**
     * 接口回调
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

}

interface EvidenceImpl {
    fun onStart(baoquan_no: String)//更新某个item的状态（开始上传）
    fun onLoading(baoquan_no: String, progress: Int)//更新某个item的进度
    fun onComplete(baoquan_no: String, success: Boolean)
}