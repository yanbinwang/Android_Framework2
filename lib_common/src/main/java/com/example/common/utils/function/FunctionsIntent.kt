package com.example.common.utils.function

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.example.common.R
import com.example.common.base.page.ResultCode.RESULT_ALBUM
import com.example.common.base.page.ResultCode.RESULT_IMAGE
import com.example.common.base.page.ResultCode.RESULT_VIDEO
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
interface ActivityResultRegistrar {
    val activityResultCaller: ActivityResultCaller
    fun registerResult(func: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return activityResultCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            func.invoke(it)
        }
    }
}

fun AppCompatActivity.registerResultWrapper(): ActivityResultRegistrar = object : ActivityResultRegistrar {
    override val activityResultCaller: ActivityResultCaller get() = this@registerResultWrapper
}

fun Fragment.registerResultWrapper(): ActivityResultRegistrar = object : ActivityResultRegistrar {
    override val activityResultCaller: ActivityResultCaller get() = this@registerResultWrapper
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
    startActivityForResult(intent, RESULT_ALBUM)
}

/**
 * 打开手机相机-拍照
 * CAMERA, STORAGE
 */
fun Activity?.pullUpImage() {
    this ?: return
    val file = getOutputFile(StorageType.IMAGE)
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    forResult(file, intent, RESULT_IMAGE)
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
    forResult(file, intent, RESULT_VIDEO)
}

private fun Activity?.forResult(file: File?, intent: Intent, requestCode: Int) {
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
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 高版本后台服务有浮层需要允许当前设置
 */
fun Activity?.pullUpOverlay(): Boolean {
    this ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = "package:${packageName}".toUri()
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
    } catch (e: Exception) {
        e.printStackTrace()
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
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}

/**
 * 显示地图
 */
fun Context?.toMap(longitude: Double, latitude: Double) {
    this ?: return
    startActivity(Intent(Intent.ACTION_VIEW, "geo:${longitude.orZero},${latitude.orZero}".toUri()))
}

/**
 * 拨打电话
 */
fun Context?.toPhone(tel: String) {
    this ?: return
    startActivity(Intent(Intent.ACTION_DIAL, "tel:$tel".toUri()))
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
    Intent(Intent.ACTION_SENDTO, "smsto:$tel".toUri()).apply {
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
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(FileProvider.getUriForFile(this@openFile, "${Constants.APPLICATION_ID}.fileProvider", file), type)
                } else {
                    setDataAndType("file://$filePath".toUri(), type)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            "未找到合适的应用来打开此文件，请安装相关应用".shortToast()
        }
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
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

/**
 * makeCustomAnimation
 * 效果：借助自定义的动画资源，达成 Activity 切换时的过渡效果。
 * 用法：
 * // 从activityA跳转到activityB时使用自定义动画
 * Intent intent = new Intent(activityA, activityB);
 * ActivityOptions options = ActivityOptions.makeCustomAnimation(
 *     activityA,
 *     R.anim.slide_in_right,  // 进入动画
 *     R.anim.slide_out_left   // 退出动画
 * ActivityCompat.startActivity(activityA, intent, options.toBundle());
 * 实现效果：新 Activity 从右侧滑入，旧 Activity 向左侧滑出。
 */
fun getCustomOption(context: Context, enterResId: Int, exitResId: Int): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeCustomAnimation(context, enterResId, exitResId)
}

/**
 * makeScaleUpAnimation
 * 效果：新 Activity 从特定位置开始，进行缩放和淡入操作。
 * 用法：
 * // 从坐标(startX, startY)处以初始尺寸(startWidth, startHeight)开始缩放
 * ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
 *     view,           // 动画起始的视图
 *     startX,         // X轴起始坐标
 *     startY,         // Y轴起始坐标
 *     startWidth,     // 初始宽度
 *     startHeight     // 初始高度
 * );
 * 实现效果：新 Activity 从指定点开始，逐渐放大到全屏
 */
fun getScaleUpOption(view: View, startX: Int, startY: Int, width: Int, height: Int): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeScaleUpAnimation(view, startX, startY, width, height)
}

/**
 * makeThumbnailScaleUpAnimation
 * 效果：以缩略图为基础，实现 Activity 的缩放过渡效果。
 * 用法：
 * // 共享缩略图的缩放动画
 * Bitmap thumbnail = getThumbnailBitmap(); // 获取缩略图
 * ActivityOptions options = ActivityOptions.makeThumbnailScaleUpAnimation(
 *     sourceView,     // 源视图
 *     thumbnail,      // 缩略图
 *     startX,         // 起始X坐标
 *     startY          // 起始Y坐标
 * );
 * 实现效果：新 Activity 从缩略图位置开始，逐步放大到全屏。
 */
fun getThumbnailScaleUpOption(view: View, thumbnail: Bitmap, startX: Int, startY: Int): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeThumbnailScaleUpAnimation(view, thumbnail, startX, startY)
}

/**
 * makeSceneTransitionAnimation
 * 效果：实现共享元素在不同 Activity 之间的平滑过渡。
 * 用法：
 * // 共享元素的场景过渡动画
 * Intent intent = new Intent(this, DetailActivity.class);
 * ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
 *     this,
 *     Pair.create(view1, "shared_element_name1"), // 共享元素1
 *     Pair.create(view2, "shared_element_name2")  // 共享元素2
 * );
 * ActivityCompat.startActivity(this, intent, options.toBundle());
 * 实现效果：共享元素在 Activity 切换时保持视觉连贯性，仿佛是同一个元素在移动或变换。
 */
fun getSceneTransitionOption(activity: Activity, vararg sharedElements: Pair<View, String>): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *sharedElements)
}

/**
 * makeClipRevealAnimation
 * 效果：以圆形或矩形的方式，显示新 Activity。
 * 用法：
 * // 从指定位置开始的圆形显示动画
 * ActivityOptions options = ActivityOptions.makeClipRevealAnimation(
 *     targetView,     // 目标视图
 *     startX,         // 起始X坐标
 *     startY,         // 起始Y坐标
 *     width,          // 宽度
 *     height          // 高度
 * );
 * 实现效果：新 Activity 从指定点开始，像水波一样逐渐显示出来。
 */
@RequiresApi(Build.VERSION_CODES.M)
fun getClipRevealOption(view: View, startX: Int, startY: Int, width: Int, height: Int): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeClipRevealAnimation(view, startX, startY, width, height)
}

/**
 * makeTaskLaunchBehind
 * 效果：在当前 Activity 的后面启动新的 Activity 任务。
 * 用法：
 * // 在当前Activity后面启动新任务
 * ActivityOptions options = ActivityOptions.makeTaskLaunchBehind();
 * startActivity(intent, options.toBundle());
 */
fun getTaskLaunchBehind(): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeTaskLaunchBehind()
}

/**
 * setLaunchBounds
 * 效果：对 Activity 的启动区域进行限制。
 * 用法：
 * // 设置Activity的启动边界
 * ActivityOptions options = ActivityOptions.makeBasic();
 * options.setLaunchBounds(new Rect(left, top, right, bottom));
 * startActivity(intent, options.toBundle());
 */
@RequiresApi(Build.VERSION_CODES.N)
fun getMakeBasic(left: Int, top: Int, right: Int, bottom: Int): ActivityOptionsCompat {
    return ActivityOptionsCompat.makeBasic().apply { launchBounds = Rect(left, top, right, bottom) }
}