package com.example.thirdparty.greendao.utils

import com.example.common.utils.file.FileUtil
import com.example.common.utils.file.FileUtil.TmpInfo
import com.example.common.utils.file.deleteFile
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.common.utils.helper.AccountHelper.storage
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.logE
import com.example.thirdparty.greendao.bean.EvidenceDB
import com.example.thirdparty.greendao.dao.EvidenceDBDao
import java.io.File

object EvidenceHelper {
    //    private val dao by lazy { BaseApplication.instance?.daoSession!!.mobileFileDBDao }
    private var dao: EvidenceDBDao? = null

    // <editor-fold defaultstate="collapsed" desc="数据库基础增删改查">
    fun init(dao: EvidenceDBDao) {
        this.dao = dao
    }

    //查询当前用户本机数据库存储的所有集合
    @JvmStatic
    fun query(): MutableList<EvidenceDB>? {
        return try {
            dao?.queryBuilder()?.where(EvidenceDBDao.Properties.UserId.eq(getUserId()))?.list()
        } catch (e: Exception) {
            null
        }
    }

    //查询对应保全号的具体信息
    @JvmStatic
    fun query(baoquan: String): EvidenceDB? {
        return try {
            dao?.queryBuilder()?.where(
                EvidenceDBDao.Properties.Baoquan.eq(baoquan),
                EvidenceDBDao.Properties.UserId.eq(getUserId())
            )?.unique()
        } catch (e: Exception) {
            null
        }
    }

    //插入对应数据
    @JvmStatic
    fun insert(bean: EvidenceDB?) {
        bean ?: return
        dao?.insertOrReplace(bean)
    }

    //删除对应sourcePath数据
    @JvmStatic
    fun delete(baoquan: String) {
        dao?.deleteByKey(baoquan)
    }

    //删除对应model数据
    @JvmStatic
    fun delete(bean: EvidenceDB?) {
        bean ?: return
        dao?.delete(bean)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="项目操作方法">
    //整理数据库对应用户的数据
    @JvmStatic
    fun sort(serverList: MutableList<String>) {
        //查询手机内存储的集合
        val localList = query()
        for (bean in localList?.filter { !serverList.contains(it.sourcePath) }.orEmpty()) {
            //删除源文件和数据表值
            delete(bean)
            bean.sourcePath.deleteFile()
        }
    }

    //获取对应文件在手机内的路径
    @JvmStatic
    fun sourcePath(appType: String, title: String): String {
        return "${storage}${
            when (appType) {
                "1" -> "拍照"
                "2" -> "录音"
                "3" -> "录像"
                else -> "录屏"
            }
        }/${title}"
    }

    //外层先query查找对应数据库，没有找到值的话，重新insert，找到值的话，获取里面的内容
    @JvmStatic
    fun split(fileDB: EvidenceDB): TmpInfo {
        //获取切片源文件
        val targetFile = File(fileDB.sourcePath)
        //文件的总大小
        val length = targetFile.length()
        //确定切割的结尾
        var end = (fileDB.index + 1).times(fileDB.size)
        //如果当前是分片的最后一片，结尾为文件本身长度
        if (fileDB.index + 1 >= fileDB.total) end = length
        " \n文件大小：${length}\n切割大小：${fileDB.size}\n分片开头：${fileDB.filePointer}\n分片结尾：${end}".logE
        //返回切割好的信息
        val tmp = FileUtil.write(fileDB.sourcePath, fileDB.index, fileDB.filePointer, end)
        tmp.fileSize = end
        return tmp
    }

    //接口回调200成功存储此次断点和下标
    @JvmStatic
    fun update(baoquan: String, filePointer: Long, index: Int) {
        val bean = query(baoquan)
        bean?.filePointer = filePointer
        bean?.index = index
        insert(bean)
    }

    //开始上传文件
    @JvmStatic
    fun update(baoquan: String, isUpload: Boolean = true) {
        val bean = query(baoquan)
        bean?.isUpload = isUpload
        dao?.update(bean)
    }

    //更新所有文件的上传状态
    @JvmStatic
    fun updateAll(isUpload: Boolean = false) {
        val daoList = query()
        for (bean in daoList.orEmpty()) {
            update(bean.sourcePath, isUpload)
        }
    }

    //标记文件此时的状态
    @JvmStatic
    fun complete(baoquan: String, isComplete: Boolean = false) {
        val bean = query(baoquan)
        bean?.isComplete = isComplete
        bean?.isUpload = false
        dao?.update(bean)
    }

    //文件是否正在上传
    @JvmStatic
    fun isUpload(baoquan: String): Boolean {
        val bean = query(baoquan)
        return bean?.isUpload.orFalse
    }

    //文件是否上传完成
    @JvmStatic
    fun isComplete(baoquan: String): Boolean {
        val model = query(baoquan)
        return model?.isComplete.orTrue
    }
    // </editor-fold>
}