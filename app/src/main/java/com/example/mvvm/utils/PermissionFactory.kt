package com.example.mvvm.utils

import android.Manifest
import android.app.Activity
import androidx.fragment.app.Fragment
import com.example.common.constant.RequestCode
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference

class PermissionFactory private constructor() : EasyPermissions.PermissionCallbacks {
    private var weakAct: WeakReference<Activity>? = null
    private var weakFrag: WeakReference<Fragment>? = null
    var PERMMISSIONS: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @AfterPermissionGranted(100)
    private fun doSomethingWithPermissions() {

    }

    private fun tts(){
        if (EasyPermissions.hasPermissions(this, *PERMMISSIONS)) {
            showToast("权限申请通过")
        } else {
            //如果没有上述权限,那么申请权限
            EasyPermissions.requestPermissions(this, "权限申请原理对话框 : 描述申请权限的原理", 100, *PERMMISSIONS)
        }
    }

//    constructor(activity: Activity) {
//        weakAct = WeakReference(activity)
//    }
//
//    constructor(fragment: Fragment) {
//        weakFrag = WeakReference(fragment)
//    }

    /**
     * 重写 Activity 的 onRequestPermissionsResult 方法
     * 主要是在该方法中使用 EasyPermissions 进一步处理权限申请后续结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(
            RequestCode.PERMISSION_REQUEST,
            permissions,
            grantResults,
            this
        )
    }

    /**
     * 用户点击同意授权后会回调该方法
     * perms-》拒绝的权限
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    /**
     * 如果申请的权限中有任何一个权限存在 永久拒绝 的情况
     * 设置 引导用户前往设置界面 自行设置权限的引导对话框
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (weakAct?.get() == null) onFragmentDenied(perms) else onActivityDenied(perms)
    }

    private fun onActivityDenied(perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(weakAct?.get()!!, perms)) {
            AppSettingsDialog.Builder(weakAct?.get()!!)
                .setTitle("需要手动设置权限")
                .setRationale("存在永久拒绝的权限 , 需要手动前往设置界面为应用进行授权")
                .setPositiveButton("前往设置界面")
                .setNegativeButton("不使用该功能")
                .build().show()
        }
    }

    private fun onFragmentDenied(perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(weakFrag?.get()!!, perms)) {
            AppSettingsDialog.Builder(weakFrag?.get()!!)
                .setTitle("需要手动设置权限")
                .setRationale("存在永久拒绝的权限 , 需要手动前往设置界面为应用进行授权")
                .setPositiveButton("前往设置界面")
                .setNegativeButton("不使用该功能")
                .build().show()
        }
    }

    companion object {
        @JvmStatic
        val instance by lazy { PermissionFactory() }
    }

}