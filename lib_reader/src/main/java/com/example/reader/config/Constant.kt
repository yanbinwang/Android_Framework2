package com.example.reader.config

import com.example.common.BaseApplication

object Constant {
    val EPUB_SAVE_PATH get() = BaseApplication.instance.applicationContext.filesDir?.absolutePath + "/epubFile"
}