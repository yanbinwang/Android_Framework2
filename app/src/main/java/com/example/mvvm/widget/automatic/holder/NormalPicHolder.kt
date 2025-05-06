package com.example.mvvm.widget.automatic.holder

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getFileFromUri
import com.example.common.utils.permission.PermissionHelper
import com.example.common.widget.dialog.LoadingDialog
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ViewNormalPicBinding
import com.example.mvvm.widget.automatic.AutomaticBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * @description 自动绘制图片选择
 * @author yan
 */
class NormalPicHolder(private val activity: AppCompatActivity, private val bean: AutomaticBean) : CoroutineScope, LifecycleEventObserver, AutomaticInterface {
//    private val binding by lazy { ViewNormalPicBinding.bind(activity.inflate(R.layout.view_normal_edit)) }
    private val binding by lazy { ViewNormalPicBinding.inflate(activity.layoutInflater) }
    private val loadingDialog by lazy { LoadingDialog(activity) }
    private val permission by lazy { PermissionHelper(activity) }
    private val activityResultValue = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it?.data ?: return@registerForActivityResult
            val uri = it.data?.data
            uri.getFileFromUri()?.absolutePath.shortToast()
        }
    }
    private var value = ""
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        activity.lifecycle.addObserver(this)
        binding.tvLabel.text = bean.label
        if (bean.enable.orFalse) {
            binding.flContent.click {
                permission.requestPermissions {
                    if (it) {
                        val intent = Intent(Intent.ACTION_PICK, null)
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                        activityResultValue.launch(intent)
                    }
                }
            }
        }
    }

    override fun getBean() = bean

    override fun getValue() = value

    override fun getCheckValue() = true

    override fun getView() = binding.root

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                job.cancel()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}