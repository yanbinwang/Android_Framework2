package com.example.mvvm.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.base.utils.ToastUtil
import com.example.base.utils.function.string
import com.example.common.R
import com.example.common.constant.RequestCode
import com.example.common.widget.dialog.AndDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.lang.ref.WeakReference
import java.text.MessageFormat

/**
 * 权限库帮助类
 * 需要用到权限的页面需要重写onRequestPermissionsResult
 * 将请求结果传递EasyPermission库处理
 * EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults)
 *
 */
class PermissionHelper(activity: Activity)  {
    private val weakActivity = WeakReference(activity).get()
    private var onRequest: ((hasPermissions: Boolean) -> Unit)? = null

    fun requestPermissions(
        vararg perms: String,
        onRequest: ((hasPermissions: Boolean) -> Unit)? = {}
    ) {
        this.onRequest = onRequest
        //6.0+系统做特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(weakActivity!!) {
                /**
                 * return true:已经获取权限
                 * return false: 未获取权限，主动请求权限
                 */
                if (!EasyPermissions.hasPermissions(this, *perms)) {
                    /**
                     * 第一个参数：Context对象
                     * 第二个参数：权限弹窗上的文字提示语。告诉用户，这个权限用途。
                     * 第三个参数：这次请求权限的唯一标示，code。
                     * 第四个参数 : 一些系列的权限。
                     * 如果第一次弹出申请，而用户拒绝，后续弹的都会是参数2的弹框
                     */
//                    EasyPermissions.requestPermissions(
//                        this,
//                        "文案",
//                        RequestCode.PERMISSION_REQUEST,
//                        *perms
//                    )
                    EasyPermissions.requestPermissions(
                        PermissionRequest.Builder(this, RequestCode.PERMISSION_REQUEST,  *perms)
//                        .setRationale(rationale)
                        .build())
                } else onRequest?.invoke(true)
            }
        } else onRequest?.invoke(true)
    }




}