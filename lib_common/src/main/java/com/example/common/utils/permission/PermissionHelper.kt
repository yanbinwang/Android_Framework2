package com.example.common.utils.permission

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.utils.i18n.string
import com.example.common.utils.permission.XXPermissionsGroup.CAMERA_GROUP
import com.example.common.utils.permission.XXPermissionsGroup.STORAGE_GROUP
import com.example.common.widget.dialog.AppDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.base.IPermission

/**
 * author: wyb
 * date: 2018/6/11.
 * 获取选项工具类
 * 根据项目需求哪取需要的权限组
 */
class PermissionHelper(private val activity: FragmentActivity) {
    private val dialog by lazy { AppDialog(activity) }
    // 当前app内需要的所有权限组
    private val allPermsGroup = arrayOf(
        CAMERA_GROUP,//拍摄照片，录制视频
        STORAGE_GROUP)//访问照片。媒体。内容和文件

    /**
     * 检测权限->6.0+系统做特殊处理(默认拿全部，可单独拿某个权限)
     * hasPrompt:系统默认所有权限的提示,如需拿取配置值外的权限,需写false
     */
    fun requestPermissions(listener: (isGranted: Boolean, permissions: List<IPermission?>?) -> Unit = { _, _ -> }) {
        requestPermissions(*allPermsGroup, listener = listener)
    }

    fun requestPermissions(vararg groups: List<IPermission>, listener: (isGranted: Boolean, permissions: List<IPermission?>?) -> Unit = { _, _ -> }, hasPrompt: Boolean = true) {
        XXPermissions.with(activity)
            .permissions(groups.toMutableList().flatten())
            .request(object : OnPermissionCallback {
                /**
                 * allGranted->标记是否是获取部分权限成功，部分未正常授予，true全拿，false部分拿到(同时onDenied会回调)
                 */
                override fun onGranted(permissions: List<IPermission?>, allGranted: Boolean) {
                    if (allGranted) {
                        listener.invoke(true, null)
                    }
                }

                /**
                 * doNotAskAgain->被永久拒绝授权，请手动授予
                 */
                override fun onDenied(permissions: List<IPermission?>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                    listener.invoke(false, permissions)
                    if (hasPrompt) onDenied(permissions.toMutableList())
                }
            })
    }

    /**
     * 彈出授權彈框
     */
    private fun onDenied(permissions: MutableList<IPermission?>?) {
        if (permissions.isNullOrEmpty()) return
        // 拼接用戶拒絕後的提示参数
        var reason = ""
        var subscript = 0
        for (index in allPermsGroup.indices) {
            // 拿到对应权限组下标的权限集合
            val permsGroup = allPermsGroup[index]
            // 判断权限集合是否有交集（A 包含 B 中的部分元素）
            val hasCommonElements = permsGroup.intersect(permissions).isNotEmpty()
            // 字符串提示拼接
            if (hasCommonElements) {
                subscript ++
                reason += "(\u0020${subscript}\u0020)\u0020${onReason(index)}\n"
            }
        }
        dialog
            .setParams(message = string(R.string.permissionGoSetting, reason))
            .setDialogListener({
                XXPermissions.startPermissionActivity(activity, permissions)
            })
            .show()
    }

    /**
     * 获取提示文案
     */
    private fun onReason(index: Int): String? {
        return when (index) {
            0 -> string(R.string.permissionCamera)
            1 -> string(R.string.permissionStorage)
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