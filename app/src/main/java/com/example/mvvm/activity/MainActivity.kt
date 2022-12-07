package com.example.mvvm.activity

import android.content.Intent
import android.preference.PreferenceManager.OnActivityResultListener
import android.provider.MediaStore
import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.clicks
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.common.constant.RequestCode.REQUEST_MANAGER
import com.example.common.constant.RequestCode.REQUEST_PHOTO
import com.example.common.utils.builder.shortToast
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding


@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {

    override fun initEvent() {
        super.initEvent()
        clicks(binding.btnFileManager, binding.btnAlbum)
        onActivityResultListener = object :OnActivityResultListener{
            override fun onActivityResult(
                requestCode: Int,
                resultCode: Int,
                data: Intent?
            ): Boolean {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //-------常用类型
            //图片
            //intent.setType(“image/*”);
            //音频
            //intent.setType(“audio/*”);
            //视频
            //intent.setType(“video/*”);
            //intent.setType(“video/*;image/*”);
            R.id.btn_file_manager -> {
//                val intent = Intent(Intent.ACTION_GET_CONTENT)
//                //任意类型文件
//                intent.type = "*/*"
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                startActivityForResult(intent, REQUEST_MANAGER)
                navigation(ARouterPath.TestActivity,Extras.REQUEST_CODE to REQUEST_PHOTO)
            }
            R.id.btn_album -> {
                val intent = Intent(Intent.ACTION_PICK, null)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                startActivityForResult(intent, REQUEST_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_MANAGER -> {
                    //...
                }
                REQUEST_PHOTO -> {
                    //...
                }
            }
        }
    }

}