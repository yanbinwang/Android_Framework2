package com.example.thirdparty.oss

import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.deleteFile
import com.example.common.utils.helper.AccountHelper.getUserId
import com.example.framework.utils.function.value.safeSize
import com.example.greendao.bean.OssDB
import com.example.greendao.dao.OssDBDao

/**
 * OSS帮助类
 */
object OssDBHelper {
    private var dao: OssDBDao? = null

    // <editor-fold defaultstate="collapsed" desc="数据库基础增删改查">
    /**
     * application中调取
     */
    @JvmStatic
    fun init(dao: OssDBDao) {
        OssDBHelper.dao = dao
    }

    /**
     * 查询当前用户本机数据库存储的所有集合
     */
    @JvmStatic
    fun query(): MutableList<OssDB>? {
        return try {
            dao?.queryBuilder()?.where(OssDBDao.Properties.UserId.eq(getUserId()))?.list()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 查询对应保全号的具体文件信息
     */
    @JvmStatic
    fun query(baoquan: String): OssDB? {
        return try {
            dao?.queryBuilder()?.where(OssDBDao.Properties.Baoquan.eq(baoquan), OssDBDao.Properties.UserId.eq(getUserId()))?.unique()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 插入对应数据
     */
    @JvmStatic
    fun insert(bean: OssDB?) {
        bean ?: return
        dao?.insertOrReplace(bean)
    }

    /**
     * 删除对应baoquan数据
     */
    @JvmStatic
    fun delete(baoquan: String) {
        dao?.deleteByKey(baoquan)
    }

    /**
     * 删除对应model数据
     */
    @JvmStatic
    fun delete(bean: OssDB?) {
        bean ?: return
        dao?.delete(bean)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="项目操作方法">
    /**
     * 更新数据库中所有数据的上传状态
     */
    @JvmStatic
    fun addObserver(observer: LifecycleOwner) {
        //以main为底座，绑定main的生命周期
        OssFactory.instance.cancelAllWork(observer)
        //加载数据前，让数据库中所有上传中状态的数据，变为未上传
        updateAll(1)
    }

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
     * 更新文件上传状态
     * 0上传中 1上传失败 2上传完成（证据缺失直接校验源文件路径）
     */
    @JvmStatic
    fun update(baoquan: String, state: Int = 2) {
        val bean = query(baoquan)
        bean ?: return
        bean.state = state
        dao?.update(bean)
    }

    /**
     * 更新所有文件的上传状态（登录成功后调取一次）
     */
    @JvmStatic
    fun updateAll(state: Int = 1) {
        //获取所有任务
        val allTasks = query().orEmpty()
        //批量修改任务属性
        for (task in allTasks) {
            //批量保存修改后的任务
            task.state = state
            dao?.update(task)
        }
    }

    /**
     * 文件是否正在上传
     */
    @JvmStatic
    fun isUpload(baoquan: String): Boolean {
        val bean = query(baoquan)
        bean ?: return false
        return bean.state == 0
    }

    /**
     * 文件是否完成上传
     */
    @JvmStatic
    fun isComplete(baoquan: String): Boolean {
        val bean = query(baoquan)
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