package com.example.framework.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.framework.utils.function.value.orFalse
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 *  Created by wangyanbin
 *  弱handler
 */
class WeakHandler {
    private var mCallback: Handler.Callback? = null
    private var mExec: ExecHandler? = null
    private val mLock by lazy { ReentrantLock() }
    private val mRunnables by lazy { ChainedRef(mLock, null) }

    constructor() {
        mCallback = null
        mExec = ExecHandler()
    }

    constructor(callback: Handler.Callback) {
        mCallback = callback
        mExec = ExecHandler(WeakReference(callback))
    }

    constructor(looper: Looper) {
        mCallback = null
        mExec = ExecHandler(looper)
    }

    constructor(looper: Looper, callback: Handler.Callback) {
        mCallback = callback
        mExec = ExecHandler(looper, WeakReference(callback))
    }

    fun post(r: Runnable): Boolean {
        return mExec?.post(wrapRunnable(r)).orFalse
    }

    fun postAtTime(r: Runnable, uptimeMillis: Long): Boolean {
        return mExec?.postAtTime(wrapRunnable(r), uptimeMillis).orFalse
    }

    fun postAtTime(r: Runnable, token: Any, uptimeMillis: Long): Boolean {
        return mExec?.postAtTime(wrapRunnable(r), token, uptimeMillis).orFalse
    }

    fun postDelayed(r: Runnable, delayMillis: Long): Boolean {
        return mExec?.postDelayed(wrapRunnable(r), delayMillis).orFalse
    }

    fun postAtFrontOfQueue(r: Runnable): Boolean {
        return mExec?.postAtFrontOfQueue(wrapRunnable(r)).orFalse
    }

    private fun wrapRunnable(r: Runnable?): WeakRunnable {
        if (r == null) throw NullPointerException("Runnable can't be null")
        val hardRef = ChainedRef(mLock, r)
        mRunnables.insertAfter(hardRef)
        return hardRef.wrapper
    }

    fun removeCallbacks(r: Runnable) {
        val runnable = mRunnables.remove(r) ?: return
        mExec?.removeCallbacks(runnable)
    }

    fun removeCallbacks(r: Runnable, token: Any?) {
        val runnable = mRunnables.remove(r) ?: return
        mExec?.removeCallbacks(runnable, token)
    }

    fun sendMessage(msg: Message): Boolean {
        return mExec?.sendMessage(msg).orFalse
    }

    fun sendEmptyMessage(what: Int): Boolean {
        return mExec?.sendEmptyMessage(what).orFalse
    }

    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        return mExec?.sendEmptyMessageDelayed(what, delayMillis).orFalse
    }

    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        return mExec?.sendEmptyMessageAtTime(what, uptimeMillis).orFalse
    }

    fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean {
        return mExec?.sendMessageDelayed(msg, delayMillis).orFalse
    }

    fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        return mExec?.sendMessageAtTime(msg, uptimeMillis).orFalse
    }

    fun sendMessageAtFrontOfQueue(msg: Message): Boolean {
        return mExec?.sendMessageAtFrontOfQueue(msg).orFalse
    }

    fun removeMessages(what: Int) {
        mExec?.removeMessages(what)
    }

    fun removeMessages(what: Int, obj: Any?) {
        mExec?.removeMessages(what, obj)
    }

    fun removeCallbacksAndMessages(token: Any?) {
        mExec?.removeCallbacksAndMessages(token)
    }

    fun hasMessages(what: Int): Boolean {
        return mExec?.hasMessages(what).orFalse
    }

    fun hasMessages(what: Int, obj: Any?): Boolean {
        return mExec?.hasMessages(what, obj).orFalse
    }

    fun getLooper(): Looper? {
        return mExec?.looper
    }

    private class ExecHandler : Handler {
        private var mCallback: WeakReference<Callback>? = null

        constructor() {
            mCallback = null
        }

        constructor(callback: WeakReference<Callback>) {
            mCallback = callback
        }

        constructor(looper: Looper) : super(looper) {
            mCallback = null
        }

        constructor(looper: Looper, callback: WeakReference<Callback>) : super(looper) {
            mCallback = callback
        }

        override fun handleMessage(msg: Message) {
            if (mCallback == null) return
            val callback = mCallback?.get() ?: return
            callback.handleMessage(msg)
        }

    }

    private class WeakRunnable(private val delegate: WeakReference<Runnable>, private val reference: WeakReference<ChainedRef>) : Runnable {

        override fun run() {
            reference.get()?.remove()
            delegate.get()?.run()
        }

    }

    private class ChainedRef(private val lock: Lock, private val runnable: Runnable?) {
        var next: ChainedRef? = null
        var prev: ChainedRef? = null
        var wrapper = WeakRunnable(WeakReference(runnable), WeakReference(this))

        fun remove(): WeakRunnable {
            lock.lock()
            try {
                prev?.next = next
                next?.prev = prev
                prev = null
                next = null
            } finally {
                lock.unlock()
            }
            return wrapper
        }

        fun insertAfter(candidate: ChainedRef) {
            lock.lock()
            try {
                next?.prev = candidate
                candidate.next = next
                next = candidate
                candidate.prev = this
            } finally {
                lock.unlock()
            }
        }

        fun remove(obj: Runnable): WeakRunnable? {
            lock.lock()
            try {
                var curr = next
                while (curr != null) {
                    if (curr.runnable === obj) return curr.remove()
                    curr = curr.next
                }
            } finally {
                lock.unlock()
            }
            return null
        }

    }

}