package com.example.debugging.utils

import com.example.common.bean.ServerBean
import com.example.common.config.ServerConfig
import com.example.common.network.interceptor.LoggingInterceptor
import com.example.common.utils.DataStringCacheUtil
import com.example.common.utils.builder.shortToast
import com.example.common.utils.toJson
import com.example.common.utils.toList
import com.example.debugging.bean.RequestBean
import com.example.debugging.utils.DebuggingUtil.updateNotificationContent
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.value.toSafeInt
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

/**
 * 针对服务器切换/编辑的工具类
 */
object ServerUtil {
    /**
     * 当前服务器版本号+本地集合
     * 使用server_type::server_list_json的形式
     */
    private const val SERVER_DATA = "server_data"
    internal val serverData = DataStringCacheUtil(SERVER_DATA)

    /**
     * 网络请求列表
     */
    internal val requestList by lazy { AtomicReference(ArrayList<RequestBean>()) }

    /**
     * 初始化
     */
    @JvmStatic
    fun init() {
        //初始化服务器配置
        val data = serverData()
        val serverType = data.first
        val serverList = data.second
        ServerConfig.changeServerType(serverType, serverList.toArrayList())
        //请求拦截地址,只保留最近30个请求
        LoggingInterceptor.setOnDebuggingListener { header, method, url, params, code, body ->
            addRequest(url, method, header, params, code, body)
        }
    }

    /**
     * 因为debugging是继承自lib_thirdparty的，而lib_thirdparty继承common，
     * 所有的三方库以及主库都是能添加的，如有需要，可在三方库里扩展回调，debugging里获取并添加
     */
    private fun addRequest(url: String? = null, method: String? = null, header: String? = null, params: String? = null, code: Int? = null, body: String? = null) {
        val bean = RequestBean(url, method, header, params, Date().time, code, body)
        // 获取当前列表
        val currentList = requestList.get()
        // 在列表头部插入元素
        currentList.add(0, bean)
        // 检查列表长度是否超过 30
        if (currentList.safeSize > 30) {
            // 截取前 30 个元素
            val newList = ArrayList<RequestBean>(currentList.subList(0, 30))
            // 替换原列表
            currentList.clear()
            currentList.addAll(newList)
        }
    }

    /**
     * 添加一组新的服务器接口
     */
    fun addServer(server: String = "", port: Int = 0, path: String = "", name: String = "", https: Boolean = false) {
        val data = serverData()
        val serverType = data.first
        val serverList = data.second
        if (serverList.find { it.server == server && it.port == port && it.path == path && it.name == name && it.https == https } == null) {
            serverList.toArrayList().add(ServerBean(server, port, path, name, https))
            serverData.set("${serverType}::${serverList.toJson()}")
            "添加成功".shortToast()
        } else {
            "添加失败，已有相同地址".shortToast()
        }
    }

    /**
     * 获取当前存储的服务器数据
     */
    fun serverData(): Pair<Int, List<ServerBean>> {
        var serverType: Int //当前选中的服务器地址
        var serverList: List<ServerBean>
        serverData.get().let {
            //本地没有存储值的时候，第0个就是测试的第一个地址
            if (it.isNullOrEmpty()) {
                serverType = 0
                serverList = ServerConfig.servers.get().drop(1)//线上地址排除
                serverData.set("${serverType}::${serverList.toJson()}")
            } else {
                //存值的话，取对应的值和集合
                val list = it.split("::")
                serverType = list.safeGet(0).toSafeInt()
                serverList = list.safeGet(1).toList(ServerBean::class.java).orEmpty()
            }
        }
        return serverType to serverList
    }

    /**
     * 修改请求地址
     */
    fun changeServer(newType: Int) {
        val data = serverData()
        val serverList = data.second
        val serverBean = serverList.safeGet(newType)
        ServerConfig.changeServerType(newType, serverList.toArrayList())
        updateNotificationContent("本程序包为 " + serverBean?.name + " 包")
    }

    /**
     * 还原为原始数据
     */
    fun resetServer() {
        ServerConfig.init()
        val serverList = ServerConfig.servers.get().drop(1)
        serverData.set("0::${serverList.toJson()}")
        ServerConfig.changeServerType(0, serverList.toArrayList())
    }

}