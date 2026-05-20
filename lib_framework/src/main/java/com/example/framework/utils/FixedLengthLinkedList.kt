package com.example.framework.utils

import java.util.ArrayDeque
import java.util.Deque

/**
 * LinkedList类提供了一个可以动态调整长度的列表。
 * 1) 如果你想要一个固定长度的列表，你可以使用ArrayDeque，它是LinkedList的固定长度版本，并且在大多数操作上具有更好的性能。
 * 2) 在添加元素时，如果列表已满，则会从相应的末尾移除元素以保持固定长度。
 */
class FixedLengthLinkedList<T>(maxSize: Int = 10) {
    // 自动容错：非法值 <=0 自动转为 10
    private val maxSize = maxSize.coerceAtLeast(10)
    // 安全锁
    private val lock = Any()
    // ArrayDeque 初始化极轻
    private val deque: Deque<T> = ArrayDeque(this.maxSize)
    // 列表长度
    val size get() = synchronized(lock) { deque.size }

    /**
     * 头部添加元素
     */
    fun addFirst(element: T) {
        synchronized(lock) {
            if (deque.size >= maxSize) {
                deque.removeLast()
            }
            deque.addFirst(element)
        }
    }

    /**
     * 尾部添加元素
     */
    fun addLast(element: T) {
        synchronized(lock) {
            if (deque.size >= maxSize) {
                deque.removeFirst()
            }
            deque.addLast(element)
        }
    }

    /**
     * 移除头部，空返回null
     */
    fun removeFirstOrNull(): T? {
        return synchronized(lock) {
            deque.pollFirst()
        }
    }

    /**
     * 移除尾部，空返回null
     */
    fun removeLastOrNull(): T? {
        return synchronized(lock) {
            deque.pollLast()
        }
    }

    /**
     * 清空列表
     */
    fun clear() {
        synchronized(lock) {
            deque.clear()
        }
    }

    /**
     * 列表是否为空
     */
    fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * 线程安全的只读列表
     */
    fun getReadOnlyList(): List<T> {
        return synchronized(lock) {
            deque.toList()
        }
    }

}