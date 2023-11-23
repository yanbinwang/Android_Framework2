package com.example.thirdparty.greendao.utils

import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.file.FileHelper
import com.example.common.utils.file.deleteFile
import com.example.common.utils.file.getSizeFormat
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.doOnDestroy
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
import kotlin.coroutines.CoroutineContext

object EvidenceExecutors : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    @JvmStatic
    fun addObserver(observer: LifecycleOwner) {
        observer.doOnDestroy { job.cancel() }
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

    private fun partUpload(
        queryDB: EvidenceDB,
        tmpPath: String,
        fileType: String,
        baoquan_no: String,
        isZip: Boolean = false
    ) {
        launch {

        }
    }

    private suspend fun suspendingSplit(baoquan_no: String, sourcePath: String): EvidenceDB {
        return withContext(IO) {
            //查询/创建一条用于存表的数据，并重新插入一次
            val queryDB = query(baoquan_no, sourcePath)
            EvidenceHelper.insert(queryDB)
            //先将当前查询/创建的数据在未上传列表内刷出来，文件分片需要一些时间
            EvidenceHelper.update(sourcePath, true)
//            LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
            //开始分片，并获取分片信息
            val tmp = EvidenceHelper.split(queryDB)
            queryDB.filePointer = tmp.fileSize
            queryDB
        }
    }

    private suspend fun suspendingUpload(queryDB: EvidenceDB, tmpPath: String, fileType: String, baoquan_no: String, isZip: Boolean = false){
        withContext(IO){
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
//                    LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
                    //再开启下一次传输
                    partUpload(fileDB, nextTmp.filePath.orEmpty(), fileType, baoquan_no, isZip)
                } else if (fileDB.index >= queryDB.total) {
//                    loadHttp(request = {
//                        SplitSubscribe.getPartCombineApi(HttpParams().append("baoquan_no", baoquan_no).map)
//                    }, end = {
//                        //删除源文件，清空表
//                        FileUtil.deleteFile(queryDB.sourcePath)
//                        FileHelper.complete(queryDB.sourcePath, true)
//                        FileHelper.delete(queryDB.sourcePath)
//                        LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_UPDATE, fileType), LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
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
//        LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
        val mediaType = when (fileType) {
            "1" -> "image"
            "2" -> "audio"
            else -> if (isZip) "zip" else "video"
        }
        val paramsFile = File(sourcePath)
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("baoquan", baoquan_no)
        builder.addFormDataPart(
            "file",
            paramsFile.name,
            paramsFile.asRequestBody(mediaType.toMediaTypeOrNull())
        )
        //成功
        sourcePath.deleteFile()
        EvidenceHelper.complete(baoquan_no, true)
        EvidenceHelper.delete(baoquan_no)
//        LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_UPDATE, fileType))
        " \n————————————————————————文件上传————————————————————————\n文件路径：${baoquan_no}::${sourcePath}\n上传状态：成功\n————————————————————————文件上传————————————————————————".logWTF
        //失败
        EvidenceHelper.complete(baoquan_no)
        " \n————————————————————————文件上传————————————————————————\n文件路径:${baoquan_no}::${sourcePath}\n上传状态：失败\n失败原因：xxxx\n————————————————————————文件上传————————————————————————".logWTF
        //完成
//        LiveDataBus.instance.post(LiveDataEvent(Constants.APP_EVIDENCE_EXTRAS_UPDATE))
        " \n————————————————————————文件上传————————————————————————\n上传完毕:${baoquan_no}::${sourcePath}\n————————————————————————文件上传————————————————————————".logWTF
    }

    private fun query(baoquan_no: String, sourcePath: String): EvidenceDB {
        var bean = EvidenceHelper.query(baoquan_no)
        if (bean == null) bean =
            EvidenceDB(
                baoquan_no,
                sourcePath,
                getUserId(),
                0,
                0,
                true,
                false
            )
        return bean
    }
}