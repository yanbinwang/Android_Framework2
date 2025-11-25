package com.example.thirdparty.media.oss

import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.function.deleteFile
import com.example.common.utils.function.getAllFilePaths
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.toNewList
import com.example.objectbox.dao.OssDB
import com.example.objectbox.dao.OssDB_
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder.StringOrder
import java.io.File

/**
 * OSS帮助类
 * application中调用MyObjectBox.builder().androidContext(context).build()
 */
object OssDBHelper {
    private var dao: Box<OssDB>? = null

    // <editor-fold defaultstate="collapsed" desc="数据库基础增删改查">
    /**
     * application中调取
     */
    @JvmStatic
    fun init(store: BoxStore) {
        dao = store.boxFor(OssDB::class.java)
    }

    /**
     * 查询当前用户本机数据库存储的所有集合
     */
    @JvmStatic
    fun query(): MutableList<OssDB>? {
        return try {
            dao?.query()
                ?.equal(OssDB_.userId, AccountHelper.getUserId(), StringOrder.CASE_SENSITIVE)
                ?.build()
                ?.find()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 查询对应保全号的具体文件信息
     */
    @JvmStatic
    fun query(baoquan: String?): OssDB? {
        baoquan ?: return null
        return try {
            getOssDBByBaoquan(baoquan)?.findUnique()
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
        dao?.put(bean)
    }

    /**
     * 删除对应baoquan数据
     */
    @JvmStatic
    fun delete(baoquan: String?) {
        baoquan ?: return
        try {
            getOssDBByBaoquan(baoquan)?.remove()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 删除对应bean数据
     */
    @JvmStatic
    fun delete(bean: OssDB?) {
        bean ?: return
        dao?.remove(bean)
    }

    /**
     * 针对当前用户的保全号的删除
     */
    private fun getOssDBByBaoquan(baoquan: String?): Query<OssDB>? {
        baoquan ?: return null
        return try {
            dao?.query()
                ?.equal(OssDB_.userId, AccountHelper.getUserId(), StringOrder.CASE_SENSITIVE)
                ?.equal(OssDB_.baoquan, baoquan, StringOrder.CASE_SENSITIVE)
                ?.build()
        } catch (e: Exception) {
            null
        }
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
        //加载数据前，让数据库中所有上传中状态的数据，变为未上传（上传失败）
        updateAll(1)
    }

    /**
     * 整理数据库对应用户的数据
     * 1.服务器请求完成后-》 val existsList = data.list.filter { it.isExists() }//整理服务器给的对照列表，抓出其中本地具备的文件
     * 2.调取sort方法，传入服务器的对照列表，对数据库进行一次删减
     * 3.上述方法执行完毕后-》 val loseList = data.list.filter { !it.isExists() }//标记损坏的文件
     * 4.批量调取oss文件上传
     */
//    @JvmStatic
//    fun sort(serverList: MutableList<String>) {
//        // 查询手机内存储的集合，如果服务器内不存在这条数据，手机内也不应该存在
//        val localList = query()
//        for (bean in localList?.filter { !serverList.contains(it.sourcePath) }.orEmpty()) {
//            // 删除源文件和数据表值
//            delete(bean)
//            bean.sourcePath.deleteFile()
//        }
//    }
    @JvmStatic
    fun sort(data: MutableList<Pair<String, String>>) {
        // 取出服务器中包含的所有文件详细路径集合(本地会做拼接)
        val serverAllSet = data.toNewList { it.second }.toSet()
        // 查询手机本地数据库存储的所有文件数据
        val localDbList = query()
        // 清除本地数据库内不存在于服务器的脏数据
        for (bean in localDbList?.filter { !serverAllSet.contains(it.sourcePath) }.orEmpty()) {
            // 删除源文件和数据表值
            delete(bean)
            bean.sourcePath.deleteFile()
        }
        // 本地源文件脏数据清理 1.拍照取证，2.录音取证，3.录像取证，4.录屏取证
        listOf("1", "2", "3", "4").forEach { appType ->
            // 获取对应证据目录下所有文件
            val localList = File(getStoragePath("${
                when (appType) {
                    "1" -> "拍照"
                    "2" -> "录音"
                    "3" -> "录像"
                    else -> "录屏"
                }
            }取证")).getAllFilePaths()
            // 筛选服务器对应目录的证据路径集合
            val serverTypeSet = data
                .filter { it.first == appType }
                .map { it.second }
                .toSet()
            // 删除本地存在但服务器不存在的文件
            for (bean in localList.filter { !serverTypeSet.contains(it) }) {
                bean.deleteFile()
            }
        }
    }

    /**
     * 更新文件上传状态
     * 0上传中 1上传失败 2上传完成（证据缺失直接校验源文件路径）
     */
    @JvmStatic
    fun update(baoquan: String?, state: Int = 2) {
        val bean = query(baoquan)
        bean ?: return
        bean.state = state
        dao?.put(bean)
    }

    /**
     * 更新所有文件的上传状态（登录成功后调取一次）
     */
    @JvmStatic
    fun updateAll(state: Int = 1) {
        //获取所有任务
        val allTasks = dao?.all.orEmpty()
        //批量修改任务属性
        for (task in allTasks) {
            task.state = state
        }
        //批量保存修改后的任务
        dao?.put(allTasks)
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