package com.example.socket.config

import com.example.socket.bean.SocketBean

object SocketConfig {
    //socket地址
    const val DEAL_SOCKET_URL = "/user/topic/console/subscribe/pendingOrder"//訂單
    const val ADVERTISE_SOCKET_URL = "/user/topic/console/subscribe/entrustInProgress"//廣告
    const val FUNDS_SOCKET_URL = "/user/topic/console/subscribe/assetInfo"//資金

    /**
     * 0正式地址 1测试地址
     */
    var serverType = 0
    var servers = arrayListOf<SocketBean>()

    /**
     * BaseApplication初始化
     * master分支中serverType永远是0
     * develop或者release中可切换为别的地址编码
     */
    fun init() {
        serverType = 1
        servers = arrayListOf(
            SocketBean("user.cheezeebit.com", "api/ws_endpoint", "线上"),
            SocketBean("user-test-acceptor.91fafafa.com", "api/ws_endpoint", "测试")
        )
    }

    /**
     * 目前正在用的socket服务器地址
     */
    fun socketUrl(): String {
        return socketBean().getUrl()
    }

    /**
     * 目前正在用的服务器AppServerBean
     */
    fun socketBean(): SocketBean {
        return socketBeanDefault()
    }

    /**包内默认配置的的服务器AppServerBean*/
    fun socketBeanDefault(): SocketBean {
        return servers[serverType]
    }

}