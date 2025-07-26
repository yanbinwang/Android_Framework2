package com.example.gallery.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.mvp.Bye
import com.yanzhenjie.album.util.AlbumUtils

/**
 * 相册-基类
 */
abstract class BaseActivity : AppCompatActivity(), Bye {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Album.getAlbumConfig().locale
        AlbumUtils.applyLanguageForContext(this, locale)
    }

    override fun bye() {
        onBackPressed()
    }

}