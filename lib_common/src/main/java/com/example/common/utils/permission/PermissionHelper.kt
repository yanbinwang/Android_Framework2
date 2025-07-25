package com.example.common.utils.permission

import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.common.R
import com.example.common.utils.function.string
import com.example.common.utils.permission.XXPermissionsGroup.CAMERA_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.LOCATION_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.MICROPHONE_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.STORAGE_GROUP
import com.example.common.widget.dialog.AndDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.base.IPermission

/**
 * author: wyb
 * date: 2018/6/11.
 * 获取选项工具类
 * 根据项目需求哪取需要的权限组
 */
class PermissionHelper(private val context: Context) {
    private val andDialog by lazy { AndDialog(context) }
    private val permsGroup = arrayOf(
        LOCATION_GROUP,//定位
        CAMERA_GROUP,//拍摄照片，录制视频
        MICROPHONE_GROUP,//录制音频(腾讯x5)
        STORAGE_GROUP)//访问照片。媒体。内容和文件

    /**
     * 检测权限(默认拿全部，可单独拿某个权限组)
     */
    fun requestPermissions(listener: (hasPermissions: Boolean) -> Unit = {}) {
        requestPermissions(*permsGroup, listener = listener)
    }

    fun requestPermissions(vararg groups: List<IPermission>, listener: (hasPermissions: Boolean) -> Unit = {}, force: Boolean = true) {
        //6.0+系统做特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XXPermissions.with(context)
                .permissions(groups.toMutableList().flatten())
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<IPermission>, allGranted: Boolean) {
                        //allGranted->标记是否是获取部分权限成功，部分未正常授予，true全拿，false部分拿到
                        listener.invoke(allGranted)
                    }

                    override fun onDenied(permissions: MutableList<IPermission>, doNotAskAgain: Boolean) {
                        super.onDenied(permissions, doNotAskAgain)
                        //doNotAskAgain->被永久拒绝授权，请手动授予
                        listener.invoke(false)
                        if (force) onDenied(permissions)
                    }
                })
        } else {
            listener.invoke(true)
        }
    }

    /**
     * 彈出授權彈框
     */
    private fun onDenied(permissions: MutableList<IPermission>?) {
        if (permissions.isNullOrEmpty()) return
        //拼接用戶拒絕後的提示参数
        var reason = ""
        for (index in permsGroup.indices) {
            if (permsGroup[index].contains(permissions[0])) {
                reason += "${onReason(index)}\n"
            }
        }
        andDialog.apply {
            setParams(message = string(R.string.permissionGoSetting, reason))
            setDialogListener({ XXPermissions.startPermissionActivity(context, permissions) })
            show()
        }
    }

    /**
     * 获取提示文案
     */
    private fun onReason(index: Int): String? {
        return when (index) {
            0 -> string(R.string.permissionLocation)
            1 -> string(R.string.permissionCamera)
            2 -> string(R.string.permissionMicrophone)
            3 -> string(R.string.permissionStorage)
            else -> null
        }
    }

}

/**
 *  private val mRequestPermissionWrapper = registerRequestPermissionWrapper()
 *  private val mRequestPermissionResult = mRequestPermissionWrapper.registerResult { ... }
 */
interface RequestPermissionRegistrar {
    val requestPermissionCaller: ActivityResultCaller
    fun registerResult(func: (Boolean) -> Unit): ActivityResultLauncher<String> {
        return requestPermissionCaller.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            func.invoke(it)
        }
    }
}

fun AppCompatActivity.registerRequestPermissionWrapper(): RequestPermissionRegistrar = object : RequestPermissionRegistrar {
    override val requestPermissionCaller: ActivityResultCaller get() = this@registerRequestPermissionWrapper
}

fun Fragment.registerRequestPermissionWrapper(): RequestPermissionRegistrar = object : RequestPermissionRegistrar {
    override val requestPermissionCaller: ActivityResultCaller get() = this@registerRequestPermissionWrapper
}