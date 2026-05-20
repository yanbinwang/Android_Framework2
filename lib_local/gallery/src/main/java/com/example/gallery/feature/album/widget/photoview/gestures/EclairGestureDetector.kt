package com.example.gallery.feature.album.widget.photoview.gestures

import android.content.Context
import android.view.MotionEvent

/**
 * Android 2.1 专用手势检测器
 * 主要作用：处理【多点触控】时的手指切换逻辑
 * 继承自 CupcakeGestureDetector
 */
abstract class EclairGestureDetector(context: Context) : CupcakeGestureDetector(context) {
    // 当前有效的手指索引
    private var mActivePointerIndex = 0
    // 当前有效的手指ID
    private var mActivePointerId = INVALID_POINTER_ID

    companion object {
        // 无效手指ID常量
        private const val INVALID_POINTER_ID = -1

        /**
         * 获取多点触控的“手指索引”
         * 用于：双指缩放、多指滑动
         */
        private fun getPointerIndex(action: Int): Int {
            return (action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        }
    }

    /**
     * 处理触摸事件
     * 处理多指触摸时，抬起一根手指后，自动切换到另一根手指继续拖动
     */
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> mActivePointerId = ev.getPointerId(0)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_POINTER_UP -> {
                // 获取抬起的手指索引
                val pointerIndex = getPointerIndex(ev.action)
                val pointerId = ev.getPointerId(pointerIndex)
                // 如果抬起的是当前有效的手指
                if (pointerId == mActivePointerId) {
                    // 自动切换到另一根手指
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                    // 更新最后触摸位置
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                }
            }
        }
        // 找到当前有效手指的索引
        mActivePointerIndex = ev.findPointerIndex(if (mActivePointerId != INVALID_POINTER_ID) mActivePointerId else 0)
        // 交给父类处理拖动逻辑，自带防崩溃
        return try {
            super.onTouchEvent(ev)
        } catch (_: IllegalArgumentException) {
            // 兼容系统库崩溃
            true
        }
    }

    /**
     * 获取当前有效手指的 X 坐标
     * 兼容处理，异常时返回默认手指坐标
     */
    override fun getActiveX(ev: MotionEvent): Float {
        return try {
            ev.getX(mActivePointerIndex)
        } catch (_: Exception) {
            ev.x
        }
    }

    /**
     * 获取当前有效手指的 Y 坐标
     */
    override fun getActiveY(ev: MotionEvent): Float {
        return try {
            ev.getY(mActivePointerIndex)
        } catch (_: Exception) {
            ev.y
        }
    }

}