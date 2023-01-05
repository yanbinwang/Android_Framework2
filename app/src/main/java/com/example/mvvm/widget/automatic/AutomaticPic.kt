package com.example.mvvm.widget.automatic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.getFileFromUri
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.mvvm.R
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @description
 * @author
 */
class AutomaticPic(private val activity: AppCompatActivity, private val bean: AutomaticBean) : AutomaticInterface, LifecycleEventObserver {
    private val rootView by lazy(NONE) { activity.inflate(R.layout.view_automatic_pic) }
    private var activityResultValue = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it?.data ?: return@registerForActivityResult
            val uri = it.data?.data
            uri.getFileFromUri()?.absolutePath.shortToast()
        }
    }
    private var filePath = ""

    init {
        activity.lifecycle.addObserver(this)

        val textLabel = rootView.findViewById<TextView>(R.id.tv_label)
        textLabel.text = bean.label

        val flContent = rootView.findViewById<FrameLayout>(R.id.fl_content)
        flContent.click {
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            activityResultValue.launch(intent)
        }
    }

    override fun getBean(): AutomaticBean {
        return bean
    }

    override fun getResult(): Pair<String, String> {
        return bean.key to filePath
    }

    override fun getView(): View {
        return rootView
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}