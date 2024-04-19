package com.example.mvvm.activity

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.common.base.BaseActivity
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ActivitySimpleBinding
import com.example.mvvm.widget.dialog.FingerDialog

@RequiresApi(Build.VERSION_CODES.M)
class SimpleActivity : BaseActivity<ActivitySimpleBinding>() {
    private val finger by lazy { FingerDialog(this) }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initEvent() {
        super.initEvent()
        mBinding?.btnRecognition.click {
            finger.startAuthenticate()
        }
        finger.setOnFingerListener {

        }
    }

}