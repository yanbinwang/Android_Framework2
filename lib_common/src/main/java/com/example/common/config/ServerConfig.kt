package com.example.common.config

import com.example.common.bean.ServerBean

/**
 * @description 服务器配置类（可在master分支中，将serverType设为0，develop或release设为别的服务器类，避免上架合并分支接口地址获取错误）
 * @author yan
 */
object ServerConfig {
    /**
     * 0正式地址 1测试地址
     */
    var serverType = 0
    var servers = arrayListOf<ServerBean>()

    /**
     * BaseApplication初始化
     * master分支中serverType永远是0
     * develop或者release中可切换为别的地址编码
     */
    @JvmStatic
    fun init() {
        serverType = 1
        servers = arrayListOf(
            ServerBean("user.cheezeebit.com", -1, "", "线上").https(),
            ServerBean("user-test-acceptor.91fafafa.com", -1, "", "测试").https()
        )
    }

    /**
     * 是否是测试地址
     */
    @JvmStatic
    fun isTest(): Boolean {
        return serverType != 0
    }

    /**
     * 包内默认配置的的服务器ServerBean
     */
    @JvmStatic
    fun serverBean(): ServerBean {
        return servers[serverType]
    }

    /**
     * 目前正在用的服务器地址
     */
    @JvmStatic
    fun serverUrl(): String {
        return serverBean().getUrl()
    }

    /**
     * 服务器名称
     */
    @JvmStatic
    fun serverName(): String {
        return serverBean().name
    }

    /**
     * 服务器序号
     */
    fun serverType(): Int {
        return serverType
    }

}