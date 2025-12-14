package com.example.framework.utils

import java.util.ArrayDeque
import java.util.Deque

/**
 * LinkedList类提供了一个可以动态调整长度的列表。
 * 1.如果你想要一个固定长度的列表，你可以使用ArrayDeque，它是LinkedList的固定长度版本，并且在大多数操作上具有更好的性能。
 * 2.在添加元素时，如果列表已满，则会从相应的末尾移除元素以保持固定长度。
 */
class FixedLengthLinkedList<T>(private val maxSize: Int = 0) {
    private val deque: Deque<T> by lazy { ArrayDeque(maxSize) }
    private val LOCK = Any()

    /**
     * 首位添加
     */
    fun addFirst(element: T) {
        synchronized(LOCK) {
            if (deque.size >= maxSize) {
                deque.removeLast()
            }
            deque.addFirst(element)
        }
    }

    /**
     * 结尾添加
     */
    fun addLast(element: T) {
        synchronized(LOCK) {
            if (deque.size >= maxSize) {
                deque.removeFirst()
            }
            deque.addLast(element)
        }
    }

    /**
     * 首位删除
     */
    fun removeFirst(): T? {
        return synchronized(LOCK) {
            try {
                deque.removeFirst()
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }

    /**
     * 底部增加
     */
    fun removeLast(): T? {
        return synchronized(LOCK) {
            try {
                deque.removeLast()
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }

    /**
     * 获取当前集合
     */
    fun getReadOnlyList(): List<T> {
        return synchronized(LOCK) {
            deque.toList()
        }
    }

}