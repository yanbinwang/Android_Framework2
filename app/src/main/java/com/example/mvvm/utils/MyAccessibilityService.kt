package com.example.mvvm.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.framework.utils.logWTF


/**
 * AccessibilityService (无障碍服务)
 * 主要用于两类场景：
 * 1)是为残障用户提供无障碍支持（核心设计目标）
 * 2)是实现特定自动化或辅助功能（需注意合规性）
 * <service
 *     android:name=".MyAccessibilityService"
 *     android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
 *     <intent-filter>
 *         <action android:name="android.accessibilityservice.AccessibilityService" />
 *     </intent-filter>
 *
 *     <!-- 配置服务参数 -->
 *     <meta-data
 *         android:name="android.accessibilityservice"
 *         android:resource="@xml/accessibility_config" />
 * </service>
 *
 * isAccessibilityServiceEnabled->扩展函数,记得开启页面授权
 *
 * 拿到权限后,启动app会自动挂载,没拿到的情况下,跳转授权后,回来就会挂载
 */
class MyAccessibilityService : AccessibilityService() {
    private val TAG = "Accessibility"

    /**
     * 服务连接成功时调用
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        // 可以在这里初始化一些配置
        "服务已连接".logWTF(TAG)
    }

    /**
     * 接收无障碍事件时调用
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 事件类型
        val eventType = event?.eventType
        // 获取事件来源的应用包名
        val packageName = event?.packageName
        // 根据事件类型处理
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "点击事件：$packageName".logWTF(TAG)
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "窗口变化：$packageName".logWTF(TAG)
        }
//        // 获取事件源的视图节点信息
//        val nodeInfo = event?.source
//        if (nodeInfo == null) {
//            return
//        }
//        // 遍历所有子节点，查找文本为"确定"的按钮并点击
//        val nodes = nodeInfo.findAccessibilityNodeInfosByText("确定")
//        for (node in nodes) {
//            // 检查节点是否可点击
//            if (node.isClickable) {
//                // 执行点击操作
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                "自动点击了确定按钮".logWTF(TAG)
//                break
//            }
//        }
//        // 回收资源
//        nodeInfo.recycle()
    }

    /**
     * 服务被中断时调用
     */
    override fun onInterrupt() {
        "服务被中断".logWTF(TAG)
    }

}