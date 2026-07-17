package com.example.framework.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 *  Created by wangyanbin
 *  弱 Handler
 */
class WeakHandler {
    private var mCallback: Handler.Callback? = null
    private val mExec: ExecHandler
    private val mLock = ReentrantLock()
    private val mRunnables = ChainedRef(mLock, null)

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

    fun post(r: Runnable?): Boolean {
        return mExec.post(wrapRunnable(r))
    }

    fun postAtTime(r: Runnable?, uptimeMillis: Long): Boolean {
        return mExec.postAtTime(wrapRunnable(r), uptimeMillis)
    }

    fun postAtTime(r: Runnable?, token: Any, uptimeMillis: Long): Boolean {
        return mExec.postAtTime(wrapRunnable(r), token, uptimeMillis)
    }

    fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        return mExec.postDelayed(wrapRunnable(r), delayMillis)
    }

    fun postAtFrontOfQueue(r: Runnable?): Boolean {
        return mExec.postAtFrontOfQueue(wrapRunnable(r))
    }

    private fun wrapRunnable(r: Runnable?): WeakRunnable {
        requireNotNull(r) { "Runnable can't be null" }
        val hardRef = ChainedRef(mLock, r)
        mRunnables.insertAfter(hardRef)
        return hardRef.wrapper
    }

    fun removeCallbacks(r: Runnable) {
        val runnable = mRunnables.remove(r) ?: return
        mExec.removeCallbacks(runnable)
    }

    fun removeCallbacks(r: Runnable, token: Any?) {
        val runnable = mRunnables.remove(r) ?: return
        mExec.removeCallbacks(runnable, token)
    }

    fun sendMessage(msg: Message): Boolean {
        return mExec.sendMessage(msg)
    }

    fun sendEmptyMessage(what: Int): Boolean {
        return mExec.sendEmptyMessage(what)
    }

    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        return mExec.sendEmptyMessageDelayed(what, delayMillis)
    }

    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        return mExec.sendEmptyMessageAtTime(what, uptimeMillis)
    }

    fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean {
        return mExec.sendMessageDelayed(msg, delayMillis)
    }

    fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        return mExec.sendMessageAtTime(msg, uptimeMillis)
    }

    fun sendMessageAtFrontOfQueue(msg: Message): Boolean {
        return mExec.sendMessageAtFrontOfQueue(msg)
    }

    fun removeMessages(what: Int) {
        mExec.removeMessages(what)
    }

    fun removeMessages(what: Int, obj: Any?) {
        mExec.removeMessages(what, obj)
    }

    fun removeCallbacksAndMessages(token: Any?) {
        mExec.removeCallbacksAndMessages(token)
    }

    fun hasMessages(what: Int): Boolean {
        return mExec.hasMessages(what)
    }

    fun hasMessages(what: Int, obj: Any?): Boolean {
        return mExec.hasMessages(what, obj)
    }

    fun getLooper(): Looper {
        return mExec.looper
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
            mCallback?.get()?.handleMessage(msg)
        }

    }

    private class WeakRunnable(private val delegate: WeakReference<Runnable>, private val reference: WeakReference<ChainedRef>) : Runnable {

        override fun run() {
            reference.get()?.remove()
            delegate.get()?.run()
        }

    }

    private class ChainedRef(private val lock: Lock, private val runnable: Runnable?) {
        private var next: ChainedRef? = null
        private var prev: ChainedRef? = null
        var wrapper = WeakRunnable(WeakReference(runnable), WeakReference(this))
            private set

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