package com.example.mvvm.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 *  Created by wangyanbin
 *  app整体启动页
 *  安卓本身bug会在初次安装应用后点击图标再次拉起启动页，造成界面显示不不正常
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}