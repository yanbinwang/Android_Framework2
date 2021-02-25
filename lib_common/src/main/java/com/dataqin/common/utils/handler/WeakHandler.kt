package com.dataqin.common.utils.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
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
    private val mLock: Lock = ReentrantLock()
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
        return mExec!!.post(wrapRunnable(r))
    }

    fun postAtTime(r: Runnable?, uptimeMillis: Long): Boolean {
        return mExec!!.postAtTime(wrapRunnable(r), uptimeMillis)
    }

    fun postAtTime(r: Runnable?, token: Any, uptimeMillis: Long): Boolean {
        return mExec!!.postAtTime(wrapRunnable(r), token, uptimeMillis)
    }

    fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        return mExec!!.postDelayed(wrapRunnable(r), delayMillis)
    }

    fun postAtFrontOfQueue(r: Runnable?): Boolean {
        return mExec!!.postAtFrontOfQueue(wrapRunnable(r))
    }

    private fun wrapRunnable(r: Runnable?): WeakRunnable {
        if (r == null) {
            throw NullPointerException("Runnable can't be null")
        }
        val hardRef = ChainedRef(mLock, r)
        mRunnables.insertAfter(hardRef)
        return hardRef.wrapper!!
    }

    fun removeCallbacks(r: Runnable) {
        val runnable: WeakRunnable? = mRunnables.remove(r)
        if (runnable != null) {
            mExec!!.removeCallbacks(runnable)
        }
    }

    fun removeCallbacks(r: Runnable, token: Any) {
        val runnable: WeakRunnable? = mRunnables.remove(r)
        if (runnable != null) {
            mExec!!.removeCallbacks(runnable, token)
        }
    }

    fun sendMessage(msg: Message): Boolean {
        return mExec!!.sendMessage(msg)
    }

    fun sendEmptyMessage(what: Int): Boolean {
        return mExec!!.sendEmptyMessage(what)
    }

    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        return mExec!!.sendEmptyMessageDelayed(what, delayMillis)
    }

    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        return mExec!!.sendEmptyMessageAtTime(what, uptimeMillis)
    }

    fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean {
        return mExec!!.sendMessageDelayed(msg, delayMillis)
    }

    fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        return mExec!!.sendMessageAtTime(msg, uptimeMillis)
    }

    fun sendMessageAtFrontOfQueue(msg: Message): Boolean {
        return mExec!!.sendMessageAtFrontOfQueue(msg)
    }

    fun removeMessages(what: Int) {
        mExec!!.removeMessages(what)
    }

    fun removeMessages(what: Int, obj: Any?) {
        mExec!!.removeMessages(what, obj)
    }

    fun removeCallbacksAndMessages(token: Any) {
        mExec!!.removeCallbacksAndMessages(token)
    }

    fun hasMessages(what: Int): Boolean {
        return mExec!!.hasMessages(what)
    }

    fun hasMessages(what: Int, obj: Any?): Boolean {
        return mExec!!.hasMessages(what, obj)
    }

    fun getLooper(): Looper {
        return mExec!!.looper
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
            if (mCallback == null) {
                return
            }
            val callback = mCallback?.get() ?: return
            callback.handleMessage(msg)
        }

    }

    private class WeakRunnable(delegate: WeakReference<Runnable>, reference: WeakReference<ChainedRef>) : Runnable {
        private var mDelegate: WeakReference<Runnable>? = delegate
        private var mReference: WeakReference<ChainedRef>? = reference

        override fun run() {
            val delegate = mDelegate?.get()
            val reference: ChainedRef? = mReference?.get()
            reference?.remove()
            delegate?.run()
        }

    }

    private class ChainedRef(lock: Lock, r: Runnable?) {
        var next: ChainedRef? = null
        var prev: ChainedRef? = null
        var lock: Lock? = lock
        var runnable: Runnable? = r
        var wrapper: WeakRunnable? = null

        init {
            this.wrapper = WeakRunnable(WeakReference(r), WeakReference(this))
        }

        fun remove(): WeakRunnable? {
            lock?.lock()
            try {
                if (prev != null) {
                    prev?.next = next
                }
                if (next != null) {
                    next?.prev = prev
                }
                prev = null
                next = null
            } finally {
                lock?.unlock()
            }
            return wrapper
        }

        fun insertAfter(candidate: ChainedRef) {
            lock?.lock()
            try {
                if (next != null) {
                    next?.prev = candidate
                }
                candidate.next = next
                next = candidate
                candidate.prev = this
            } finally {
                lock?.unlock()
            }
        }

        fun remove(obj: Runnable): WeakRunnable? {
            lock?.lock()
            try {
                var curr: ChainedRef? = next
                while (curr != null) {
                    if (curr.runnable === obj) {
                        return curr.remove()
                    }
                    curr = curr.next
                }
            } finally {
                lock?.unlock()
            }
            return null
        }

    }

}