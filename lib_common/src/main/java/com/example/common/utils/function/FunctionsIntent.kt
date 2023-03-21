package com.example.common.utils.function

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.common.config.Constants
import com.example.common.utils.builder.shortToast
import java.io.File

/**
 * 跳转通知设置界面
 */
fun Activity.notificationSetting() {
    val intent = Intent()
    val sdkVersion = Build.VERSION.SDK_INT
    when {
        //8.0+
        sdkVersion >= Build.VERSION_CODES.O -> {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("android.provider.extra.APP_PACKAGE", Constants.APPLICATION_ID)
        }
        //5.0-7.0
        sdkVersion >= Build.VERSION_CODES.LOLLIPOP && sdkVersion < Build.VERSION_CODES.O -> {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", Constants.APPLICATION_ID)
            intent.putExtra("app_uid", applicationInfo?.uid)
        }
        //其他
        else -> {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", Constants.APPLICATION_ID, null)
        }
    }
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

/**
 * 跳转系统默认相册
 * if (resultCode == RESULT_OK && data != null) {
 * val uri = data.data
 * val oriFile = uri.getFileFromUri()
 * val albumPath = oriFile?.absolutePath
 */
fun Activity.pullUpAlbum() {
    val intent = Intent(Intent.ACTION_PICK, null)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
    startActivityForResult(intent, 0x114)
}

/**
 * 拉起app
 * AndroidMainFest中写入包名配置清单
 * <queries>
 * <package android:name="com.google.android.apps.walletnfcrel" />
 * <package android:name="com.phonepe.app" />
 * </queries>
 */
fun Context.pullUpPackage(packageName: String) {
    if (packageName.isEmpty()) return
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
fun Context.toGoogleSearch(searchText: String) {
    Intent().apply {
        action = Intent.ACTION_WEB_SEARCH
        putExtra(SearchManager.QUERY, searchText)
        startActivity(this)
    }
}

/**
 * 浏览网页
 */
fun Context.toBrowser(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/**
 * 显示地图
 */
fun Context.toMap(longitude: Float, latitude: Float) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$longitude,$latitude")))
}

/**
 * 拨打电话
 */
fun Context.toPhone(tel: String) {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))
}

/**
 * 调用发短信的程序
 */
fun Context.toSMS(text: String) {
    Intent(Intent.ACTION_VIEW).apply {
        putExtra("sms_body", text)
        type = "vnd.android-dir/mms-sms"
        startActivity(this)
    }
}

/**
 * 发短信
 */
fun Context.toSMSApp(tel: String, text: String) {
    Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$tel")).apply {
        putExtra("sms_body", text)
        startActivity(this)
    }
}

/**
 * 打开压缩包
 */
fun Context.openZip(filePath: String) = openFile(filePath, "application/x-zip-compressed")

/**
 * 打开world
 */
fun Context.openWorld(filePath: String) = openFile(filePath, "application/msword")

/**
 * 打开安装包
 */
fun Context.openSetupApk(filePath: String) =
    openFile(filePath, "application/vnd.android.package-archive")

/**
 * 统一开启文件
 */
fun Context.openFile(filePath: String, type: String) {
    val file = File(filePath)
    if (!file.exists()) {
        "文件路径错误".shortToast()
        return
    }
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

/**
 * 发送文件
 * image -> 图片
 */
fun Context.sendFile(filePath: String, fileType: String? = "*/*", title: String? = "分享文件") {
    val file = File(filePath)
    if (!file.exists()) {
        "文件路径错误".shortToast()
        return
    }
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