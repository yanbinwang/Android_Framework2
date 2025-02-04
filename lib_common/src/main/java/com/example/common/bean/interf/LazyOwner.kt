package com.example.common.bean.interf

/**
 * @description
 * fragment在使用FragmentTransaction切换时，
 * 生命周期的走向会出问题，接入当前注解在基类中处理
 * @author yan
 * val clazz = source::class.java.getAnnotation(SocketRequest::class.java)
 */
annotation class LazyOwner