package com.example.common.utils.permission

/**
 * author: wyb
 * date: 2018/6/11.
 * 权限回调监听
 */
interface OnAndPermissionListener {

    fun onAndPermissionListener(isGranted: Boolean = false)

}
