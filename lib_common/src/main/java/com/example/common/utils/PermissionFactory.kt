package com.example.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.example.common.R
import com.example.common.widget.dialog.AndDialog
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import java.lang.ref.WeakReference
import java.text.MessageFormat

/**
 * author: wyb
 * date: 2018/6/11.
 * 获取选项工具类
 * 根据项目需求哪取需要的权限组
 */
class PermissionFactory(context: Context) {
    private var denied = true
    private val context = WeakReference(context).get()!!
    private val permsGroup = arrayOf(
        Permission.Group.LOCATION,//定位
        Permission.Group.CAMERA,//拍摄照片，录制视频
        Permission.Group.MICROPHONE,//录制音频(腾讯x5)
        Permission.Group.STORAGE)//访问照片。媒体。内容和文件
    private var onRequest: ((hasPermissions: Boolean) -> Unit)? = null

    //检测权限(默认拿全部，可单独拿某个权限组)
    fun requestPermissions(): PermissionFactory {
        return requestPermissions(*permsGroup)
    }

    fun requestPermissions(vararg groups: Array<String>): PermissionFactory {
        //6.0+系统做特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission()) {
                onRequest?.invoke(true)
            } else {
                AndPermission.with(context)
                    .permission(*groups)
                    //权限申请成功回调
                    .onGranted {
                        onRequest?.invoke(true)
                    }
                    //权限申请失败回调-安卓Q定位为使用期间允许，此处做一次检测
                    .onDenied {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (checkSelfLocation() && it.size == 1 && listOf(*Permission.Group.LOCATION).contains(it[0])) {
                                onRequest?.invoke(true)
                                return@onDenied
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (checkSelfStorage() && it.size == 1 && listOf(*Permission.Group.STORAGE).contains(it[0])) {
                                onRequest?.invoke(true)
                                return@onDenied
                            }
                        }
                        if (checkSelfPermission()) {
                            onRequest?.invoke(true)
                        } else {
                            onRequest?.invoke(false)
                            if (denied) {
                                var index = 0
                                for (i in permsGroup.indices) {
                                    if (listOf(*permsGroup[i]).contains(it[0])) {
                                        index = i
                                        break
                                    }
                                }
                                //提示参数
                                val rationale = when (index) {
                                    0 -> context.getString(R.string.label_permissions_location)
                                    1 -> context.getString(R.string.label_permissions_camera)
                                    2 -> context.getString(R.string.label_permissions_microphone)
                                    3 -> context.getString(R.string.label_permissions_storage)
                                    else -> null
                                }
                                //如果用户拒绝了开启权限
                                if (AndPermission.hasAlwaysDeniedPermission(context, it)) {
                                    AndDialog.with(context)
                                        .setOnDialogListener({ context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) })
                                        .setParams(context.getString(R.string.label_window_title), MessageFormat.format(context.getString(R.string.label_window_permission), rationale), context.getString(R.string.label_window_sure), context.getString(R.string.label_window_cancel))
                                        .show()
                                }
                            }
                        }
                    }.start()
            }
        } else onRequest?.invoke(true)
        return this
    }

    /**
     * 全局回调
     */
    fun onRequest(onRequest: ((hasPermissions: Boolean) -> Unit), denied: Boolean = true): PermissionFactory {
        this.onRequest = onRequest
        this.denied = denied
        return this
    }

    /**
     * 权限检测
     */
    private fun checkSelfPermission(vararg permission: String): Boolean {
        for (perm in permission) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, perm)) return false
        }
        return true
    }

    /**
     * 全局快速检测
     */
    private fun checkSelfPermission(): Boolean {
        var granted = true
        for (groupIndex in permsGroup.indices) {
            if (0 == groupIndex) {
                granted = checkSelfLocation()
            } else {
                for (index in permsGroup[groupIndex].indices) {
                    if (!checkSelfPermission(permsGroup[groupIndex][index])) granted = false
                }
            }
        }
        return granted
    }

    /**
     * 定位权限在安卓10开始有些许变化
     * 1.允许-前后台皆可定位
     * 2.仅在使用中允许-前台可定位，后台被拒绝
     * 3.拒绝-前后台都拒绝
     */
    fun checkSelfLocation() = checkSelfPermission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)

    /**
     * 存储权限安卓11开始废除
     */
    fun checkSelfStorage() = checkSelfPermission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)

    companion object {
        @JvmStatic
        fun with(context: Context?): PermissionFactory {
            return PermissionFactory(context!!)
        }
    }

}