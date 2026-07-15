package com.example.framework.utils

import java.util.ArrayDeque
import java.util.Deque

/**
 * 基于ArrayDeque封装的线程安全定长双端队列
 * 内部底层使用 ArrayDeque，读写性能优于 LinkedList
 * 添加元素达到最大容量时，自动淘汰对应一端旧数据，维持固定长度
 */
class FixedLengthLinkedList<T>(maxSize: Int = 10) {
    // 自动容错：非法值 <=0 自动转为 10
    private val maxSize = maxSize.coerceAtLeast(10)
    // 安全锁
    private val lock = Any()
    // ArrayDeque 初始化极轻，无需预分配容量，内部自动扩容
    private val deque: Deque<T> = ArrayDeque()

    /**
     * 头部添加元素
     * 容量超出maxSize时，自动移除尾部最旧元素
     */
    fun addFirst(element: T) {
        synchronized(lock) {
            if (deque.size >= maxSize) deque.removeLast()
            deque.addFirst(element)
        }
    }

    /**
     * 尾部添加元素
     * 容量超出maxSize时，自动移除头部最旧元素
     */
    fun addLast(element: T) {
        synchronized(lock) {
            if (deque.size >= maxSize) deque.removeFirst()
            deque.addLast(element)
        }
    }

    /**
     * 查看头部元素，不移除，空返回null
     */
    fun peekFirstOrNull(): T? {
        return synchronized(lock) { deque.peekFirst() }
    }

    /**
     * 查看尾部元素，不移除，空返回null
     */
    fun peekLastOrNull(): T? {
        return synchronized(lock) { deque.peekLast() }
    }

    /**
     * 移除头部，空返回null
     */
    fun removeFirstOrNull(): T? {
        return synchronized(lock) { deque.pollFirst() }
    }

    /**
     * 移除尾部，空返回null
     */
    fun removeLastOrNull(): T? {
        return synchronized(lock) { deque.pollLast() }
    }

    /**
     * 清空列表
     */
    fun clear() {
        synchronized(lock) { deque.clear() }
    }

    /**
     * 列表是否为空
     */
    fun isEmpty(): Boolean {
        return synchronized(lock) { deque.isEmpty() }
    }

    /**
     * 列表长度
     */
    fun getSize(): Int {
        return synchronized(lock) { deque.size }
    }

    /**
     * 线程安全的只读列表
     */
    fun getReadOnlyList(): List<T> {
        return synchronized(lock) { deque.toList() }
    }

}