package com.example.common.utils.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.framework.utils.function.string
import com.example.common.R
import com.example.common.utils.permission.XXPermissionsGroup.CAMERA
import com.example.common.utils.permission.XXPermissionsGroup.LOCATION
import com.example.common.utils.permission.XXPermissionsGroup.MICROPHONE
import com.example.common.utils.permission.XXPermissionsGroup.STORAGE
import com.example.common.widget.dialog.AndDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.text.MessageFormat

/**
 * author: wyb
 * date: 2018/6/11.
 * 获取选项工具类
 * 根据项目需求哪取需要的权限组
 */
class PermissionFactory(private val context: Context) {

    companion object {

        /**
         * 权限检测
         */
        fun checkSelfPermission(context: Context, vararg permission: String): Boolean {
            for (perm in permission) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, perm)) return false
            }
            return true
        }

        /**
         * 定位权限组
         */
        fun checkSelfLocation(context: Context) = checkSelfPermission(context, Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)

        /**
         * 存储权限组
         */
        fun checkSelfStorage(context: Context) = checkSelfPermission(context, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)

    }

    private val andDialog by lazy { AndDialog(context) }
    private val permsGroup = arrayOf(
        LOCATION,//定位
        CAMERA,//拍摄照片，录制视频
        MICROPHONE,//录制音频(腾讯x5)
        STORAGE)//访问照片。媒体。内容和文件
    var onRequest: ((hasPermissions: Boolean) -> Unit)? = null

    /**
     * 检测权限(默认拿全部，可单独拿某个权限组)
     */
    fun requestPermissions() {
        requestPermissions(*permsGroup)
    }

    fun requestPermissions(vararg groups: Array<String>) {
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
                        //never->被永久拒绝授权，请手动授予
                        onRequest?.invoke(false)
                        description(permissions)
                    }
                })
        } else onRequest?.invoke(true)
    }

    /**
     * 彈出授權彈框
     */
    private fun description(permissions: MutableList<String>?) {
        with(context) {
            if (permissions.isNullOrEmpty()) return
            //拼接用戶拒絕後的提示参数
            var rationale = ""
            for (index in permsGroup.indices) {
                if (listOf(*permsGroup[index]).contains(permissions[0])) {
                    rationale += "*${rationale(index)};\n"
                }
            }
            andDialog.apply {
                    onConfirm = { XXPermissions.startPermissionActivity(context, permissions) }
                    setParams(
                        string(R.string.label_window_title),
                        MessageFormat.format(string(R.string.label_window_permission), rationale),
                        string(R.string.label_window_sure),
                        string(R.string.label_window_cancel))
                    show()
                }
        }
    }

    /**
     * 获取提示文案
     */
    private fun rationale(index: Int): String? {
        with(context) {
            return when (index) {
                0 -> string(R.string.label_permissions_location)
                1 -> string(R.string.label_permissions_camera)
                2 -> string(R.string.label_permissions_microphone)
                3 -> string(R.string.label_permissions_storage)
                else -> null
            }
        }
    }

}