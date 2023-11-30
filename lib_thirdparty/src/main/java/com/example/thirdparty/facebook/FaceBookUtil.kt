package com.example.thirdparty.facebook

import android.app.Application
import com.facebook.FacebookSdk

/**
 * 脸书工具栏
 */
object FaceBookUtil {
    fun initMultiProcess(application: Application){
        FacebookSdk.sdkInitialize(application)
    }
}