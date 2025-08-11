package com.example.thirdparty.media.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.example.common.event.EventCode.EVENT_MENU_ACTION

/**
 *  Created by wangyanbin
 *  按键广播
 */
class KeyEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            /**
             * 请求关闭临时系统对话框广播
             * 接收 ACTION_CLOSE_SYSTEM_DIALOGS 广播不需要申请 BROADCAST_CLOSE_SYSTEM_DIALOGS 权限（该权限仅用于发送此广播），接收时无需额外权限。
             */
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                /**
                 * "homekey"	用户按下 Home 键（返回桌面）
                 * "recentapps"	用户按下 最近任务键（多任务键，调出最近使用的应用列表）
                 * "lock"	用户按下 电源键锁屏 或系统自动锁屏（部分设备）
                 * "voice"	触发 语音助手（如长按 Home 键唤醒 Google Assistant）
                 * "assist"	触发系统辅助功能（与语音助手类似，部分设备统一用此值）
                 * "global_action"	执行系统全局动作（如通过通知栏快捷方式触发的飞行模式、亮度调节等）
                 * "dream"	进入 / 退出 屏幕保护模式（Daydream，部分老版本系统）
                 * "accessibility"	辅助功能相关操作触发（如通过辅助服务执行的系统交互）
                 */
                when (intent.getStringExtra("reason")) {
                    "homekey", "recentapps" -> EVENT_MENU_ACTION.post()
                }
            }
            // 电源键广播
            Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON -> EVENT_MENU_ACTION.post()
            // 多媒体按键广播
            Intent.ACTION_MEDIA_BUTTON -> {
                /**
                 * KEYCODE_MEDIA_PLAY 多媒体键 播放
                 * KEYCODE_MEDIA_STOP 多媒体键 停止
                 * KEYCODE_MEDIA_PAUSE 多媒体键 暂停
                 * KEYCODE_MEDIA_PLAY_PAUSE 多媒体键 播放/暂停
                 * KEYCODE_MEDIA_FAST_FORWARD 多媒体键 快进
                 * KEYCODE_MEDIA_REWIND 多媒体键 快退
                 * KEYCODE_MEDIA_NEXT 多媒体键 下一首
                 * KEYCODE_MEDIA_PREVIOUS 多媒体键 上一首
                 * KEYCODE_MEDIA_CLOSE 多媒体键 关闭
                 * KEYCODE_MEDIA_EJECT 多媒体键 弹出
                 * KEYCODE_MEDIA_RECORD 多媒体键 录音
                 */
                val keyEvent = intent.extras?.get(Intent.EXTRA_KEY_EVENT) as? KeyEvent
//                if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_RECORD) EVENT_MEDIA_ACTION.post()
            }
        }
    }
}