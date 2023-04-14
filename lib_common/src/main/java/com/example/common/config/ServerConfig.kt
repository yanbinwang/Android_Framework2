package com.example.common.config

import com.example.common.bean.ServerBean

/**
 * @description 服务器配置类
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
    fun init() {
        serverType = 1
        servers = arrayListOf(
            ServerBean("user.cheezeebit.com", -1, "", "线上").https(),
            ServerBean("user-test-acceptor.91fafafa.com", -1, "", "测试").https()
        )
    }

    /**
     * 包内默认配置的的服务器ServerBean
     */
    fun serverBeanDefault(): ServerBean {
        return servers[serverType]
    }

    /**
     * 目前正在用的服务器地址
     */
    fun serverUrl(): String {
        return serverBeanDefault().getUrl()
    }

    /**
     * 服务器名称
     */
    fun serverName(): String {
        return servers[serverType].name
    }

    /**
     * 服务器序号
     */
    fun serverType(): Int {
        return serverType
    }

    /**
     * 是否是测试地址
     */
    fun isTest(): Boolean {
        return serverType != 0
    }

}