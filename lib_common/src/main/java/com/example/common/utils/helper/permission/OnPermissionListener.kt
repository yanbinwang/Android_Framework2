package com.example.common.utils.helper.permission

/**
 * author: wyb
 * date: 2018/6/11.
 * 权限回调监听
 */
interface OnPermissionListener {

    fun onAndPermissionListener(isGranted: Boolean = false)

}
