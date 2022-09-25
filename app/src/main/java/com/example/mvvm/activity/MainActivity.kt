package com.example.mvvm.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.ToastUtil
import com.example.base.utils.function.string
import com.example.base.utils.function.view.click
import com.example.common.R
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.RequestCode
import com.example.common.widget.dialog.AndDialog
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.PermissionHelper
import pub.devrel.easypermissions.EasyPermissions
import java.text.MessageFormat

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>(),EasyPermissions.PermissionCallbacks,EasyPermissions.RationaleCallbacks {
    private val permissionHelper by lazy { PermissionHelper(this) }

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
//        PermissionHelper.initialize(this)
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnLogin.click {
            permissionHelper.requestPermissions(Manifest.permission.CAMERA, onRequest = {
                if(it) navigation(ARouterPath.LoginActivity)
            })
        }
//        /**
//         * return true:已经获取权限
//         * return false: 未获取权限，主动请求权限
//         */
//        EasyPermissions.hasPermissions(this,(Manifest.permission.CAMERA))
//        /**
//         * 第一个参数：Context对象
//         * 第二个参数：权限弹窗上的文字提示语。告诉用户，这个权限用途。
//         * 第三个参数：这次请求权限的唯一标示，code。
//         * 第四个参数 : 一些系列的权限。
//         */
//        EasyPermissions.requestPermissions(this, "文案",RequestCode.PERMISSION_REQUEST,(Manifest.permission.CAMERA))
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        //将请求结果传递EasyPermission库处理
//        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults)
//    }
//
////    @AfterPermissionGranted(Constance.WRITE_PERMISSION_CODE) 是可选的
////    public void onPermissionsSuccess() {
////        ToastUtils.showToast(getApplicationContext(), "用户授权成功");
////    }
//
//    /**
//     * 请求权限成功。
//     * 可以弹窗显示结果，也可执行具体需要的逻辑操作
//     */
//    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//    }
//
//    /**
//     * 请求权限失败
//     */
//    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        /**
//         * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
//         * 这时候，需要跳转到设置界面去，让用户手动开启。
//         */
//        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
////            new AppSettingsDialog.Builder(this).build().show();
//        }
//    }

    /**
     * 将请求结果传递EasyPermission库处理
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 请求权限成功。
     * 可以弹窗显示结果，也可执行具体需要的逻辑操作
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        onRequest?.invoke(true)
    }

    /**
     * 请求权限失败
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        showToast("请求权限失败")
            /**
             * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
             * 这时候，需要跳转到设置界面去，让用户手动开启。
             */
            if (pub.devrel.easypermissions.EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                com.example.common.widget.dialog.AndDialog.with(this)
                    .setOnDialogListener({
                        startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${packageName}")))
                    })
                    .setParams(
                        string(com.example.common.R.string.label_window_title),
                        java.text.MessageFormat.format(string(com.example.common.R.string.label_window_permission)),
                        string(com.example.common.R.string.label_window_sure),
                        string(com.example.common.R.string.label_window_cancel)
                    )
                    .show()
            }
    }

    /**
     * 用户再次授权
     */
    override fun onRationaleAccepted(requestCode: Int) {
        showToast("用户再次授权")
    }

    /**
     * 用户取消授权
     */
    override fun onRationaleDenied(requestCode: Int) {
        showToast("用户取消授权")
    }

}