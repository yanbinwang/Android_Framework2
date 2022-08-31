package com.example.base.function

/**
 * @description
 * @author
 */
/**
 * 现在的运行时间，用来作时间间隔判断
 * */
val currentTimeNano: Long
    get() {
        return System.nanoTime() / 1000000L
    }