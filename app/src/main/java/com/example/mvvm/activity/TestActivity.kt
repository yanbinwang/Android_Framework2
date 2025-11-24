package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.insertImageResolver
import com.example.common.utils.function.isPathExists
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityTestBinding
import com.example.thirdparty.media.utils.CameraHelper
import com.therouter.router.Route
import java.io.File

/**
 * https://blog.csdn.net/YllP_1230/article/details/130317459
 */
@Route(path = RouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityTestBinding>() ,OnClickListener{
    private val camera by lazy { CameraHelper(this) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding?.titleRoot?.setLeftButton(R.mipmap.ic_btn_back2)?.bind(this)
        camera.bind(mBinding?.camera)
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.ivTake, mBinding?.flFlash, mBinding?.flSwitch)
        camera.setOnTakePictureListener(object : CameraHelper.OnTakePictureListener {
            override fun onShutter() {
                mBinding?.mask?.onShutter()
                mBinding?.ivTake.disable()
            }

            override fun onTaken(sourcePath: String?) {
                if (sourcePath.isPathExists()) {
                    insertImageResolver(File(sourcePath.orEmpty()))
                    "拍摄完成".shortToast()
                } else {
                    R.string.responseError.shortToast()
                }
                mBinding?.ivTake.enable()
            }

            override fun onFlash(isOpen: Boolean) {
                mBinding?.ivFlash.background(if (isOpen) R.mipmap.ic_flash_on else R.mipmap.ic_flash_off)
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_take -> camera.takePicture()
            R.id.fl_flash -> camera.flash()
            R.id.fl_switch -> camera.toggleFacing()
        }
    }

}