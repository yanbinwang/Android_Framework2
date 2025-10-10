package com.example.mvvm.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.framework.utils.function.value.orFalse

/**
 * 手势管理
 * private val gestureManager by lazy { GestureManager(object :GestureCallback{
 *  }) }
 *  override fun onTouchEvent(event: MotionEvent?): Boolean {
 *   // 先让手势管理器处理，再决定是否传递给父类
 *   val handled = gestureManager.onTouchEvent(event)
 *   return handled || super.onTouchEvent(event)
 *  }
 */
class GestureManager(private val callback: GestureCallback) {
    // 使用SimpleOnGestureListener可以只重写需要的方法
    private var gestureDetector: GestureDetector? = null

    /**
     * 1.创建 GestureDetector 对象：在Activity中声明一个GestureDetector对象，并在onCreate方法中进行初始化。(val mGestureDetector = GestureDetector(this, this))
     * 2.让 Activity 实现 OnGestureListener 接口：OnGestureListener接口定义了处理各种手势事件的方法。可以让Activity直接实现该接口，然后重写需要的手势处理方法。
     * 3.重写 onTouchEvent 方法：在Activity中重写onTouchEvent方法，将触摸事件传递给GestureDetector进行处理。
     * (如果不想实现OnGestureListener接口中的所有方法，也可以继承GestureDetector.SimpleOnGestureListener类，它是一个包含了OnGestureListener所有方法的空实现类，开发者可以根据需求选择性地重写其中的手势处理方法)
     */
    fun init(context: Context) {
        gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean {
                /**
                 * 手指按下时的处理逻辑
                 * 让 onDown 返回 true，本质是告诉系统 “我要关注这个手势的后续动作”，而非 “拦截事件不让子 View 用”
                 * 它不影响子 View 的触摸事件，只确保系统会继续把 onScroll、onFling 等后续手势回调。
                 * 如果 onDown 返回 false，可能会导致手指滑动时，onScroll 完全不触发；
                 * 但返回 true 后，onScroll 就能正常响应 —— 因为系统会认为 “你不关心这个手势”，直接跳过了后续识别。
                 */
                callback.onDown(e)
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                // 手指快速滑动时的处理逻辑
                callback.onFling(e1, e2, velocityX, velocityY)
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                // 手指长按的处理逻辑
                callback.onLongPress(e)
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                // 手指滑动时的处理逻辑
                callback.onScroll(e1, e2, distanceX, distanceY)
                return false
            }

            override fun onShowPress(e: MotionEvent) {
                // 手指按住未移动时的处理逻辑
                callback.onShowPress(e)
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 手指单点抬起时的处理逻辑
                callback.onSingleTapUp(e)
                return false
            }
        })
    }

    /**
     * 提供方法让外部传递触摸事件
     */
    fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        return gestureDetector?.onTouchEvent(event).orFalse
    }

    /**
     * 定义接口回调，将手势事件暴露给外部
     */
    interface GestureCallback {
        fun onDown(e: MotionEvent) {}
        fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float) {}
        fun onLongPress(e: MotionEvent) {}
        fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float) {}
        fun onShowPress(e: MotionEvent) {}
        fun onSingleTapUp(e: MotionEvent) {}
    }

}