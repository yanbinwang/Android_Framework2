package com.example.common.utils.function

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.page.RequestCode.REQUEST_ALBUM
import com.example.common.base.page.RequestCode.REQUEST_IMAGE
import com.example.common.base.page.RequestCode.REQUEST_VIDEO
import com.example.common.config.Constants
import com.example.common.utils.StorageUtil.StorageType
import com.example.common.utils.StorageUtil.getOutputFile
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.value.orZero
import java.io.File
import java.io.Serializable

/**
 * 当前页面注册一个activity的result，获取resultCode
 * 1.拉起相册/视频库/录屏皆可
 * 2.需要读写权限
 * 3.注册方法不允许by lazy，直接变量里这么写
 *  val activityResultValue = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
 *      if (it.resultCode == Activity.RESULT_OK) {
 *          it?.data ?: return@registerForActivityResult
 *          val uri = it.data?.data
 *          func.invoke(uri.getFileFromUri()?.absolutePath)
 *     }
 * }
 */
fun FragmentActivity?.registerResult(func: ((it: ActivityResult) -> Unit)): ActivityResultLauncher<Intent>? {
    this ?: return null
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        func.invoke(it)
    }
}

/**
 * 拉起屏幕录制
 */
fun ActivityResultLauncher<Intent>?.pullUpScreen(mContext: Context?) {
    this ?: return
    val mediaProjectionManager = mContext?.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    launch(mediaProjectionManager?.createScreenCaptureIntent())
}

/**
 * 拉起系统默认相机
 */
fun ActivityResultLauncher<Intent>?.pullUpAlbum() {
    this ?: return
    launch(Intent(Intent.ACTION_PICK, null).apply {
        setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
    })
}

/**
 * 跳转系统默认相册
 * if (resultCode == RESULT_OK && data != null) {
 * val uri = data.data
 * val oriFile = uri.getFileFromUri()
 * val albumPath = oriFile?.absolutePath
 */
fun Activity?.pullUpAlbum() {
    this ?: return
    val intent = Intent(Intent.ACTION_PICK, null)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
    startActivityForResult(intent, REQUEST_ALBUM)
}

/**
 * 打开手机相机-拍照
 * CAMERA, STORAGE
 */
fun Activity?.pullUpImage() {
    this ?: return
    val file = getOutputFile(StorageType.IMAGE)
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    getResult(file, intent, REQUEST_IMAGE)
}

/**
 * 打开手机相机-录像
 * CAMERA, MICROPHONE, STORAGE
 */
fun Activity?.pullUpVideo(second: Int? = 50000, quality: Double? = 0.5) {
    this ?: return
    val file = getOutputFile(StorageType.VIDEO)
    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, second)//设置视频录制的最长时间
    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality)
    getResult(file, intent, REQUEST_VIDEO)
}

private fun Activity?.getResult(file: File?, intent: Intent, requestCode: Int) {
    if (null == file || null == this) return
    try {
        val uri: Uri?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            uri = FileProvider.getUriForFile(this, "${Constants.APPLICATION_ID}.fileProvider", file)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestCode)
    } catch (_: Exception) {
    }
}

/**
 * 高版本后台服务有浮层需要允许当前设置
 */
fun Activity?.pullUpOverlay(): Boolean {
    this ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:${packageName}")
        startActivity(intent)
        false
    } else {
        true
    }
}

/**
 * 拉起app
 * AndroidMainFest中写入包名配置清单
 * <queries>
 * <package android:name="com.google.android.apps.walletnfcrel" />
 * <package android:name="com.phonepe.app" />
 * </queries>
 */
fun Context?.pullUpPackage(packageName: String) {
    this ?: return
    try {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (_: Exception) {
    }
}

/**
 * 从google搜索内容
 */
fun Context?.toGoogleSearch(searchText: String) {
    this ?: return
    Intent().apply {
        action = Intent.ACTION_WEB_SEARCH
        putExtra(SearchManager.QUERY, searchText)
        startActivity(this)
    }
}

/**
 * 浏览网页
 */
fun Context?.toBrowser(url: String) {
    this ?: return
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/**
 * 显示地图
 */
fun Context?.toMap(longitude: Double, latitude: Double) {
    this ?: return
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${longitude.orZero},${latitude.orZero}")))
}

/**
 * 拨打电话
 */
fun Context?.toPhone(tel: String) {
    this ?: return
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))
}

/**
 * 调用发短信的程序
 */
fun Context?.toSMS(text: String) {
    this ?: return
    Intent(Intent.ACTION_VIEW).apply {
        putExtra("sms_body", text)
        type = "vnd.android-dir/mms-sms"
        startActivity(this)
    }
}

/**
 * 发短信
 */
fun Context?.toSMSApp(tel: String, text: String) {
    this ?: return
    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$tel")).apply {
        putExtra("sms_body", text)
        startActivity(this)
    }
}

/**
 * 打开压缩包
 */
fun Context?.openZip(filePath: String) = openFile(filePath, "application/x-zip-compressed")

/**
 * 打开world
 */
fun Context?.openWorld(filePath: String) = openFile(filePath, "application/msword")

/**
 * 打开安装包
 */
fun Context?.openSetupApk(filePath: String) =
    openFile(filePath, "application/vnd.android.package-archive")

/**
 * 统一开启文件
 * https://zhuanlan.zhihu.com/p/260340912
 */
fun Context?.openFile(filePath: String, type: String) {
    this ?: return
    val file = File(filePath)
    if (file.fileValidation()) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                setDataAndType(FileProvider.getUriForFile(this@openFile, "${Constants.APPLICATION_ID}.fileProvider", file), type)
            } else {
                setDataAndType(Uri.parse("file://$filePath"), type)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        })
    }
}

/**
 * 发送文件
 * image -> 图片
 */
fun Context?.sendFile(filePath: String, fileType: String? = "*/*", title: String? = "分享文件") {
    this ?: return
    val file = File(filePath)
    if (file.fileValidation()) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@sendFile, "${Constants.APPLICATION_ID}.fileProvider", file))
            } else {
                putExtra(Intent.EXTRA_STREAM, file)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = fileType//此处可发送多种文件
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }, title))
    }
}

/**
 * 文件校验方法
 */
private fun File?.fileValidation(): Boolean {
    this ?: return false
    return if (!exists()) {
        R.string.sourcePathError.shortToast()
        false
    } else {
        true
    }
}

/**
 * 获取对应对象->方法已经淘汰，扩展写下
 */
fun <T : Serializable> Intent?.getExtra(name: String, clazz: Class<T>): T? {
    this ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, clazz)
    } else {
        getSerializableExtra(name) as? T
    }
}

fun <T : Parcelable> Intent?.getExtra(name: String, clazz: Class<T>): T? {
    this ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, clazz)
    } else {
        getParcelableExtra(name) as? T
    }
}

/**
 * Serializable未提供集合的方法，需要强转：
 * putSerializable(Extra.BUNDLE_LIST, list as? Serializable)
 * 然后getArrayListExtra取值后再强转
 * 故而使用putParcelableArrayListExtra来传输集合
 * putParcelableArrayListExtra(Extra.BUNDLE_LIST, list) })
 * list--->ArrayList<T>
 */
fun <T : Parcelable> Intent?.getArrayListExtra(name: String, clazz: Class<T>): ArrayList<T>? {
    this ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, clazz)
    } else {
        getParcelableArrayListExtra(name)
    }
}

fun <T> Intent?.getArrayListExtra(name: String): ArrayList<T>? {
    this ?: return null
    return getSerializableExtra(name) as? ArrayList<T>
}