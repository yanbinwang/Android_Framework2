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
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                //Home键,菜单键
                when (intent.getStringExtra("reason")) {
                    "homekey", "recentapps" -> EVENT_MENU_ACTION.post()
                }
            }
            //电源键
            Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON -> EVENT_MENU_ACTION.post()
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
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = intent.extras?.get(Intent.EXTRA_KEY_EVENT) as? KeyEvent
//                if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_RECORD) EVENT_MEDIA_ACTION.post()
            }
        }
    }
}