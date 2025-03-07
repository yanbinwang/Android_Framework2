package com.example.thirdparty.part

import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.deleteFile
import com.example.common.utils.function.write
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.logWTF
import com.example.greendao.bean.PartDB
import com.example.greendao.dao.PartDBDao
import com.example.thirdparty.part.bean.PartBean
import java.io.File

/**
 * 文件分片数据库帮助类
 */
object PartDBHelper {
    private var dao: PartDBDao? = null

    // <editor-fold defaultstate="collapsed" desc="数据库基础增删改查">
    /**
     * application中调取
     */
    fun init(dao: PartDBDao) {
        PartDBHelper.dao = dao
    }

    /**
     * 查询当前用户本机数据库存储的所有集合
     */
    @JvmStatic
    fun query(): MutableList<PartDB>? {
        return try {
            dao?.queryBuilder()?.where(PartDBDao.Properties.UserId.eq(getUserId()))?.list()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 查询对应保全号的具体文件信息
     */
    @JvmStatic
    fun query(baoquan_no: String): PartDB? {
        return try {
            dao?.queryBuilder()?.where(PartDBDao.Properties.Baoquan_no.eq(baoquan_no), PartDBDao.Properties.UserId.eq(getUserId()))?.unique()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 插入对应数据
     */
    @JvmStatic
    fun insert(bean: PartDB?) {
        bean ?: return
        dao?.insertOrReplace(bean)
    }

    /**
     * 删除对应baoquan数据
     */
    @JvmStatic
    fun delete(baoquan_no: String) {
        dao?.deleteByKey(baoquan_no)
    }

    /**
     * 删除对应model数据
     */
    @JvmStatic
    fun delete(bean: PartDB?) {
        bean ?: return
        dao?.delete(bean)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="项目操作方法">
    /**
     * 整理数据库对应用户的数据
     * 1.服务器请求完成后-》 val existsList = data.list.filter { it.isExists() }//整理服务器给的对照列表，抓出其中本地具备的文件
     * 2.调取sort方法，传入服务器的对照列表，对数据库进行一次删减
     * 3.上述方法执行完毕后-》 val loseList = data.list.filter { !it.isExists() }//标记损坏的文件
     * 4.批量调取oss文件上传
     */
    @JvmStatic
    fun sort(serverList: MutableList<String>) {
        //查询手机内存储的集合，如果服务器内不存在这条数据，手机内也不应该存在
        val localList = query()
        for (bean in localList?.filter { !serverList.contains(it.sourcePath) }.orEmpty()) {
            //删除源文件和数据表值
            delete(bean)
            bean.sourcePath.deleteFile()
        }
    }

    /**
     * 外层先query查找对应数据库，没有找到值的话，重新insert，找到值的话，获取里面的内容
     */
    @JvmStatic
    fun split(bean: PartDB): PartBean {
        //获取切片源文件
        val targetFile = File(bean.sourcePath)
        //文件的总大小
        val length = targetFile.length()
        //确定切割的结尾
        var end = (bean.index + 1).times(bean.size)
        //如果当前是分片的最后一片，结尾为文件本身长度
        if (bean.index + 1 >= bean.total) end = length
        " \n文件大小：${length}\n切割大小：${bean.size}\n分片开头：${bean.filePointer}\n分片结尾：${end}".logWTF
        //返回切割好的信息
        val pair = write(bean.sourcePath, bean.index, bean.filePointer, end)
        return PartBean(pair.first, end)
    }

    /**
     * 更新所有文件的上传状态（登录成功后调取一次）
     */
    @JvmStatic
    fun updateUpload(isUpload: Boolean = false) {
        val list = query()
        list ?: return
        list.forEach { updateUpload(it.baoquan_no, isUpload) }
    }

    /**
     * 接口回调200成功存储此次断点和下标
     */
    @JvmStatic
    fun updateUpload(baoquan_no: String, filePointer: Long, index: Int) {
        val bean = query(baoquan_no)
        bean ?: return
        bean.filePointer = filePointer
        bean.index = index
        insert(bean)
    }

    /**
     * 开始上传文件
     */
    @JvmStatic
    fun updateUpload(baoquan_no: String, isUpload: Boolean = true) {
        val bean = query(baoquan_no)
        bean ?: return
        bean.state = if (isUpload) 0 else 1
        dao?.update(bean)
    }

    /**
     * 完成上传，通常此时这条数据已经被删除不存在了
     */
    @JvmStatic
    fun updateComplete(baoquan_no: String, isComplete: Boolean = true) {
        val bean = query(baoquan_no)
        bean ?: return
        bean.state = if (isComplete) 2 else 1
        dao?.update(bean)
    }

    /**
     * 更新数据库中所有数据的上传状态
     */
    @JvmStatic
    fun addObserver(observer: LifecycleOwner) {
        //以main为底座，绑定main的生命周期
        PartFactory.instance.cancelAllWork(observer)
        //加载数据前，让数据库中所有上传中状态的数据，变为未上传
        dao?.loadAll()?.forEach { updateUpload(it.baoquan_no, false) }
    }

    /**
     * 文件是否正在上传
     */
    @JvmStatic
    fun isUpload(baoquan_no: String): Boolean {
        val bean = query(baoquan_no)
        bean ?: return false
        return bean.state == 0
    }

    /**
     * 文件是否完成上传
     */
    @JvmStatic
    fun isComplete(baoquan_no: String): Boolean {
        val bean = query(baoquan_no)
        bean ?: return false
        return bean.state == 2
    }

    /**
     * 当前用户是否有正在提交的数据
     */
    @JvmStatic
    fun isSubmit(): Boolean {
        return submitNumber() > 0
    }

    /**
     * 当前上传数
     */
    @JvmStatic
    fun submitNumber(): Int {
        return query()?.filter { it.state == 0 }.safeSize
    }
    // </editor-fold>

}