package com.example.mvvm.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.common.R
import com.example.common.widget.dialog.AndDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.lang.ref.WeakReference
import java.text.MessageFormat

/**
 * 权限库帮助类
 */
class PermissionHelper(context: Context) {
    val CALENDAR = arrayOf(Permission.READ_CALENDAR, Permission.WRITE_CALENDAR)
    val CAMERA = arrayOf(Permission.CAMERA)
    val CONTACTS = arrayOf(Permission.READ_CONTACTS, Permission.WRITE_CONTACTS, Permission.GET_ACCOUNTS)
    val LOCATION = arrayOf(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
    val MICROPHONE = arrayOf(Permission.RECORD_AUDIO)
    val PHONE = arrayOf(Permission.READ_PHONE_STATE, Permission.CALL_PHONE, Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.ADD_VOICEMAIL, Permission.USE_SIP, Permission.PROCESS_OUTGOING_CALLS)
    val SENSORS = arrayOf(Permission.BODY_SENSORS)
    val SMS = arrayOf(Permission.SEND_SMS, Permission.RECEIVE_SMS, Permission.READ_SMS, Permission.RECEIVE_WAP_PUSH, Permission.RECEIVE_MMS)
    val STORAGE = arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
    private var denied = true
    private val context = WeakReference(context).get()!!
    private val permsGroup = arrayOf(
        LOCATION,//定位
        CAMERA,//拍摄照片，录制视频
        MICROPHONE,//录制音频(腾讯x5)
        STORAGE)//访问照片。媒体。内容和文件
    private var onRequest: ((hasPermissions: Boolean) -> Unit)? = null

    //检测权限(默认拿全部，可单独拿某个权限组)
    fun requestPermissions(): PermissionHelper {
        return requestPermissions(*permsGroup)
    }

    fun requestPermissions(vararg groups: Array<String>): PermissionHelper {
        //6.0+系统做特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XXPermissions.with(context)
                .permission(*groups)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        //all->标记是否是获取部分权限成功，部分未正常授予，true全拿，false部分拿到
                        onRequest?.invoke(all)
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                        super.onDenied(permissions, never)
                        onRequest?.invoke(false)
                        if (denied && !permissions.isNullOrEmpty()) {
                            var index = 0
                            for (i in permsGroup.indices) {
                                if (listOf(*permsGroup[i]).contains(permissions[0])) {
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
                            AndDialog.with(context)
                                .setOnDialogListener({
                                    XXPermissions.startPermissionActivity(
                                        context,
                                        permissions
                                    )
                                })
                                .setParams(
                                    context.getString(R.string.label_window_title),
                                    MessageFormat.format(
                                        context.getString(R.string.label_window_permission),
                                        rationale
                                    ),
                                    context.getString(R.string.label_window_sure),
                                    context.getString(R.string.label_window_cancel)
                                )
                                .show()
//                            //被永久拒绝授权，请手动授予
//                            if (never) {
//                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                                XXPermissions.startPermissionActivity(context, permissions)
//                            } else {
//
//                            }
                        }
                    }
                })
        } else onRequest?.invoke(true)
        return this
    }

    /**
     * 全局回调
     */
    fun onRequest(
        onRequest: ((hasPermissions: Boolean) -> Unit),
        denied: Boolean = true
    ): PermissionHelper {
        this.onRequest = onRequest
        this.denied = denied
        return this
    }

    /**
     * 权限检测
     */
    private fun checkSelfPermission(vararg permission: String): Boolean {
        for (perm in permission) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                    context,
                    perm
                )
            ) return false
        }
        return true
    }

    /**
     * 定位权限组
     */
    fun checkSelfLocation() =
        checkSelfPermission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)

    /**
     * 存储权限组
     */
    fun checkSelfStorage() =
        checkSelfPermission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)

    companion object {
        @JvmStatic
        fun with(context: Context?): PermissionHelper {
            return PermissionHelper(context!!)
        }
    }

}