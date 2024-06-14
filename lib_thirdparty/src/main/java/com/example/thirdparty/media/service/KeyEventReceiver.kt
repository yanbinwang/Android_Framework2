package com.example.thirdparty.media.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.example.common.event.EventCode.EVENT_MEDIA_ACTION
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
            //检测是否调用录屏（并不准确）
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent = intent.extras?.get(Intent.EXTRA_KEY_EVENT) as? KeyEvent
                if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_RECORD) EVENT_MEDIA_ACTION.post()
            }
        }
    }
}